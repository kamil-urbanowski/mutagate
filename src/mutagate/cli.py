"""Commands and orchestration; the logic lives in the modules. Design: `docs/design/cli.md`."""

import json
from pathlib import Path

import click

from . import context, gate, generate, mutants, pit, report
from .context import MutantContext
from .gate import Candidate, GateResult
from .mutants import Mutant
from .providers.base import REPLIES_DIR, Provider, ProviderError, SavedReplies, load

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
@click.option(
    "--provider",
    type=click.Choice(["claude-code", "offline"]),
    default="claude-code",
    show_default=True,
)
def run(repo: Path, target: str, control: bool, provider: str) -> None:
    """Generate tests for one class's undetected mutants and keep the ones that
    pass the gate."""
    if control:
        raise click.ClickException("control runs are not built yet (build order step 5)")
    replies = SavedReplies(load(provider), REPLIES_DIR / repo.resolve().name)
    try:
        metrics = run_targeted(repo, target, replies)
    except FileNotFoundError as e:
        raise click.ClickException(f"{e}: record the repo in corpus/ before any run") from e
    except pit.PitError as e:
        raise click.ClickException(f"{e}\n{e.output[-5000:]}") from e
    except ProviderError as e:
        raise click.ClickException(
            f"{e}\nThe run stopped. Replies received so far are saved: running the same "
            "command again sends only the rest."
        ) from e
    path = report.write_run(metrics)

    click.echo(f"Saved {path}")
    counts = report.verdict_counts([metrics])
    click.echo(
        f"Kept {metrics.kept} of {len(metrics.ledger)} candidates "
        f"({report.kept_after_repair([metrics])} after a repair)"
    )
    for verdict, count in sorted(counts.items()):
        click.echo(f"  {verdict.value}: {count}")
    total, undetected = metrics.mutants_total, metrics.undetected_before
    click.echo(
        f"Class mutation score: {total - undetected}/{total} = {metrics.score_before:.1%} -> "
        f"{total - undetected + metrics.newly_detected}/{total} = {metrics.score_after:.1%}"
    )
    reused = sum(q.reused for q in metrics.requests)
    click.echo(f"Requests: {len(metrics.requests) - reused} sent, {reused} reused")
    if metrics.tokens:
        click.echo("Tokens: " + ", ".join(f"{k} {v:,}" for k, v in metrics.tokens.items()))


@main.command()
@click.argument("repo_name")
def summarize(repo_name: str) -> None:
    """Compute the whole-module headline scores from every saved run."""
    baseline = report.read_baseline(repo_name)
    selected = mutants.select_classes(baseline, report.read_exclusions(repo_name))
    runs = report.read_runs(repo_name)
    if not runs:
        raise click.ClickException(f"no saved runs for {repo_name}")
    path = report.write_summary(repo_name, baseline, runs, selected)
    summary = json.loads(path.read_text(encoding="utf-8"))

    click.echo(f"Saved {path}")
    total, before = summary["mutants_total"], summary["detected_before"]
    for kind in report.RunKind:
        s = summary.get(kind.value.lower())
        if s is None:
            continue
        run_count = len(s["classes"])
        click.echo(
            f"{kind.value.capitalize()}: {run_count} of {len(selected)} selected classes run"
        )
        click.echo(
            f"  Module mutation score: {before}/{total} = {s['score_before']:.1%} -> "
            f"{s['detected_after']}/{total} = {s['score_after']:.1%}"
        )
        per_kept = s["attempts_per_kept_test"]
        click.echo(
            f"  Kept {s['kept']} of {s['candidates']} candidates; attempts per kept test: "
            f"{f'{per_kept:.2f}' if per_kept else 'none kept'}; "
            f"kept after a repair: {s['kept_after_repair']}"
        )
        for c in s["classes"]:
            click.echo(
                f"  {c['class']}: kept {c['kept']} of {c['candidates']}, "
                f"{c['score_before']:.1%} -> {c['score_after']:.1%}"
            )
        if len(s["settings"]) > 1:
            click.echo(f"  Warning: runs used different settings: {s['settings']}")


