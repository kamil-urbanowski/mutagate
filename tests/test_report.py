import json
from dataclasses import asdict
from pathlib import Path

import pytest

from mutagate import report
from mutagate.gate import Candidate, GateResult, Verdict
from mutagate.mutants import Mutant, Status


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


def mutant(line, status=Status.SURVIVED):
    return Mutant("Foo.java", "pkg.Foo", "get", "()I", line, "M", (1,), "d", status)


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
