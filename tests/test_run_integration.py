"""A whole targeted run of a real corpus class, with canned replies.

No model: a canned provider answers the line-81 target of GameZoneUnicesar's
`CategoryDiscount` with the gate check's `CategoryDiscountGate1Test`, renamed,
and every other request with no test. So this checks `mutagate run` end to end
with real builds and a real PIT run, then runs it again to check that every
reply is reused and nothing is sent twice. Needs git, Maven, and JDK 21; about
two minutes. Opt in with MUTAGATE_INTEGRATION=1.
"""

import re

import pytest

from mutagate import cli, generate, mutants, report
from mutagate.gate import Verdict
from mutagate.providers.base import SavedReplies

CLASS = "com.gamezone.model.CategoryDiscount"

pytestmark = pytest.mark.integration


class CannedProvider:
    """Answers by the class name a prompt asks for; no test for any other."""

    name = "canned"
    version = ""
    last_usage = None

    def __init__(self, replies):
        self.replies = replies
        self.asked = []
        self.settings = {"model": "canned"}

    def complete(self, prompt):
        simple = re.search(r"^Class name: (\w+)$", prompt, re.MULTILINE).group(1)
        self.asked.append(simple)
        return self.replies.get(simple, "Sorry, I cannot write that test.")


def test_targeted_run_and_rerun(tmp_path, gamezone_repo, corpus, gate_fixtures):
    baseline = report.read_baseline("GameZoneUnicesar", root=corpus)
    targets = mutants.select_targets(baseline, CLASS)
    line_81 = next(i for i, t in enumerate(targets, start=1) if t.line == 81)
    name = generate.test_class_name(CLASS, line_81)
    simple = name.rpartition(".")[2]
    fixture = (gate_fixtures / "CategoryDiscountGate1Test.java").read_text()
    good = f"```java\n{fixture.replace('CategoryDiscountGate1Test', simple)}```\n"

    canned = CannedProvider({simple: good})
    metrics = cli.run_targeted(
        gamezone_repo, CLASS, SavedReplies(canned, tmp_path / "replies"), corpus=corpus
    )

    verdicts = {r.candidate.test_class: r.verdict for r in metrics.ledger}
    assert verdicts.pop(name) is Verdict.KEPT
    assert set(verdicts.values()) == {Verdict.NO_USABLE_TEST}  # after their repairs
    assert len(metrics.requests) == 1 + 2 * (len(targets) - 1)
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
