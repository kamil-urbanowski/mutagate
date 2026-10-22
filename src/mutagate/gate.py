"""Accept or reject candidate tests. Design: `docs/design/gate.md`.

Judges the outcomes `cli.py` hands it; it runs nothing and holds no provider.
"""

from dataclasses import dataclass
from enum import Enum
from pathlib import Path

from .mutants import Mutant, is_detected, killed_by

# How many times the screen runs a candidate against unmodified code.
SCREEN_RUNS = 5

# How many lines of a failed build's output a verdict keeps, for the ledger and
# the repair attempt.
DETAIL_LINES = 40


class Verdict(str, Enum):
    """The schema of a run ledger entry."""

    # Kept
    KEPT = "KEPT"

    # Rejected during the per-candidate screen
    NO_USABLE_TEST = "NO_USABLE_TEST"
    DID_NOT_COMPILE = "DID_NOT_COMPILE"
    FAILED_ON_UNMODIFIED_CODE = "FAILED_ON_UNMODIFIED_CODE"
    NONDETERMINISTIC = "NONDETERMINISTIC"

    # Rejected during batch verification
    MISSED_TARGET = "MISSED_TARGET"  # targeted: did not kill its own target
    NO_NEW_KILL = "NO_NEW_KILL"  # control: killed nothing the suite missed
    BATCH_FAILED = "BATCH_FAILED"  # the verification run itself failed


@dataclass
class Candidate:
    """One generated test: a complete test file holding exactly one test method.

    `test_class` is unique within the batch, which is what lets a kill be
    attributed to exactly one candidate. `source` is None when the model's reply
    held no usable test.
    """

    test_class: str  # fully qualified
    path: Path  # where the file goes, relative to the repository root
    source: str | None
    target: Mutant | None  # None for a control candidate
    attempts: int = 1


@dataclass
class GateResult:
    """The verdict on one candidate test, and why."""

    candidate: Candidate
    verdict: Verdict
    detail: str = ""
    mutants_killed: tuple[Mutant, ...] = ()  # recorded; only the target counts

    @property
    def kept(self) -> bool:
        return self.verdict is Verdict.KEPT


# --------------------------------------------------------------------------
# Stage 1 — per-candidate screen. No PIT. Each check returns None on a pass.
# --------------------------------------------------------------------------


def check_usable(candidate: Candidate) -> GateResult | None:
    """Reject a reply that held no usable test.

    What counts as usable is decided by `generate.extract_test`, which leaves
    `source` None otherwise.
    """
    if candidate.source is None:
        return GateResult(candidate, Verdict.NO_USABLE_TEST, "the reply held no usable test")
    return None


def check_compiled(candidate: Candidate, build: tuple[bool, str]) -> GateResult | None:
    """Judge the build that included the candidate. `build` is success and output."""
    ok, output = build
    if ok:
        return None
    return GateResult(candidate, Verdict.DID_NOT_COMPILE, _failure_lines(output))


def check_runs(candidate: Candidate, runs: list[tuple[bool, str]]) -> GateResult | None:
    """Judge the runs against unmodified code. Exactly `SCREEN_RUNS` are required,
    so "deterministic" means the same thing in every run of the tool."""
    if len(runs) != SCREEN_RUNS:
        raise ValueError(f"expected {SCREEN_RUNS} runs, got {len(runs)}")
    failures = [output for ok, output in runs if not ok]
    if not failures:
        return None
    if len(failures) == len(runs):
        return GateResult(candidate, Verdict.FAILED_ON_UNMODIFIED_CODE, _failure_lines(failures[0]))
    passed = len(runs) - len(failures)
    return GateResult(
        candidate,
        Verdict.NONDETERMINISTIC,
        f"passed {passed} of {len(runs)} runs against unmodified code\n"
        + _failure_lines(failures[0]),
    )


# --------------------------------------------------------------------------
# Stage 2 — batched verification. One scoped PIT run per class.
# --------------------------------------------------------------------------


def verify(
    candidates: list[Candidate], mutants: list[Mutant], undetected_before: list[Mutant]
) -> list[GateResult]:
    """Judge every screened candidate from one verification run's mutants.

    `undetected_before` is the class's targets, taken from the baseline; a
    control candidate is kept if it killed any of them. A targeted candidate is
    kept only if it is among its own target's killing tests, so a target that
    ends TIMED_OUT, which lists none, earns no credit.
    """
    names = [c.test_class for c in candidates]
    duplicates = sorted({name for name in names if names.count(name) > 1})
    if duplicates:
        raise ValueError(f"test class names not unique in the batch: {', '.join(duplicates)}")

    by_identity = {m: m for m in mutants}
    undetected = set(undetected_before)
    results = []
    for candidate in candidates:
        killed = tuple(m for m in mutants if killed_by(m, candidate.test_class))

        if candidate.target is None:
            new = [m for m in killed if m in undetected]
            if new:
                verdict = Verdict.KEPT
                detail = f"killed {len(new)} of the class's undetected mutants"
            else:
                verdict = Verdict.NO_NEW_KILL
                detail = "killed none of the class's undetected mutants"
            results.append(GateResult(candidate, verdict, detail, killed))
            continue

        target = by_identity.get(candidate.target)
        if target is None:
            raise ValueError(
                f"target of {candidate.test_class} is not in the verification run: "
                f"{_describe(candidate.target)}"
            )
        if killed_by(target, candidate.test_class):
            results.append(GateResult(candidate, Verdict.KEPT, "killed its target", killed))
        else:
            results.append(GateResult(candidate, Verdict.MISSED_TARGET, _missed(target), killed))
    return results


def batch_failed(candidates: list[Candidate], reason: str) -> list[GateResult]:
    """Reject the whole batch when the verification run itself failed."""
    return [GateResult(candidate, Verdict.BATCH_FAILED, reason) for candidate in candidates]


# --------------------------------------------------------------------------
# Helpers
# --------------------------------------------------------------------------

# Maven's closing advice after a failure, which says nothing about the failure.
_BOILERPLATE = (
    "[Help 1]",
    "To see the full stack trace",
    "Re-run Maven using",
    "For more information about the errors",
    "After correcting the problems",
    "Please refer to",
)


def _failure_lines(output: str) -> str:
    """The `[ERROR]` lines of a build's output: compiler errors or test failures."""
    lines: list[str] = []
    for line in output.splitlines():
        if not line.startswith("[ERROR]"):
            continue
        text = line.removeprefix("[ERROR]").strip()
        if text and text not in lines and not any(b in text for b in _BOILERPLATE):
            lines.append(text)
    if not lines:
        lines = output.splitlines()[-DETAIL_LINES:]
    return "\n".join(lines[:DETAIL_LINES])


def _missed(target: Mutant) -> str:
    """What became of a target its candidate did not kill."""
    status = target.status.value
    if target.killing_tests:
        return f"target {status}, killed only by: {', '.join(target.killing_tests)}"
    if is_detected(target):
        return f"target ended {status} with no killing test listed, so no credit"
    return f"target ended {status}"


def _describe(mutant: Mutant) -> str:
    return f"{mutant.mutated_class} line {mutant.line}: {mutant.description}"
