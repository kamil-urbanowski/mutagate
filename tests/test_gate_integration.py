"""The gate on a real corpus class, with hand-written candidate tests.

Every build and PIT run is real, so this needs git, Maven, and JDK 21, and
takes about a minute. Opt in with MUTAGATE_INTEGRATION=1. On first use
`conftest.py` clones GameZoneUnicesar into `work/`, which needs internet. The
candidates are in `tests/fixtures/gate/`; each file says which verdict it
should get.
"""

from pathlib import Path

import pytest

from mutagate import cli, gate, mutants, pit, report
from mutagate.gate import Candidate, Verdict

CLASS = "com.gamezone.model.CategoryDiscount"
PACKAGE = "com.gamezone.model"

pytestmark = pytest.mark.integration


def judge(workspace: Path, batch: list[Candidate], targets: list) -> tuple[dict, list]:
    """Screen each candidate, verify the survivors together; verdicts by name."""
    # cli's own screen, so this checks the code `mutagate run` uses. No repair here.
    results = {c.test_class: cli._screen(workspace, c) for c in batch}
    passed = [c for c in batch if results[c.test_class] is None]
    verification = pit.parse_report(pit.run_pit(workspace, CLASS, set()))
    for result in gate.verify(passed, verification, targets):
        results[result.candidate.test_class] = result
    for name, result in results.items():
        print(f"{name.rsplit('.', 1)[1]}: {result.verdict.value} | {result.detail}")
    return results, verification


def test_gate_on_category_discount(gamezone_repo, corpus, gate_fixtures):
    baseline = report.read_baseline("GameZoneUnicesar", root=corpus)
    targets = mutants.select_targets(baseline, CLASS)
    by_line = {m.line: m for m in targets}
    assert sorted(by_line) == [37, 55, 81]

    def candidate(name, line=None, source=True):
        text = (gate_fixtures / f"{name}.java").read_text() if source else None
        path = Path("src/test/java", *PACKAGE.split("."), f"{name}.java")
        return Candidate(f"{PACKAGE}.{name}", path, text, by_line.get(line))

    workspace = pit.make_workspace(gamezone_repo)
    try:
        assert pit.compile_tests(workspace)[0]

        # Targeted batch.
        batch = [
            candidate("CategoryDiscountGate1Test", 81),
            candidate("CategoryDiscountGate2Test", 37),
            candidate("CategoryDiscountGate3Test", 55),
            candidate("CategoryDiscountGate4Test", 37),
            candidate("CategoryDiscountGate5Test", 37),
            candidate("CategoryDiscountGate6Test", 37),
            candidate("CategoryDiscountGate7Test", 55, source=False),
        ]
        results, verification = judge(workspace, batch, targets)
        verdicts = {name.rsplit(".", 1)[1]: r.verdict for name, r in results.items()}
        assert verdicts == {
            "CategoryDiscountGate1Test": Verdict.KEPT,
            "CategoryDiscountGate2Test": Verdict.MISSED_TARGET,
            "CategoryDiscountGate3Test": Verdict.DID_NOT_COMPILE,
            "CategoryDiscountGate4Test": Verdict.FAILED_ON_UNMODIFIED_CODE,
            "CategoryDiscountGate5Test": Verdict.NONDETERMINISTIC,
            "CategoryDiscountGate6Test": Verdict.KEPT,
            "CategoryDiscountGate7Test": Verdict.NO_USABLE_TEST,
        }
        missed = results[f"{PACKAGE}.CategoryDiscountGate2Test"]
        assert [m.line for m in missed.mutants_killed] == [55]
        compile_error = results[f"{PACKAGE}.CategoryDiscountGate3Test"].detail
        assert "getCategory()" in compile_error
        assert "src/test/java/com/gamezone/model/CategoryDiscountGate3Test.java:[30,52]" in (
            compile_error
        )
        assert str(workspace) not in compile_error
        assert "passed 3 of 5" in results[f"{PACKAGE}.CategoryDiscountGate5Test"].detail

        # After-score: only kept candidates' kills count. Line 55 was killed
        # by a rejected candidate, so it stays undetected.
        kept = [name for name, r in results.items() if r.kept]
        newly = mutants.newly_detected(baseline, verification, kept)
        assert sorted(m.line for m in newly) == [37, 81]
        before = mutants.for_class(baseline, CLASS)
        detected_before = sum(mutants.is_detected(m) for m in before)
        print(
            f"class score: {detected_before}/{len(before)} -> "
            f"{detected_before + len(newly)}/{len(before)}"
        )

        # Control batch: a separate run, so the targeted candidates leave first.
        for c in batch:
            pit.remove_candidate(workspace, c)
        controls = [
            candidate("CategoryDiscountControl1Test"),
            candidate("CategoryDiscountControl2Test"),
        ]
        results, _ = judge(workspace, controls, targets)
        assert results[f"{PACKAGE}.CategoryDiscountControl1Test"].verdict is Verdict.KEPT
        assert results[f"{PACKAGE}.CategoryDiscountControl2Test"].verdict is Verdict.NO_NEW_KILL
    finally:
        pit.remove_workspace(workspace)
