"""Metrics, the run ledger, and every file in `results/` and `corpus/`.

The only module that reads or writes `results/` and `corpus/`. Each repository
is keyed by its directory name.

    corpus/<repo>/   baseline.json, exclusions.json, failing-tests.json
    results/<repo>/  one file per run, the kept tests, and the headline summary
"""

import json
from dataclasses import asdict, dataclass, field
from enum import Enum
from pathlib import Path

from .gate import GateResult, Verdict
from .mutants import Mutant, Status

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


def write_baseline(
    repo: str, mutants: list[Mutant], setup: dict[str, str], root: Path = CORPUS_DIR
) -> Path:
    """Save a repository's baseline ledger, with the setup it was measured on."""
    path = root / repo / "baseline.json"
    path.parent.mkdir(parents=True, exist_ok=True)
    ledger = {**setup, "mutants": [asdict(m) for m in mutants]}
    path.write_text(json.dumps(ledger, indent=2) + "\n", encoding="utf-8")
    return path


def read_baseline(repo: str, root: Path = CORPUS_DIR) -> list[Mutant]:
    """Load a repository's baseline ledger."""
    ledger = _read_json(root / repo / "baseline.json")
    return [
        Mutant(
            **{
                **m,
                "indexes": tuple(m["indexes"]),
                "status": Status(m["status"]),
                "killing_tests": tuple(m["killing_tests"]),
            }
        )
        for m in ledger["mutants"]
    ]


def read_exclusions(repo: str, root: Path = CORPUS_DIR) -> dict[str, str]:
    """Classes excluded by the selection rule, each with its one-line reason."""
    return dict(_read_json(root / repo / "exclusions.json"))


def read_failing_tests(repo: str, root: Path = CORPUS_DIR) -> set[str]:
    """Existing test classes that fail on unmodified code, left out of every PIT run."""
    return set(_read_json(root / repo / "failing-tests.json"))


def _read_json(path: Path):
    return json.loads(path.read_text(encoding="utf-8"))


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