def run_targeted(
    repo: Path, class_name: str, provider: SavedReplies, corpus: Path = report.CORPUS_DIR
) -> report.RunMetrics:
    """One targeted run of one class: steps 2-6 of the flow in ARCHITECTURE.md.

    Every request goes through `provider`; its exchanges become the run's
    requests. Nothing is written to `results/`: the caller saves the run.
    """
    name = repo.resolve().name
    baseline = report.read_baseline(name, corpus)
    excluded = report.read_exclusions(name, corpus)
    failing = report.read_failing_tests(name, corpus)
    if class_name not in mutants.select_classes(baseline, excluded):
        raise click.ClickException(f"{class_name} is not one of {name}'s selected classes")
    targets = mutants.select_targets(baseline, class_name)
    setup = {
        **pit.describe_setup(repo),
        **pit.describe_tool(),
        "provider": provider.name,
        "provider_version": provider.version,
        **provider.settings,
    }
    if setup["mutagate_commit"].endswith("-dirty"):
        click.echo(
            "Warning: MutaGate's code has uncommitted changes; this run has no exact commit."
        )
    click.echo(
        f"{class_name}: {len(targets)} targets, provider {provider.name} {provider.settings}"
    )

    workspace = pit.make_workspace(repo)
    try:
        sources = pit.read_sources(workspace)  # before any candidate is added
        contexts = [context.build_context(sources, class_name, t) for t in targets]
        names = [generate.test_class_name(class_name, i) for i in range(1, len(targets) + 1)]
        for ctx, test_class in zip(contexts, names):
            path = ctx.test_dir / f"{test_class.rpartition('.')[2]}.java"
            if path in sources:
                raise click.ClickException(
                    f"{path} exists in the repo; MutaGate never replaces a file"
                )
        # Compile before any candidate is added, so a later build failure is the
        # candidate's own.
        ok, output = pit.compile_tests(workspace)
        if not ok:
            raise pit.PitError("the copy does not compile before any candidate is added", output)

        screened: list[tuple[Candidate, GateResult | None]] = []
        requests: list[report.Request] = []
        for i, (ctx, test_class) in enumerate(zip(contexts, names), start=1):
            sent = len(provider.exchanges)
            first = generate.generate_test(ctx, provider, test_class)
            candidate, result, failure = screen_with_repair(workspace, first, ctx, provider)
            screens = [failure, result] if failure else [result]
            for attempt, (exchange, screen) in enumerate(
                zip(provider.exchanges[sent:], screens, strict=True), start=1
            ):
                requests.append(
                    report.Request(
                        test_class,
                        attempt,
                        exchange.reply,
                        exchange.tokens,
                        exchange.reused,
                        screen.verdict if screen else None,
                        screen.detail if screen else "",
                    )
                )
            screened.append((candidate, result))
            outcome = result.verdict.value if result else "passed the screen"
            repaired = ", after a repair" if candidate.attempts == 2 else ""
            click.echo(f"  [{i}/{len(targets)}] line {ctx.mutant.line}: {outcome}{repaired}")

        batch = [c for c, r in screened if r is None]
        verification: list[Mutant] = []
        verified: dict[str, GateResult] = {}
        if batch:
            click.echo(f"  Verifying {len(batch)} candidates in one PIT run")
            try:
                verification = pit.parse_report(pit.run_pit(workspace, class_name, failing))
                results = gate.verify(batch, verification, targets)
            except pit.PitError as e:
                verification = []
                tail = "\n".join(e.output.splitlines()[-gate.DETAIL_LINES :])
                results = gate.batch_failed(batch, f"{e}\n{tail}")
            verified = {r.candidate.test_class: r for r in results}
    finally:
        pit.remove_workspace(workspace)

    ledger = [r if r is not None else verified[c.test_class] for c, r in screened]
    kept = [r.candidate.test_class for r in ledger if r.kept]
    return report.RunMetrics(
        repo=name,
        target_class=class_name,
        kind=report.RunKind.TARGETED,
        setup=setup,
        mutants_total=len(mutants.for_class(baseline, class_name)),
        undetected_before=len(targets),
        newly_detected=len(mutants.newly_detected(baseline, verification, kept)),
        ledger=ledger,
        requests=requests,
    )


def screen_with_repair(
    workspace: Path, candidate: Candidate, context: MutantContext, provider: Provider
) -> tuple[Candidate, GateResult | None, GateResult | None]:
    """Screen one candidate; on a failure, one repair and one re-screen.

    Returns the final candidate; None if it passed (its file stays in the copy
    for the batch) or the rejecting GateResult (its file is removed); and the
    first attempt's rejection when there was a repair, else None.
    """
    rejected = _screen(workspace, candidate)
    if rejected is None:
        return candidate, None, None
    repaired = generate.repair(context, provider, candidate, rejected)
    return repaired, _screen(workspace, repaired), rejected


def _screen(workspace: Path, candidate: Candidate) -> GateResult | None:
    """The screen's checks in order, stopping at the first rejection. A
    rejected candidate's file is removed from the copy."""
    rejected = gate.check_usable(candidate)
    if rejected is not None:
        return rejected
    pit.add_candidate(workspace, candidate)
    rejected = gate.check_compiled(candidate, pit.compile_tests(workspace))
    if rejected is None:
        runs = pit.run_test(workspace, candidate.test_class, gate.SCREEN_RUNS)
        rejected = gate.check_runs(candidate, runs)
    if rejected is not None:
        pit.remove_candidate(workspace, candidate)
    return rejected


if __name__ == "__main__":
    main()
