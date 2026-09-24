"""Invoke PIT and parse its report.

PIT runs via Maven without any change to the target repository:

    mvn org.pitest:pitest-maven:mutationCoverage \
        -DtargetClasses=... -DtargetTests=... -DoutputFormats=XML
"""

from pathlib import Path

from .mutants import Mutant


def run_pit(
    repo: Path,
    target_classes: str,
    target_tests: str | None = None,
    report_dir: Path | None = None,
) -> Path:
    """Run PIT against `repo` and return the directory holding its report.

    Nothing in the repository is modified.
    """
    raise NotImplementedError


def parse_report(report_dir: Path) -> list[Mutant]:
    """Read PIT's XML report into Mutant records."""
    raise NotImplementedError


def kills(report_dir: Path, mutant: Mutant) -> bool:
    """True if `mutant` is killed in this report."""
    raise NotImplementedError
