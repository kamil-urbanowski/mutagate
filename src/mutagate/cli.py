"""MutaGate command line. Orchestration only; the logic lives in the modules."""

from pathlib import Path

import click

from .context import MutantContext
from .gate import Candidate, GateResult
from .providers.base import Provider

RepoPath = click.Path(exists=True, file_okay=False, path_type=Path)


@click.group()
@click.version_option()
def main() -> None:
    """Mutation-gated test generation for Java test suites."""


@main.command()
@click.option("--repo", type=RepoPath, required=True)
def baseline(repo: Path) -> None:
    """Run PIT once across the whole module and save the baseline ledger.

    Prints the baseline score and the classes the selection rule picks.
    """
    raise click.ClickException("not implemented")


@main.command()
@click.option("--repo", type=RepoPath, required=True)
@click.option("--target", required=True, help="Class to work on, e.g. com.example.Pricing")
@click.option("--control", is_flag=True, help="Untargeted control run")
@click.option("--provider", default="offline")
def run(repo: Path, target: str, control: bool, provider: str) -> None:
    """Generate tests for one class's undetected mutants and keep the ones that
    pass the gate."""
    raise click.ClickException("not implemented")


@main.command()
@click.argument("repo_name")
def summarize(repo_name: str) -> None:
    """Compute the whole-module headline scores from every saved run."""
    raise click.ClickException("not implemented")


def screen_with_repair(
    workspace: Path, candidate: Candidate, context: MutantContext, provider: Provider
) -> tuple[Candidate, GateResult | None]:
    """Screen one candidate; on a failure, one repair and one re-screen.

    Returns the final candidate, and None if it passed (its file stays in the
    copy for the batch) or the rejecting GateResult (its file is removed).
    """
    raise NotImplementedError


if __name__ == "__main__":
    main()
