import json
from dataclasses import asdict
from pathlib import Path

import pytest

from mutagate import report
from mutagate.gate import Candidate, GateResult, Verdict
from mutagate.mutants import Mutant, Status, for_class, select_targets


def test_baseline_round_trip(tmp_path):
    mutants = [
        Mutant(
            "Foo.java",
            "pkg.Foo$1",
            "<init>",
            "()V",
            12,
            "M",
            (4, 9),
            "d",
            Status.KILLED,
            ("pkg.FooTest#adds",),
        ),
        Mutant("Foo.java", "pkg.Foo", "get", "()I", 20, "M", (5,), "d", Status.NO_COVERAGE),
    ]
    setup = {"repo_commit": "abc", "java": "21"}
    path = report.write_baseline("demo", mutants, setup, root=tmp_path)

    assert path == tmp_path / "demo" / "baseline.json"
    saved = json.loads(path.read_text())
    assert saved["repo_commit"] == "abc"
    assert saved["mutants"][0]["status"] == "KILLED"

    loaded = report.read_baseline("demo", root=tmp_path)
    assert loaded == mutants
    assert [(m.status, m.killing_tests) for m in loaded] == [
        (m.status, m.killing_tests) for m in mutants
    ]


def test_exclusions_and_failing_tests(tmp_path):
    (tmp_path / "demo").mkdir()
    (tmp_path / "demo" / "exclusions.json").write_text('{"pkg.Db": "needs a database"}')
    (tmp_path / "demo" / "failing-tests.json").write_text('["pkg.FlakyTest"]')

    assert report.read_exclusions("demo", root=tmp_path) == {"pkg.Db": "needs a database"}
    assert report.read_failing_tests("demo", root=tmp_path) == {"pkg.FlakyTest"}


# --------------------------------------------------------------------------
# results/
# --------------------------------------------------------------------------


def mutant(line, status=Status.SURVIVED, cls="pkg.Foo"):
    simple = cls.rpartition(".")[2]
    return Mutant(f"{simple}.java", cls, "get", "()I", line, "M", (1,), "d", status)


def candidate(index, target, attempts=1, source="class T {}\n"):
    name = f"pkg.FooMutaGate{index}Test"
    path = Path("src/test/java/pkg", f"FooMutaGate{index}Test.java")
    return Candidate(name, path, source, target, attempts)


def run(newly_detected=1, kept=True, target_class="pkg.Foo"):
    a, b, c = mutant(10), mutant(20, Status.NO_COVERAGE), mutant(30)
    killed = Mutant(
        **{**asdict(a), "status": Status.KILLED, "killing_tests": ("pkg.FooMutaGate1Test#t",)}
    )
    ledger = [
        GateResult(
            candidate(1, a, attempts=2),
            Verdict.KEPT if kept else Verdict.MISSED_TARGET,
            "",
            (killed,),
        ),
        GateResult(candidate(2, b), Verdict.MISSED_TARGET, "target ended NO_COVERAGE"),
        GateResult(candidate(3, c, attempts=2, source=None), Verdict.NO_USABLE_TEST, "no test"),
    ]
    tokens = {"input_tokens": 100, "output_tokens": 10}
    requests = [
        report.Request(
            "pkg.FooMutaGate1Test", 1, "r1", tokens, False, Verdict.DID_NOT_COMPILE, "e"
        ),
        report.Request("pkg.FooMutaGate1Test", 2, "r2", tokens, False),
        report.Request("pkg.FooMutaGate2Test", 1, "r3", tokens, True),
        report.Request("pkg.FooMutaGate3Test", 1, "r4", None, False, Verdict.NO_USABLE_TEST),
        report.Request("pkg.FooMutaGate3Test", 2, "r5", None, False, Verdict.NO_USABLE_TEST),
    ]
    return report.RunMetrics(
        repo="demo",
        target_class=target_class,
        kind=report.RunKind.TARGETED,
        setup={"model": "m", "effort": "medium", "provider": "fake"},
        mutants_total=5,
        undetected_before=3,
        newly_detected=newly_detected,
        ledger=ledger,
        requests=requests,
    )


