"""Mutant model, mutation score, and selection of classes and targets.

Pure logic over data. Nothing here touches the filesystem or runs anything.
"""

from collections.abc import Collection
from dataclasses import dataclass, field
from enum import Enum


class Status(str, Enum):
    """PIT's outcome for one mutant. The full set from PIT 1.30.0."""

    KILLED = "KILLED"
    SURVIVED = "SURVIVED"
    NO_COVERAGE = "NO_COVERAGE"
    TIMED_OUT = "TIMED_OUT"
    MEMORY_ERROR = "MEMORY_ERROR"
    RUN_ERROR = "RUN_ERROR"
    NON_VIABLE = "NON_VIABLE"
    NOT_STARTED = "NOT_STARTED"
    STARTED = "STARTED"
    EQUIVALENT = "EQUIVALENT"


DETECTED = frozenset(
    {
        Status.KILLED,
        Status.TIMED_OUT,
        Status.MEMORY_ERROR,
        Status.RUN_ERROR,
        Status.NON_VIABLE,
        Status.EQUIVALENT,
    }
)
UNDETECTED = frozenset({Status.SURVIVED, Status.NO_COVERAGE})
UNFINISHED = frozenset({Status.NOT_STARTED, Status.STARTED})


@dataclass(frozen=True)
class Mutant:
    """One deliberate defect PIT introduced, and what the suite did about it.

    Identity is the defect alone. The outcome fields are excluded from
    comparison, so the same mutant matches across the baseline and
    verification runs.

    Killing tests are normalized by `pit.py` to `fully.qualified.TestClass#method`.
    The fields follow PIT's XML report, confirmed against a real one.
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

    Every score and every target selection goes through this function. The
    rule follows PIT's own split (decided 10/16/26): KILLED, TIMED_OUT,
    MEMORY_ERROR, RUN_ERROR, NON_VIABLE, and EQUIVALENT are detected; SURVIVED
    and NO_COVERAGE are not. NOT_STARTED and STARTED mean PIT never finished
    the mutant, so the run is incomplete and they raise.
    """
    if mutant.status in DETECTED:
        return True
    if mutant.status in UNDETECTED:
        return False
    raise ValueError(
        f"{mutant.status.value}: PIT did not finish this mutant; the run is incomplete"
    )


def mutation_score(mutants: list[Mutant]) -> float:
    """Detected mutants divided by total mutants. The headline metric."""
    if not mutants:
        raise ValueError("no mutants to score")
    return sum(is_detected(m) for m in mutants) / len(mutants)


def for_class(mutants: list[Mutant], class_name: str) -> list[Mutant]:
    """The mutants PIT made in one class, including its nested and anonymous classes."""
    return [m for m in mutants if outer_class(m) == class_name]


def outer_class(mutant: Mutant) -> str:
    """The top-level class a mutant belongs to: `pkg.Outer` for `pkg.Outer$1`."""
    return mutant.mutated_class.split("$", 1)[0]


def select_classes(baseline: list[Mutant], excluded: Collection[str]) -> list[str]:
    """Apply the selection rule to a baseline ledger.

    Every class with at least one undetected mutant, minus the classes recorded
    as excluded in `corpus/`. No other filtering.
    """
    excluded = set(excluded)
    return sorted({outer_class(m) for m in baseline if not is_detected(m)} - excluded)


def select_targets(mutants: list[Mutant], class_name: str) -> list[Mutant]:
    """Every undetected mutant in one class. No further filtering."""
    return [m for m in for_class(mutants, class_name) if not is_detected(m)]


def excluding_tests(mutants: list[Mutant], test_classes: set[str]) -> list[Mutant]:
    """Drop every kill made by the given test classes.

    Applied to the verification run with the rejected candidates' classes, so
    the after-score counts only existing tests and kept candidates. A mutant
    left with no killing test no longer counts as killed.
    """
    raise NotImplementedError
