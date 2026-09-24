"""Metrics and the per-mutant ledger."""

from dataclasses import dataclass, field
from pathlib import Path

from .gate import GateResult
from .mutants import Mutant


@dataclass
class RunMetrics:
    repo: str
    target: str
    score_before: float = 0.0
    score_after: float = 0.0
    mutants_total: int = 0
    mutants_surviving_before: int = 0
    tests_generated: int = 0
    tests_kept: int = 0
    ledger: list[tuple[Mutant, GateResult]] = field(default_factory=list)

    @property
    def acceptance_rate(self) -> float:
        raise NotImplementedError

    @property
    def attempts_per_kept_test(self) -> float:
        """Generations divided by tests kept. The efficiency measure, and the
        one that stays meaningful across providers."""
        raise NotImplementedError


def write_results(metrics: RunMetrics, out_dir: Path) -> Path:
    """Write the run to results/ as JSON. These files are the deliverable."""
    raise NotImplementedError