def test_run_metrics():
    metrics = run()
    assert (metrics.score_before, metrics.score_after) == (2 / 5, 3 / 5)
    assert metrics.kept == 1
    assert metrics.acceptance_rate == 1 / 3
    assert metrics.attempts_per_kept_test == 5  # 2 + 1 + 2 generations for 1 kept test
    assert run(newly_detected=0, kept=False).attempts_per_kept_test is None
    assert metrics.tokens == {"input_tokens": 300, "output_tokens": 30}
    assert report.kept_after_repair([metrics]) == 1
    assert report.verdict_counts([metrics]) == {
        Verdict.KEPT: 1,
        Verdict.MISSED_TARGET: 1,
        Verdict.NO_USABLE_TEST: 1,
    }
    assert report.by_target_status([metrics]) == {
        Status.SURVIVED: (2, 1),
        Status.NO_COVERAGE: (1, 0),
    }


def test_run_round_trip_and_kept_tests(tmp_path):
    metrics = run()
    path = report.write_run(metrics, root=tmp_path)

    assert path == tmp_path / "demo" / "targeted" / "pkg.Foo.json"
    kept = tmp_path / "demo" / "targeted" / "kept" / "src/test/java/pkg/FooMutaGate1Test.java"
    assert kept.read_text() == "class T {}\n"
    assert len(list((tmp_path / "demo").rglob("*.java"))) == 1  # only kept tests are written
    saved = json.loads(path.read_text())
    assert saved["kept"] == 1 and saved["score_after"] == 3 / 5

    (loaded,) = report.read_runs("demo", root=tmp_path)
    assert loaded == metrics


def test_a_rerun_replaces_the_class_run_and_its_kept_tests(tmp_path):
    report.write_run(run(), root=tmp_path)
    report.write_run(run(newly_detected=0, kept=False), root=tmp_path)

    assert list((tmp_path / "demo").rglob("*.java")) == []
    (loaded,) = report.read_runs("demo", root=tmp_path)
    assert loaded.kept == 0


def test_headline_scores():
    baseline = [mutant(i, Status.KILLED) for i in range(6)] + [mutant(i) for i in range(6, 10)]
    runs = [run(newly_detected=2), run(newly_detected=1, target_class="pkg.Bar")]
    assert report.headline_scores(baseline, runs) == (0.6, 0.9)
    with pytest.raises(ValueError):
        report.headline_scores(baseline, [run(), run()])  # the same class twice


def test_summary(tmp_path):
    baseline = [mutant(i, Status.KILLED) for i in range(6)] + [mutant(i) for i in range(6, 10)]
    runs = [run(newly_detected=2), run(newly_detected=1, target_class="pkg.Bar")]
    path, summary = report.write_summary(
        "demo", baseline, runs, ["pkg.Foo", "pkg.Bar", "pkg.Baz"], tmp_path
    )

    assert json.loads(path.read_text()) == summary  # what is printed is what is saved
    targeted = summary["targeted"]
    assert (summary["detected_before"], targeted["detected_after"]) == (6, 9)
    assert targeted["kept"] == 2 and targeted["candidates"] == 6
    assert targeted["classes_not_run"] == ["pkg.Baz"]
    assert targeted["by_target_status"]["SURVIVED"] == {"candidates": 4, "kept": 2}
    assert targeted["requests"] == 10 and targeted["requests_reused"] == 2
    assert len(targeted["settings"]) == 1
    assert "control" not in summary


# --------------------------------------------------------------------------
# The status split, the gate's value, redundant kept tests, the comparison
# --------------------------------------------------------------------------

