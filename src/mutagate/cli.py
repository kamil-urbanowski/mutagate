"""MutaGate command line."""

from pathlib import Path

import click


@click.group()
@click.version_option()
def main() -> None:
    """Mutation-gated test generation for Java test suites."""


@main.command()
@click.option("--repo", type=click.Path(exists=True, path_type=Path), required=True)
@click.option("--target", required=True, help="Class to analyze, e.g. com.example.Pricing")
def baseline(repo: Path, target: str) -> None:
    """Run PIT and report the current mutation score and surviving mutants."""
    raise click.ClickException("not implemented")


@main.command()
@click.option("--repo", type=click.Path(exists=True, path_type=Path), required=True)
@click.option("--target", required=True, help="Class to analyze, e.g. com.example.Pricing")
@click.option("--limit", type=int, default=None, help="Max mutants to attempt")
@click.option("--provider", default="offline")
def run(repo: Path, target: str, limit: int | None, provider: str) -> None:
    """Generate tests for surviving mutants and keep the ones that pass the gate."""
    raise click.ClickException("not implemented")


if __name__ == "__main__":
    main()
