"""MutaGate command line. Orchestration only; the logic lives in the modules."""

from pathlib import Path

import click

from . import mutants, pit, report
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
    name = repo.resolve().name
    excluded = report.read_exclusions(name)
    failing = report.read_failing_tests(name)
    try:
        setup = pit.describe_setup(repo)
        workspace = pit.make_workspace(repo)
        try:
            ledger = pit.parse_report(pit.run_pit(workspace, None, failing))
        finally:
            pit.remove_workspace(workspace)
    except pit.PitError as e:
        raise click.ClickException(f"{e}\n{e.output[-5000:]}") from e
    path = report.write_baseline(name, ledger, setup)

    detected = sum(mutants.is_detected(m) for m in ledger)
    click.echo(f"Saved {path}")
    click.echo(
        f"Measured on Java {setup['java']}, Maven {setup['maven']}, "
        f"pitest-maven {setup['pitest_maven']}, commit {setup['repo_commit'][:7]}"
    )
    click.echo(
        f"Baseline mutation score: {detected}/{len(ledger)} = {mutants.mutation_score(ledger):.1%}"
    )
    for status in mutants.Status:
        count = sum(m.status is status for m in ledger)
        if count:
            click.echo(f"  {status.value}: {count}")

    selected = mutants.select_classes(ledger, excluded)
    targets = {c: len(mutants.select_targets(ledger, c)) for c in selected}
    click.echo(f"Selected classes: {len(selected)}, targets: {sum(targets.values())}")
    for class_name, count in targets.items():
        click.echo(f"  {class_name}: {count}")


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