# A module of two classes. F4 and B2 were detected before any run.
F1, F2, F3, F4 = mutant(10), mutant(20, Status.NO_COVERAGE), mutant(30), mutant(40, Status.KILLED)
B1, B2 = mutant(10, cls="pkg.Bar"), mutant(20, Status.KILLED, cls="pkg.Bar")
MODULE = [F1, F2, F3, F4, B1, B2]
TARGETED, CONTROL = report.RunKind.TARGETED, report.RunKind.CONTROL


def entry(index, verdict, killed=(), target=None, attempts=1):
    """One candidate's ledger entry; `killed` are the mutants it killed in verification."""
    in_verification = tuple(
        Mutant(**{**asdict(m), "status": Status.KILLED, "killing_tests": (f"t{index}",)})
        for m in killed
    )
    return GateResult(candidate(index, target, attempts), verdict, "", in_verification)


def class_run(kind, class_name, newly_detected, *ledger):
    return report.RunMetrics(
        repo="demo",
        target_class=class_name,
        kind=kind,
        setup={"provider": "fake", "model": "m"},
        mutants_total=len(for_class(MODULE, class_name)),
        undetected_before=len(select_targets(MODULE, class_name)),
        newly_detected=newly_detected,
        ledger=list(ledger),
    )


def paired_runs():
    """Both run kinds on Foo; Bar has only a targeted run."""
    targeted_foo = class_run(
        TARGETED,
        "pkg.Foo",
        2,
        entry(1, Verdict.KEPT, (F1, F2), target=F1),
        entry(2, Verdict.KEPT, (F2,), target=F2),  # adds nothing to 1
        entry(3, Verdict.MISSED_TARGET, target=F3, attempts=2),
    )
    targeted_bar = class_run(TARGETED, "pkg.Bar", 1, entry(1, Verdict.KEPT, (B1,), target=B1))
    control_foo = class_run(
        CONTROL,
        "pkg.Foo",
        2,
        entry(1, Verdict.KEPT, (F2, F3, F4)),
        entry(2, Verdict.NO_NEW_KILL, (F4,)),
        entry(3, Verdict.NO_USABLE_TEST, attempts=2),
    )
    return targeted_foo, targeted_bar, control_foo


def test_newly_detected_by_status():
    # Kills of mutants detected before, and kills by rejected candidates, do not count.
    targeted = class_run(
        TARGETED,
        "pkg.Foo",
        2,
        entry(1, Verdict.KEPT, (F1, F2, F4), target=F1),
        entry(2, Verdict.MISSED_TARGET, (F3,), target=F2),
        entry(3, Verdict.MISSED_TARGET, target=F3),
    )
    assert report.newly_detected_by_status(MODULE, [targeted]) == {
        Status.SURVIVED: (2, 1),
        Status.NO_COVERAGE: (1, 1),
    }
    control = class_run(
        CONTROL,
        "pkg.Foo",
        1,
        entry(1, Verdict.KEPT, (F3, F4)),
        entry(2, Verdict.NO_NEW_KILL, (F4,)),
    )
    bar = class_run(CONTROL, "pkg.Bar", 1, entry(1, Verdict.KEPT, (B1,)))
    assert report.newly_detected_by_status(MODULE, [control, bar]) == {
        Status.SURVIVED: (3, 2),
        Status.NO_COVERAGE: (1, 0),
    }


def test_gate_rejections():
    control = class_run(
        CONTROL,
        "pkg.Foo",
        0,
        entry(1, Verdict.NO_NEW_KILL, (F4,)),
        entry(2, Verdict.DID_NOT_COMPILE, attempts=2),
        entry(3, Verdict.BATCH_FAILED),
    )
    # run(): KEPT, MISSED_TARGET, and NO_USABLE_TEST.
    assert report.gate_rejections([run(), control]) == {
        "rejected_at_screen": 2,
        "rejected_at_verification": 2,
        "batch_failed": 1,
    }


