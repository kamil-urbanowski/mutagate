"""context.py and generate.py on a real corpus class, with canned replies.

No model: a fake provider returns a hand-written test (the gate check's
`CategoryDiscountGate1Test`, renamed), so this checks everything around the
model with real builds and a real PIT run. Needs git, Maven, and JDK 21; about
half a minute. Opt in with MUTAGATE_INTEGRATION=1. Uses the GameZoneUnicesar
clone from `conftest.py`, cloning it on first use.
"""

from pathlib import Path

import pytest

from mutagate import context, gate, generate, mutants, pit, report
from mutagate.gate import Verdict

CLASS = "com.gamezone.model.CategoryDiscount"

pytestmark = pytest.mark.integration


class CannedProvider:
    name = "canned"

    def __init__(self, *replies):
        self.replies = list(replies)
        self.prompts = []

    def complete(self, prompt):
        self.prompts.append(prompt)
        return self.replies.pop(0)


def test_generated_candidate_survives_real_builds_and_pit(gamezone_repo, corpus, gate_fixtures):
    baseline = report.read_baseline("GameZoneUnicesar", root=corpus)
    targets = mutants.select_targets(baseline, CLASS)
    target = next(t for t in targets if t.line == 81)
    name = generate.test_class_name(CLASS, targets.index(target) + 1)
    simple = name.rpartition(".")[2]
    fixture = (gate_fixtures / "CategoryDiscountGate1Test.java").read_text()
    good = f"Here it is.\n\n```java\n{fixture.replace('CategoryDiscountGate1Test', simple)}```\n"

    workspace = pit.make_workspace(gamezone_repo)
    try:
        sources = pit.read_sources(workspace)  # before any candidate is added
        assert pit.compile_tests(workspace)[0]
        ctx = context.build_context(sources, CLASS, target)
        provider = CannedProvider("Sorry, I cannot do that.", good)

        # First reply unusable: the screen rejects it, and the repair is asked.
        first = generate.generate_test(ctx, provider, name)
        rejected = gate.check_usable(first)
        assert rejected.verdict is Verdict.NO_USABLE_TEST
        candidate = generate.repair(ctx, provider, first, rejected)
        assert candidate.attempts == 2
        assert candidate.path == Path("src/test/java/com/gamezone/model", f"{simple}.java")
        assert ">   81 |" in provider.prompts[0]
        assert "held no usable test" in provider.prompts[1]

        # The repaired candidate passes the screen with real builds...
        pit.add_candidate(workspace, candidate)
        assert gate.check_compiled(candidate, pit.compile_tests(workspace)) is None
        runs = pit.run_test(workspace, candidate.test_class, gate.SCREEN_RUNS)
        assert gate.check_runs(candidate, runs) is None

        # ...and a real scoped PIT run credits its kill to its generated name.
        verification = pit.parse_report(pit.run_pit(workspace, CLASS, set()))
        (result,) = gate.verify([candidate], verification, targets)
        assert result.verdict is Verdict.KEPT, result.detail
    finally:
        pit.remove_workspace(workspace)
