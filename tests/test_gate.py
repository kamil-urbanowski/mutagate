from pathlib import Path

import pytest

from mutagate.gate import (
    SCREEN_RUNS,
    Candidate,
    Verdict,
    batch_failed,
    check_compiled,
    check_runs,
    check_usable,
    verify,
)
from mutagate.mutants import Mutant, Status


def mutant(line, status=Status.SURVIVED, killing_tests=()):
    return Mutant("Foo.java", "pkg.Foo", "m", "()V", line, "M", (0,), "d", status, killing_tests)


def candidate(name="pkg.CandTest", target=None, source="class CandTest {}"):
    return Candidate(name, Path(f"src/test/java/{name}.java"), source, target)


COMPILE_FAILURE = """[INFO] Compiling 2 source files
[ERROR] COMPILATION ERROR :
[ERROR] /w/src/test/java/pkg/CandTest.java:[9,16] cannot find symbol
[INFO] 1 error
[ERROR] Failed to execute goal compiler:testCompile on project x: Compilation failure
[ERROR] /w/src/test/java/pkg/CandTest.java:[9,16] cannot find symbol
[ERROR]   symbol:   method nope()
[ERROR] -> [Help 1]
[ERROR]
[ERROR] To see the full stack trace of the errors, re-run Maven with the -e switch.
[ERROR] Re-run Maven using the -X switch to enable full debug logging.
"""

TEST_FAILURE = """[ERROR] Failures:
[ERROR]   CandTest.adds:12 expected: <3> but was: <4>
[ERROR] Tests run: 1, Failures: 1, Errors: 0, Skipped: 0
[ERROR] Please refer to /w/target/surefire-reports for the individual test results.
"""


# --------------------------------------------------------------------------
# Screen
# --------------------------------------------------------------------------


def test_usable():
    assert check_usable(candidate()) is None
    result = check_usable(candidate(source=None))
    assert result.verdict is Verdict.NO_USABLE_TEST


def test_compiled():
    assert check_compiled(candidate(), (True, "BUILD SUCCESS")) is None
    result = check_compiled(candidate(), (False, COMPILE_FAILURE))
    assert result.verdict is Verdict.DID_NOT_COMPILE
    assert result.detail.splitlines() == [
        "COMPILATION ERROR :",
        "/w/src/test/java/pkg/CandTest.java:[9,16] cannot find symbol",
        "Failed to execute goal compiler:testCompile on project x: Compilation failure",
        "symbol:   method nope()",
    ]


def test_detail_falls_back_to_output_tail():
    output = "\n".join(f"line {i}" for i in range(100))
    result = check_compiled(candidate(), (False, output))
    assert result.detail.splitlines() == [f"line {i}" for i in range(60, 100)]


def test_runs_all_pass():
    assert check_runs(candidate(), [(True, "")] * SCREEN_RUNS) is None


def test_runs_all_fail():
    result = check_runs(candidate(), [(False, TEST_FAILURE)] * SCREEN_RUNS)
    assert result.verdict is Verdict.FAILED_ON_UNMODIFIED_CODE
    assert "CandTest.adds:12 expected: <3> but was: <4>" in result.detail
    assert "Please refer to" not in result.detail


def test_runs_mixed_is_nondeterministic():
    runs = [(True, ""), (False, TEST_FAILURE), (True, ""), (False, TEST_FAILURE), (True, "")]
    result = check_runs(candidate(), runs)
    assert result.verdict is Verdict.NONDETERMINISTIC
    assert result.detail.startswith("passed 3 of 5 runs")


def test_runs_require_screen_runs():
    with pytest.raises(ValueError, match="expected 5 runs"):
        check_runs(candidate(), [(True, "")] * 3)


# --------------------------------------------------------------------------
# Verification
# --------------------------------------------------------------------------

TARGET = mutant(10)
OTHER = mutant(20)
DETECTED = mutant(30, Status.KILLED, ("pkg.FooTest#old",))
BEFORE = [TARGET, OTHER]


def run(*killers):
    """A verification run: TARGET and OTHER killed as given, DETECTED as before."""
    killed = dict(killers)
    return [
        mutant(10, Status.KILLED if killed.get(10) else Status.SURVIVED, killed.get(10, ())),
        mutant(20, Status.KILLED if killed.get(20) else Status.SURVIVED, killed.get(20, ())),
        mutant(30, Status.KILLED, ("pkg.FooTest#old",) + killed.get(30, ())),
    ]


def test_kept_when_it_kills_its_target():
    (result,) = verify([candidate(target=TARGET)], run((10, ("pkg.CandTest#kills",))), BEFORE)
    assert result.verdict is Verdict.KEPT
    assert result.mutants_killed == (TARGET,)


def test_kill_by_whole_class_counts():
    (result,) = verify([candidate(target=TARGET)], run((10, ("pkg.CandTest",))), BEFORE)
    assert result.verdict is Verdict.KEPT


def test_killing_another_mutant_does_not_count():
    (result,) = verify([candidate(target=TARGET)], run((20, ("pkg.CandTest#kills",))), BEFORE)
    assert result.verdict is Verdict.MISSED_TARGET
    assert result.mutants_killed == (OTHER,)
    assert result.detail == "target ended SURVIVED"


def test_target_killed_only_by_another_candidate():
    batch = [candidate("pkg.ATest", TARGET), candidate("pkg.BTest", OTHER)]
    verification = run((10, ("pkg.BTest#x",)), (20, ("pkg.BTest#x",)))
    a, b = verify(batch, verification, BEFORE)
    assert a.verdict is Verdict.MISSED_TARGET
    assert a.detail == "target KILLED, killed only by: pkg.BTest#x"
    assert b.verdict is Verdict.KEPT
    assert b.mutants_killed == (TARGET, OTHER)


def test_timed_out_target_earns_no_credit():
    verification = [mutant(10, Status.TIMED_OUT), OTHER, DETECTED]
    (result,) = verify([candidate(target=TARGET)], verification, BEFORE)
    assert result.verdict is Verdict.MISSED_TARGET
    assert "TIMED_OUT" in result.detail and "no credit" in result.detail


def test_class_name_prefix_is_not_a_kill():
    (result,) = verify([candidate(target=TARGET)], run((10, ("pkg.CandTest2#x",))), BEFORE)
    assert result.verdict is Verdict.MISSED_TARGET


def test_control_kept_for_a_new_kill():
    (result,) = verify([candidate()], run((20, ("pkg.CandTest#x",))), BEFORE)
    assert result.verdict is Verdict.KEPT
    assert result.detail == "killed 1 of the class's undetected mutants"


def test_control_killing_only_detected_mutants_is_rejected():
    (result,) = verify([candidate()], run((30, ("pkg.CandTest#x",))), BEFORE)
    assert result.verdict is Verdict.NO_NEW_KILL
    assert result.mutants_killed == (DETECTED,)


def test_duplicate_names_raise():
    with pytest.raises(ValueError, match="not unique"):
        verify([candidate(target=TARGET), candidate(target=OTHER)], run(), BEFORE)


def test_missing_target_raises():
    with pytest.raises(ValueError, match="not in the verification run"):
        verify([candidate(target=mutant(99))], run(), BEFORE)


def test_batch_failed():
    batch = [candidate("pkg.ATest", TARGET), candidate("pkg.BTest")]
    results = batch_failed(batch, "PIT run failed")
    assert [r.verdict for r in results] == [Verdict.BATCH_FAILED] * 2
    assert all(r.detail == "PIT run failed" for r in results)