def test_redundant_kept():
    control = class_run(
        CONTROL,
        "pkg.Foo",
        3,
        entry(1, Verdict.KEPT, (F1, F4)),
        entry(2, Verdict.KEPT, (F1,)),  # repeats 1
        entry(3, Verdict.NO_NEW_KILL, (F4,)),
        entry(4, Verdict.KEPT, (F1, F2)),
        entry(5, Verdict.KEPT, (F2, F4)),  # F4 was detected before the run
        entry(6, Verdict.KEPT, (F3,)),
    )
    assert report.redundant_kept(MODULE, [control]) == 2
    assert report.redundant_kept(MODULE, [control, control]) == 4  # each run from its own start
    assert report.redundant_kept(MODULE, [run()]) == 0


def test_compare():
    targeted_foo, targeted_bar, control_foo = paired_runs()
    assert report.compare(MODULE, [targeted_foo, targeted_bar]) is None

    assert report.compare(MODULE, [targeted_foo, targeted_bar, control_foo]) == {
        "classes": ["pkg.Foo"],
        "targeted": {
            "score_after": 4 / 6,
            "candidates": 3,
            "kept": 2,
            "kept_with_new_kill": 1,  # 2 adds nothing to 1
            "attempts_per_kept_test": 2,
        },
        "control": {
            "score_after": 4 / 6,
            "candidates": 3,
            "kept": 1,
            "kept_with_new_kill": 1,
            "attempts_per_kept_test": 4,
        },
        "newly_detected": {"targeted_only": 1, "control_only": 1, "both": 1},  # F1, F3, F2
    }

    # Once every class has both runs, each side's after-score is its kind's headline.
    control_bar = class_run(CONTROL, "pkg.Bar", 0, entry(1, Verdict.NO_NEW_KILL, (B2,)))
    runs = [targeted_foo, targeted_bar, control_foo, control_bar]
    comparison = report.compare(MODULE, runs)
    for kind in report.RunKind:
        headline = report.headline_scores(MODULE, [r for r in runs if r.kind is kind])
        assert comparison[kind.value.lower()]["score_after"] == headline[1]


def test_summary_with_control_runs_and_the_comparison(tmp_path):
    path, summary = report.write_summary(
        "demo", MODULE, list(paired_runs()), ["pkg.Bar", "pkg.Foo"], tmp_path
    )

    assert json.loads(path.read_text()) == summary
    targeted, control = summary["targeted"], summary["control"]
    assert targeted["newly_detected_by_status"] == {
        "NO_COVERAGE": {"undetected": 1, "newly_detected": 1},
        "SURVIVED": {"undetected": 3, "newly_detected": 2},
    }
    assert targeted["redundant_kept"] == 1
    assert targeted["gate"] == {
        "rejected_at_screen": {"count": 0, "share": 0},
        "rejected_at_verification": {"count": 1, "share": 1 / 4},
        "batch_failed": {"count": 0, "share": 0},
    }
    assert control["gate"]["rejected_at_screen"] == {"count": 1, "share": 1 / 3}
    assert "by_target_status" in targeted and "by_target_status" not in control
    assert control["classes_not_run"] == ["pkg.Bar"]
    assert summary["comparison"]["classes"] == ["pkg.Foo"]


def test_control_run_round_trip(tmp_path):
    control = class_run(
        CONTROL,
        "pkg.Foo",
        2,
        entry(1, Verdict.KEPT, (F2, F3)),
        entry(2, Verdict.NO_NEW_KILL, (F4,)),
    )
    path = report.write_run(control, root=tmp_path)

    assert path == tmp_path / "demo" / "control" / "pkg.Foo.json"
    kept = tmp_path / "demo" / "control" / "kept" / "src/test/java/pkg/FooMutaGate1Test.java"
    assert kept.is_file()
    assert json.loads(path.read_text())["ledger"][0]["target"] is None
    report.write_run(run(), root=tmp_path)
    assert report.read_runs("demo", root=tmp_path) == [run(), control]  # targeted first
