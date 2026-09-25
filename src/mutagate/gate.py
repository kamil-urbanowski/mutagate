"""The gate. A generated test is kept only if a program says it earned its place.

Pure judgment: the gate runs nothing and holds no provider. `cli.py` runs the
build and PIT through `pit.py` and hands the outcomes here for a verdict.

Two stages, for cost reasons:

1. **Screen** — per candidate, no PIT. The reply must hold a usable test, which
   must compile, pass against unmodified code, and pass deterministically. This
   stage protects the batch: one test that fails to compile breaks compilation
   for every other candidate, and a test that fails on unmodified code is simply
   wrong. A screen failure gets exactly one repair attempt.

2. **Verify** — per class, one scoped PIT run for the whole batch. PIT records
   which test killed each mutant, so per-test attribution survives batching. A
   targeted candidate is kept only if it killed the specific mutant it was
   written for; killing some other mutant does not count. A control candidate
   is kept if it killed at least one mutant the existing suite did not detect.
   A verification failure is final. If the run itself fails, the whole batch is
   rejected.

Build and verify this module before wiring in any model. Feed it tests you
wrote by hand and confirm it accepts and rejects correctly.
"""

from dataclasses import dataclass
from enum import Enum
from pathlib import Path

from .mutants import Mutant

# How many times the screen runs a candidate against unmodified code.
SCREEN_RUNS = 5


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
    """Reject a reply that held no usable test."""
    raise NotImplementedError


def check_compiled(candidate: Candidate, build: tuple[bool, str]) -> GateResult | None:
    """Judge the build that included the candidate. `build` is success and output."""
    raise NotImplementedError


def check_runs(candidate: Candidate, runs: list[tuple[bool, str]]) -> GateResult | None:
    """Judge `SCREEN_RUNS` runs against unmodified code.

    Every run passes: None. Every run fails: FAILED_ON_UNMODIFIED_CODE.
    Anything in between: NONDETERMINISTIC.
    """
    raise NotImplementedError


# --------------------------------------------------------------------------
# Stage 2 — batched verification. One scoped PIT run per class.
# --------------------------------------------------------------------------


def verify(
    candidates: list[Candidate], mutants: list[Mutant], undetected_before: list[Mutant]
) -> list[GateResult]:
    """Judge every screened candidate from one verification run's mutants.

    `undetected_before` is the class's targets, taken from the baseline; a
    control candidate is kept if it killed any of them.
    """
    raise NotImplementedError


def batch_failed(candidates: list[Candidate], reason: str) -> list[GateResult]:
    """Reject the whole batch when the verification run itself failed."""
    raise NotImplementedError
