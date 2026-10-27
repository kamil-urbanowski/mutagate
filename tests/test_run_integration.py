"""Whole runs of a real corpus class, targeted and control, with canned replies.

No model: a canned provider answers requests about GameZoneUnicesar's
`CategoryDiscount` with the gate check's hand-written tests, renamed, and
every other request with no test. So this checks `mutagate run` and `mutagate
run --control` end to end with real builds and real PIT runs. The targeted run
is made again to check that every reply is reused and nothing is sent twice.
Needs git, Maven, and JDK 21; a few minutes. Opt in with MUTAGATE_INTEGRATION=1.
"""

import re

import pytest

from mutagate import cli, generate, mutants, report
from mutagate.gate import Verdict
from mutagate.providers.base import SavedReplies

CLASS = "com.gamezone.model.CategoryDiscount"
NO_TEST = "Sorry, I cannot write that test."

pytestmark = pytest.mark.integration


class CannedProvider:
    """Answers by the class name a prompt asks for, with its replies in order;
    no test once they run out, or for any other class."""

    name = "canned"
    version = ""
    last_usage = None

    def __init__(self, replies):
        self.replies = {simple: list(texts) for simple, texts in replies.items()}
        self.asked = []
        self.requests = []
        self.settings = {"model": "canned"}

    def complete(self, shared, task):
        simple = re.search(r"^Class name: (\w+)$", task, re.MULTILINE).group(1)
        self.asked.append(simple)
        self.requests.append((shared, task))
        queued = self.replies.get(simple)
        return queued.pop(0) if queued else NO_TEST


def renamed(gate_fixtures, fixture, simple):
    """A reply holding one of the gate check's tests under a generated name."""
    source = (gate_fixtures / f"{fixture}.java").read_text().replace(fixture, simple)
    return f"```java\n{source}```\n"


def test_targeted_run_and_rerun(tmp_path, gamezone_repo, corpus, gate_fixtures):
    baseline = report.read_baseline("GameZoneUnicesar", root=corpus)
    targets = mutants.select_targets(baseline, CLASS)
    line_81 = next(i for i, t in enumerate(targets, start=1) if t.line == 81)
    name = generate.test_class_name(CLASS, line_81)
    simple = name.rpartition(".")[2]

    canned = CannedProvider({simple: [renamed(gate_fixtures, "CategoryDiscountGate1Test", simple)]})
    metrics = cli.run_targeted(
        gamezone_repo, CLASS, SavedReplies(canned, tmp_path / "replies"), corpus=corpus
    )

    verdicts = {r.candidate.test_class: r.verdict for r in metrics.ledger}
    assert verdicts.pop(name) is Verdict.KEPT
    assert set(verdicts.values()) == {Verdict.NO_USABLE_TEST}  # after their repairs
    assert len(metrics.requests) == 1 + 2 * (len(targets) - 1)
    assert len({shared for shared, _ in canned.requests}) == 1  # one cached part for the class
    assert metrics.newly_detected >= 1
    assert metrics.score_after > metrics.score_before
    assert metrics.setup["provider"] == "canned" and metrics.setup["model"] == "canned"

    # The same run again: every reply is reused, nothing is asked, same verdicts.
    again = CannedProvider({})
    rerun = cli.run_targeted(
        gamezone_repo, CLASS, SavedReplies(again, tmp_path / "replies"), corpus=corpus
    )
    assert again.asked == []
    assert all(q.reused for q in rerun.requests)
    assert [r.verdict for r in rerun.ledger] == [r.verdict for r in metrics.ledger]

    path = report.write_run(metrics, root=tmp_path / "results")
    assert report.read_runs("GameZoneUnicesar", root=tmp_path / "results") == [metrics]
    kept = path.parent / "kept" / "src/test/java/com/gamezone/model" / f"{simple}.java"
    assert kept.is_file()


def test_control_run(tmp_path, gamezone_repo, corpus, gate_fixtures, capsys):
    baseline = report.read_baseline("GameZoneUnicesar", root=corpus)
    assert len(mutants.select_targets(baseline, CLASS)) == 3  # so 3 control requests
    names = [generate.test_class_name(CLASS, i).rpartition(".")[2] for i in (1, 2, 3)]
    kills_line_81 = "CategoryDiscountControl1Test"  # checks a console discount
    repeats_a_check = "CategoryDiscountControl2Test"

    canned = CannedProvider(
        {
            names[0]: [renamed(gate_fixtures, kills_line_81, names[0])],
            names[1]: [renamed(gate_fixtures, repeats_a_check, names[1])],
            # No test at first; the repair then repeats the first request's test.
            names[2]: [NO_TEST, renamed(gate_fixtures, kills_line_81, names[2])],
        }
    )
    metrics = cli.run_control(
        gamezone_repo, CLASS, SavedReplies(canned, tmp_path / "replies"), corpus=corpus
    )

    assert metrics.kind is report.RunKind.CONTROL
    assert [r.verdict for r in metrics.ledger] == [Verdict.KEPT, Verdict.NO_NEW_KILL, Verdict.KEPT]
    assert [r.candidate.attempts for r in metrics.ledger] == [1, 1, 2]
    assert all(r.candidate.target is None for r in metrics.ledger)
    assert canned.asked == [names[0], names[1], names[2], names[2]]

    # Methods longest first: calculateDiscount (12 lines with @Override), the
    # constructor (6), then the first of four 3-line methods. Nothing from PIT.
    assert len({shared for shared, _ in canned.requests}) == 1  # one cached part for the class
    first, second, third, repair = [task for _, task in canned.requests]
    assert "public double calculateDiscount(Sale sale) {" in first
    assert "public CategoryDiscount(String id, String name," in second
    assert "public double getPercentage() {" in third
    assert not any("PIT" in task for task in (first, second, third))
    assert repair.startswith(third) and "held no usable test" in repair
    output = capsys.readouterr().out
    assert "[1/3] public double calculateDiscount(Sale sale): passed the screen" in output
    assert "[3/3] public double getPercentage(): passed the screen, after a repair" in output

    # The repeated test is kept too, and counted as redundant.
    assert metrics.newly_detected == 1
    assert metrics.score_after > metrics.score_before
    assert report.redundant_kept(baseline, [metrics]) == 1

    path = report.write_run(metrics, root=tmp_path / "results")
    assert path.parent.name == "control"
    assert report.read_runs("GameZoneUnicesar", root=tmp_path / "results") == [metrics]
    kept = path.parent / "kept" / "src/test/java/com/gamezone/model"
    assert sorted(p.name for p in kept.iterdir()) == [f"{names[0]}.java", f"{names[2]}.java"]
