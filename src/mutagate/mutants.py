"""Mutant model and target selection."""

from dataclasses import dataclass
from enum import Enum


class Status(str, Enum):
    KILLED = "KILLED"
    SURVIVED = "SURVIVED"
    NO_COVERAGE = "NO_COVERAGE"
    TIMED_OUT = "TIMED_OUT"
    NON_VIABLE = "NON_VIABLE"


@dataclass(frozen=True)
class Mutant:
    """One deliberate defect PIT introduced, and what the suite did about it."""

    source_file: str
    mutated_class: str
    mutated_method: str
    line: int
    mutator: str
    status: Status
    description: str

    @property
    def is_gap(self) -> bool:
        """True when the suite ran this line and failed to notice the defect."""
        return self.status is Status.SURVIVED


def mutation_score(mutants: list[Mutant]) -> float:
    """Killed / total, ignoring mutants nothing covered."""
    raise NotImplementedError


def select_targets(mutants: list[Mutant], limit: int | None = None) -> list[Mutant]:
    """Pick surviving mutants worth attacking.

    Drops categories that are near-certainly equivalent, deduplicates by line,
    and ranks the rest. Returns at most `limit`.
    """
    raise NotImplementedError
