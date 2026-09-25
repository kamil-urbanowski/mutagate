"""Mutant model, mutation score, and selection of classes and targets.

Pure logic over data. Nothing here touches the filesystem or runs anything.
"""

from dataclasses import dataclass, field
from enum import Enum


class Status(str, Enum):
    """PIT's outcome for one mutant. Confirm the full set against a real report."""

    KILLED = "KILLED"
    SURVIVED = "SURVIVED"
    NO_COVERAGE = "NO_COVERAGE"
    TIMED_OUT = "TIMED_OUT"
    MEMORY_ERROR = "MEMORY_ERROR"
    RUN_ERROR = "RUN_ERROR"
    NON_VIABLE = "NON_VIABLE"


@dataclass(frozen=True)
class Mutant:
    """One deliberate defect PIT introduced, and what the suite did about it.

    Identity is the defect alone. The outcome fields are excluded from
    comparison, so the same mutant matches across the baseline and
    verification runs.

    Killing tests are normalized by `pit.py` to `fully.qualified.TestClass#method`.
    """

    source_file: str
    mutated_class: str
    mutated_method: str
    method_description: str  # JVM descriptor; tells overloads apart
    line: int
    mutator: str
    indexes: tuple[int, ...]  # tells apart identical mutations on one line
    description: str
    status: Status = field(compare=False)
    killing_tests: tuple[str, ...] = field(default=(), compare=False)


def is_detected(mutant: Mutant) -> bool:
    """Whether the suite counts as having caught this mutant.

    KILLED is detected. Which other statuses count is not yet decided (see
    PROJECT_CONTEXT.md). Decide it here, once; every score and every target
    selection goes through this function.
    """
    raise NotImplementedError


def mutation_score(mutants: list[Mutant]) -> float:
    """Detected mutants divided by total mutants. The headline metric."""
    raise NotImplementedError


def for_class(mutants: list[Mutant], class_name: str) -> list[Mutant]:
    """The mutants PIT made in one class."""
    raise NotImplementedError


def select_classes(baseline: list[Mutant], excluded: set[str]) -> list[str]:
    """Apply the selection rule to a baseline ledger.

    Every class with at least one undetected mutant, minus the classes recorded
    as excluded in `corpus/`. No other filtering.
    """
    raise NotImplementedError


def select_targets(mutants: list[Mutant], class_name: str) -> list[Mutant]:
    """Every undetected mutant in one class. No further filtering."""
    raise NotImplementedError


def excluding_tests(mutants: list[Mutant], test_classes: set[str]) -> list[Mutant]:
    """Drop every kill made by the given test classes.

    Applied to the verification run with the rejected candidates' classes, so
    the after-score counts only existing tests and kept candidates. A mutant
    left with no killing test no longer counts as killed.
    """
    raise NotImplementedError
