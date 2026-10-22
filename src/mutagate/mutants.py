"""The Mutant model, scoring, and class and target selection. Design: `docs/design/mutants.md`.

Pure logic over data: nothing here touches the filesystem or runs anything.
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

    Killing tests are normalized by `pit.py` to `fully.qualified.TestClass#method`
    (`Outer$Inner#method` for a nested test class), or to the class name alone
    when PIT credits the kill to the whole class.
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
    split follows PIT's own, so scores match the ones PIT prints.
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
    """The selection rule: every class with an undetected mutant, minus those
    recorded as excluded in `corpus/`. No other filtering."""
    excluded = set(excluded)
    return sorted({outer_class(m) for m in baseline if not is_detected(m)} - excluded)


def select_targets(mutants: list[Mutant], class_name: str) -> list[Mutant]:
    """Every undetected mutant in one class. No further filtering."""
    return [m for m in for_class(mutants, class_name) if not is_detected(m)]


def killed_by(mutant: Mutant, test_class: str) -> bool:
    """Whether a test class is among the mutant's killing tests.

    Counts a kill by one of its methods (`Class#method`), a method of a nested
    class (`Class$Inner#method`), or the class as a whole (`Class`), but never
    one by a class whose name only starts the same: `ATest` is not `ATest2`.
    """
    return any(
        test == test_class or test.startswith((f"{test_class}#", f"{test_class}$"))
        for test in mutant.killing_tests
    )


def newly_detected(
    baseline: list[Mutant], verification: list[Mutant], kept_classes: Collection[str]
) -> list[Mutant]:
    """Mutants the baseline did not detect that a kept test killed in verification.

    The after-score counts the baseline's detected mutants plus these. Kills by
    rejected candidates never count.
    """
    undetected = {m for m in baseline if not is_detected(m)}
    return [
        m
        for m in verification
        if m in undetected and any(killed_by(m, test_class) for test_class in kept_classes)
    ]
