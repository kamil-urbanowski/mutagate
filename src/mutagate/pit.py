"""Everything touching the target repository and its build.

The only module that touches the target repository or runs external processes.
All work happens on a temporary copy; the original is never written to.

PIT runs through Maven with `-D` properties. The copy's build configuration is
left unchanged with one exception: PIT cannot run JUnit 5 tests without its
JUnit 5 plugin, so the pinned `pitest-maven` (1.30.0) is added to the copy's
`pom.xml`, with `pitest-junit5-plugin` (1.2.3) as its dependency.

    mvn -B test-compile org.pitest:pitest-maven:mutationCoverage \
        -DtargetClasses=... -DoutputFormats=XML -DfullMutationMatrix=true \
        -DtimestampedReports=false

`fullMutationMatrix` makes the report list every test that killed each mutant,
not just the first. Batched verification depends on it.

A class-scoped run sets `targetClasses=pkg.Outer,pkg.Outer$*` so nested and
anonymous classes are included. A bare `pkg.Outer*` would also match other
classes that share the prefix.
"""

from pathlib import Path

from .gate import Candidate
from .mutants import Mutant


class PitError(Exception):
    """A PIT run failed outright. Carries the build output."""


# --------------------------------------------------------------------------
# The temporary copy
# --------------------------------------------------------------------------


def make_workspace(repo: Path) -> Path:
    """Copy `repo` to a fresh temporary directory and return the copy's root."""
    raise NotImplementedError


def read_sources(workspace: Path) -> dict[Path, str]:
    """Every Java file in the copy, main and test, keyed by path relative to the root."""
    raise NotImplementedError


def add_candidate(workspace: Path, candidate: Candidate) -> None:
    """Write a candidate's test file into the copy."""
    raise NotImplementedError


def remove_candidate(workspace: Path, candidate: Candidate) -> None:
    """Delete a candidate's test file from the copy."""
    raise NotImplementedError


# --------------------------------------------------------------------------
# Plain builds, for the screen
# --------------------------------------------------------------------------


def compile_tests(workspace: Path) -> tuple[bool, str]:
    """Compile main and test sources. Returns success and the build output."""
    raise NotImplementedError


def run_test(workspace: Path, test_class: str, times: int = 1) -> list[tuple[bool, str]]:
    """Run one test class against unmodified code `times` times.

    Returns success and the build output for each run.
    """
    raise NotImplementedError


# --------------------------------------------------------------------------
# PIT
# --------------------------------------------------------------------------


def run_pit(workspace: Path, target_class: str | None, excluded_tests: set[str]) -> Path:
    """Run PIT on the copy and return the directory holding its report.

    `target_class` None mutates the whole module (the baseline run); a class
    name scopes the run to that class (verification). `excluded_tests` are the
    repo's failing tests recorded in `corpus/`, left out of every run.
    Raises PitError when the run fails.
    """
    raise NotImplementedError


def parse_report(report_dir: Path) -> list[Mutant]:
    """Read PIT's XML report into Mutant records, including their killing tests."""
    raise NotImplementedError
