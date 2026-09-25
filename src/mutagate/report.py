"""Metrics, the run ledger, and every file in `results/` and `corpus/`.

The only module that reads or writes `results/` and `corpus/`. Each repository
is keyed by its directory name.

    corpus/<repo>/   baseline ledger, class exclusions, excluded failing tests
    results/<repo>/  one file per run, the kept tests, and the headline summary
"""

from dataclasses import dataclass, field
from enum import Enum
from pathlib import Path

from .gate import GateResult, Verdict
from .mutants import Mutant

CORPUS_DIR = Path("corpus")
RESULTS_DIR = Path("results")


class RunKind(str, Enum):
    TARGETED = "TARGETED"
    CONTROL = "CONTROL"


@dataclass
class RunMetrics:
    """One run's outcome, for one class."""

    repo: str
    target_class: str
    kind: RunKind
    score_before: float = 0.0
    score_after: float = 0.0
    mutants_total: int = 0
    undetected_before: int = 0
    newly_detected: int = 0  # undetected mutants that kept tests killed
    ledger: list[GateResult] = field(default_factory=list)  # one entry per candidate

    @property
    def acceptance_rate(self) -> float:
        """Kept candidates divided by all candidates."""
        raise NotImplementedError

    @property
    def attempts_per_kept_test(self) -> float:
        """Generations divided by tests kept. The efficiency measure, and the
        one that stays meaningful across providers."""
        raise NotImplementedError


# --------------------------------------------------------------------------
# Derived metrics. No extra runs.
# --------------------------------------------------------------------------


def headline_scores(baseline: list[Mutant], runs: list[RunMetrics]) -> tuple[float, float]:
    """Whole-module score before and after, for runs of one kind.

    "Before" is the baseline score. "After" adds every mutant that kept tests
    newly killed across the runs. Excluded classes still count toward the total.
    """
    raise NotImplementedError


def verdict_counts(runs: list[RunMetrics]) -> dict[Verdict, int]:
    """How many candidates got each verdict. Shows what the gate filtered out."""
    raise NotImplementedError


def kept_after_repair(runs: list[RunMetrics]) -> int:
    """Kept tests that needed the repair attempt. Shows what the repair adds."""
    raise NotImplementedError


# --------------------------------------------------------------------------
# corpus/
# --------------------------------------------------------------------------


def write_baseline(repo: str, mutants: list[Mutant], root: Path = CORPUS_DIR) -> Path:
    """Save a repository's baseline ledger."""
    raise NotImplementedError


def read_baseline(repo: str, root: Path = CORPUS_DIR) -> list[Mutant]:
    """Load a repository's baseline ledger."""
    raise NotImplementedError


def read_exclusions(repo: str, root: Path = CORPUS_DIR) -> dict[str, str]:
    """Classes excluded by the selection rule, each with its one-line reason."""
    raise NotImplementedError


def read_failing_tests(repo: str, root: Path = CORPUS_DIR) -> set[str]:
    """Existing test classes that fail on unmodified code, left out of every PIT run."""
    raise NotImplementedError


# --------------------------------------------------------------------------
# results/
# --------------------------------------------------------------------------


def write_run(metrics: RunMetrics, root: Path = RESULTS_DIR) -> Path:
    """Save one run: its metrics, its run ledger, and the source of every kept test."""
    raise NotImplementedError


def read_runs(repo: str, root: Path = RESULTS_DIR) -> list[RunMetrics]:
    """Load every saved run for a repository."""
    raise NotImplementedError


def write_summary(
    repo: str, baseline: list[Mutant], runs: list[RunMetrics], root: Path = RESULTS_DIR
) -> Path:
    """Save the repository's headline scores for each run kind, and the derived metrics."""
    raise NotImplementedError
