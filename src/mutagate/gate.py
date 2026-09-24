"""The gate. A generated test is kept only if a program says it earned its place.

Two stages, for cost reasons:

1. **Screen** — per candidate, no PIT. Must compile, pass against unmodified
   code, and be deterministic. This stage exists to protect the batch: one test
   that fails to compile breaks `test-compile` for every other candidate, and a
   test that fails on clean code is simply wrong.

2. **Verify** — per class, one scoped PIT run for the whole batch. PIT's report
   records which test killed each mutant, so per-test attribution survives
   batching. A candidate is kept if it killed at least one mutant that survived
   in the baseline — its own target or another. Anything that killed nothing is
   removed.

The final mutation score comes from that same report. Removing tests that
killed nothing cannot change the kill counts, so no second run is needed.

Build and verify this module before wiring in any model. Feed it tests you
wrote by hand and confirm it accepts and rejects correctly.
"""

from dataclasses import dataclass
from enum import Enum
from pathlib import Path

from .mutants import Mutant


class Verdict(str, Enum):
    # Kept
    KEPT = "KEPT"

    # Rejected during the per-candidate screen
    DID_NOT_COMPILE = "DID_NOT_COMPILE"
    FAILED_ON_CLEAN_CODE = "FAILED_ON_CLEAN_CODE"
    NONDETERMINISTIC = "NONDETERMINISTIC"
    BROKE_EXISTING_TESTS = "BROKE_EXISTING_TESTS"

    # Rejected during batch verification
    KILLED_NOTHING = "KILLED_NOTHING"

    # Gave up before a verdict was reached
    ABANDONED = "ABANDONED"


@dataclass
class Candidate:
    """One generated test, and the mutant it was written to catch."""

    target: Mutant
    test_source: str
    test_path: Path
    test_method: str
    attempts: int = 1


@dataclass
class GateResult:
    candidate: Candidate
    verdict: Verdict
    detail: str = ""
    mutants_killed: tuple[Mutant, ...] = ()

    @property
    def kept(self) -> bool:
        return self.verdict is Verdict.KEPT

    @property
    def killed_its_target(self) -> bool:
        """Whether it caught the defect it was aimed at, as opposed to another."""
        return self.candidate.target in self.mutants_killed


# --------------------------------------------------------------------------
# Stage 1 — per-candidate screen. No PIT.
# --------------------------------------------------------------------------


def compiles(repo: Path, candidate: Candidate) -> GateResult | None:
    """None when it compiles; a failing GateResult otherwise."""
    raise NotImplementedError


def passes_on_clean_code(repo: Path, candidate: Candidate) -> GateResult | None:
    """A test that fails against unmodified code is simply wrong."""
    raise NotImplementedError


def is_deterministic(repo: Path, candidate: Candidate, runs: int = 5) -> GateResult | None:
    """Reject anything touching clocks, randomness, or the network.

    Static screen first, then repeated execution. Any variance is a rejection.
    """
    raise NotImplementedError


def screen(repo: Path, candidate: Candidate) -> GateResult | None:
    """Run the per-candidate checks in order. First failure wins.

    Returns None when the candidate is clean enough to enter the batch.
    """
    raise NotImplementedError


# --------------------------------------------------------------------------
# Stage 2 — batched verification. One scoped PIT run per class.
# --------------------------------------------------------------------------


def verify_batch(
    repo: Path,
    target_class: str,
    candidates: list[Candidate],
    baseline_survivors: list[Mutant],
) -> list[GateResult]:
    """Add every screened candidate, run PIT once for the class, judge them all.

    Reads killing-test attribution from the report. A candidate that killed at
    least one mutant from `baseline_survivors` is KEPT; the rest are
    KILLED_NOTHING and their test methods are removed from the repo.
    """
    raise NotImplementedError


def remove(repo: Path, candidates: list[Candidate]) -> None:
    """Strip rejected test methods back out of the target repo's sources."""
    raise NotImplementedError


# --------------------------------------------------------------------------
# Orchestration
# --------------------------------------------------------------------------


def run_gate(
    repo: Path,
    target_class: str,
    candidates: list[Candidate],
    baseline_survivors: list[Mutant],
) -> list[GateResult]:
    """Screen every candidate, then verify the survivors as one batch.

    Returns a result for every candidate passed in, including those rejected
    during the screen, so the caller's ledger stays complete.
    """
    raise NotImplementedError
