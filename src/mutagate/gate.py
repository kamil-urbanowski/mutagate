"""The gate. A generated test is kept only if a program says it earned its place.

Build and verify this module before wiring in any model. Feed it a test you
wrote by hand and confirm it accepts and rejects correctly.
"""

from dataclasses import dataclass
from enum import Enum
from pathlib import Path

from .mutants import Mutant


class Verdict(str, Enum):
    KEPT = "KEPT"
    DID_NOT_COMPILE = "DID_NOT_COMPILE"
    FAILED_ON_CLEAN_CODE = "FAILED_ON_CLEAN_CODE"
    DID_NOT_KILL = "DID_NOT_KILL"
    BROKE_EXISTING_TESTS = "BROKE_EXISTING_TESTS"
    NONDETERMINISTIC = "NONDETERMINISTIC"


@dataclass
class GateResult:
    verdict: Verdict
    detail: str = ""

    @property
    def kept(self) -> bool:
        return self.verdict is Verdict.KEPT


def compiles(repo: Path, test_source: str, test_path: Path) -> GateResult | None:
    """None when it compiles; a failing GateResult otherwise."""
    raise NotImplementedError


def passes_on_clean_code(repo: Path, test_path: Path) -> GateResult | None:
    """A test that fails against unmodified code is simply wrong."""
    raise NotImplementedError


def kills_target(repo: Path, test_path: Path, mutant: Mutant) -> GateResult | None:
    """Re-run PIT scoped to this mutant and confirm it is now killed."""
    raise NotImplementedError


def is_deterministic(repo: Path, test_path: Path, runs: int = 5) -> GateResult | None:
    """Reject anything touching clocks, randomness, or the network."""
    raise NotImplementedError


def evaluate(repo: Path, test_source: str, test_path: Path, mutant: Mutant) -> GateResult:
    """Run every check in order. First failure wins."""
    raise NotImplementedError
