"""Metrics and the run ledger. Design: `docs/design/report.md`.

The only module that reads or writes `results/` and `corpus/`.
"""

import json
from collections import Counter
from collections.abc import Collection
from dataclasses import asdict, dataclass, field
from enum import Enum
from pathlib import Path

from .gate import Candidate, GateResult, Verdict
from .mutants import Mutant, Status, is_detected

CORPUS_DIR = Path("corpus")
RESULTS_DIR = Path("results")
KEPT_DIR = "kept"

# The setup keys a run's replies depend on. Runs summarized together should
# share them; the summary lists every combination it found.
REPLY_SETTINGS = ("provider", "model", "effort", "system_prompt")


class RunKind(str, Enum):
    TARGETED = "TARGETED"
    CONTROL = "CONTROL"


@dataclass
class Request:
    """One request to the model in a run, and how the screen judged the
    candidate made from its reply."""

    test_class: str
    attempt: int  # 1, or 2 for the repair
    reply: str
    tokens: dict[str, int] | None  # as the provider reported them; None if it reports none
    reused: bool  # a saved reply from an earlier, stopped run; not sent again
    screen: Verdict | None = None  # the screen's rejection; None if it passed
    detail: str = ""


@dataclass
class RunMetrics:
    """One run's outcome, for one class."""

    repo: str
    target_class: str
    kind: RunKind
    setup: dict[str, str] = field(default_factory=dict)  # versions, commits, provider settings
    mutants_total: int = 0  # the class's
    undetected_before: int = 0
    newly_detected: int = 0  # undetected mutants that kept tests killed
    ledger: list[GateResult] = field(default_factory=list)  # one entry per candidate
    requests: list[Request] = field(default_factory=list)  # every request, in order

    @property
    def score_before(self) -> float:
        """The class's mutation score from the baseline."""
        return (self.mutants_total - self.undetected_before) / self.mutants_total

    @property
    def score_after(self) -> float:
        """The class's score counting the mutants kept tests newly killed."""
        return (
            self.mutants_total - self.undetected_before + self.newly_detected
        ) / self.mutants_total

    @property
    def kept(self) -> int:
        return sum(r.kept for r in self.ledger)

    @property
    def acceptance_rate(self) -> float:
        """Kept candidates divided by all candidates."""
        if not self.ledger:
            raise ValueError("no candidates")
        return self.kept / len(self.ledger)

    @property
    def attempts_per_kept_test(self) -> float | None:
        """Generations divided by tests kept. The efficiency measure, and the
        one that stays meaningful across providers. None when nothing was kept."""
        if not self.kept:
            return None
        return sum(r.candidate.attempts for r in self.ledger) / self.kept

    @property
    def tokens(self) -> dict[str, int]:
        """Every request's tokens, summed. Requests that report none add nothing."""
        total: Counter[str] = Counter()
        for request in self.requests:
            total.update(request.tokens or {})
        return dict(total)


# --------------------------------------------------------------------------
# Derived metrics. No extra runs.
# --------------------------------------------------------------------------


def headline_scores(baseline: list[Mutant], runs: list[RunMetrics]) -> tuple[float, float]:
    """Whole-module score before and after, for runs of one kind.

    "Before" is the baseline score. "After" adds every mutant that kept tests
    newly killed across the runs. Excluded classes still count toward the total.
    """
    if len({r.kind for r in runs}) > 1:
        raise ValueError("headline scores are for runs of one kind")
    classes = [r.target_class for r in runs]
    if len(set(classes)) != len(classes):
        raise ValueError("a class was run more than once")
    detected = sum(is_detected(m) for m in baseline)
    newly = sum(r.newly_detected for r in runs)
    return detected / len(baseline), (detected + newly) / len(baseline)


def verdict_counts(runs: list[RunMetrics]) -> dict[Verdict, int]:
    """How many candidates got each verdict. Shows what the gate filtered out."""
    return dict(Counter(r.verdict for run in runs for r in run.ledger))


def kept_after_repair(runs: list[RunMetrics]) -> int:
    """Kept tests that needed the repair attempt. Shows what the repair adds."""
    return sum(r.kept and r.candidate.attempts == 2 for run in runs for r in run.ledger)


def by_target_status(runs: list[RunMetrics]) -> dict[Status, tuple[int, int]]:
    """Targeted candidates and kept tests per target status: SURVIVED (the tests
    run the code but check the wrong thing) apart from NO_COVERAGE."""
    counts: dict[Status, tuple[int, int]] = {}
    for run in runs:
        for r in run.ledger:
            if r.candidate.target is not None:
                status = r.candidate.target.status
                candidates, kept = counts.get(status, (0, 0))
                counts[status] = (candidates + 1, kept + r.kept)
    return counts


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
    return [_mutant(m) for m in _read_json(root / repo / "baseline.json")["mutants"]]


def read_exclusions(repo: str, root: Path = CORPUS_DIR) -> dict[str, str]:
    """Classes excluded by the selection rule, each with its one-line reason.

    A missing file raises on purpose: a repo is reviewed before any run on it.
    """
    return dict(_read_json(root / repo / "exclusions.json"))


def read_failing_tests(repo: str, root: Path = CORPUS_DIR) -> set[str]:
    """Existing test classes that fail on unmodified code, left out of every PIT run.

    A missing file raises on purpose: a repo is reviewed before any run on it.
    """
    return set(_read_json(root / repo / "failing-tests.json"))


def _read_json(path: Path):
    return json.loads(path.read_text(encoding="utf-8"))


def _mutant(m: dict) -> Mutant:
    return Mutant(
        **{
            **m,
            "indexes": tuple(m["indexes"]),
            "status": Status(m["status"]),
            "killing_tests": tuple(m["killing_tests"]),
        }
    )


# --------------------------------------------------------------------------
# results/
# --------------------------------------------------------------------------


def write_run(metrics: RunMetrics, root: Path = RESULTS_DIR) -> Path:
    """Save one run: its metrics, its run ledger and requests, and the source of
    every kept test.

    Replaces the class's earlier run of the same kind, kept tests included.
    """
    folder = root / metrics.repo / metrics.kind.value.lower()
    path = folder / f"{metrics.target_class}.json"
    if path.is_file():
        for entry in _read_json(path)["ledger"]:
            if entry["verdict"] == Verdict.KEPT.value:
                (folder / KEPT_DIR / entry["path"]).unlink(missing_ok=True)
    for r in metrics.ledger:
        if r.kept:
            kept = folder / KEPT_DIR / r.candidate.path
            kept.parent.mkdir(parents=True, exist_ok=True)
            kept.write_text(r.candidate.source, encoding="utf-8")

    run = {
        "repo": metrics.repo,
        "class": metrics.target_class,
        "kind": metrics.kind.value,
        "setup": metrics.setup,
        "score_before": metrics.score_before,
        "score_after": metrics.score_after,
        "mutants_total": metrics.mutants_total,
        "undetected_before": metrics.undetected_before,
        "newly_detected": metrics.newly_detected,
        "candidates": len(metrics.ledger),
        "kept": metrics.kept,
        "acceptance_rate": metrics.acceptance_rate if metrics.ledger else None,
        "attempts_per_kept_test": metrics.attempts_per_kept_test,
        "tokens": metrics.tokens,
        "ledger": [_result_json(r) for r in metrics.ledger],
        "requests": [asdict(q) for q in metrics.requests],
    }
    folder.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(run, indent=2) + "\n", encoding="utf-8")
    return path


def read_runs(repo: str, root: Path = RESULTS_DIR) -> list[RunMetrics]:
    """Load every saved run for a repository, of every kind."""
    return [
        _run(_read_json(path))
        for kind in RunKind
        for path in sorted((root / repo / kind.value.lower()).glob("*.json"))
    ]


def write_summary(
    repo: str,
    baseline: list[Mutant],
    runs: list[RunMetrics],
    selected: Collection[str] = (),
    root: Path = RESULTS_DIR,
) -> Path:
    """Save the repository's headline scores for each run kind, and the derived
    metrics. `selected` are the classes the selection rule picks; any without a
    run are listed as not run."""
    detected = sum(is_detected(m) for m in baseline)
    summary = {
        "repo": repo,
        "mutants_total": len(baseline),
        "detected_before": detected,
    }
    for kind in RunKind:
        of_kind = [r for r in runs if r.kind is kind]
        if not of_kind:
            continue
        before, after = headline_scores(baseline, of_kind)
        candidates = sum(len(r.ledger) for r in of_kind)
        kept = sum(r.kept for r in of_kind)
        attempts = sum(r.candidate.attempts for run in of_kind for r in run.ledger)
        tokens: Counter[str] = Counter()
        for run in of_kind:
            tokens.update(run.tokens)
        summary[kind.value.lower()] = {
            "score_before": before,
            "score_after": after,
            "detected_after": detected + sum(r.newly_detected for r in of_kind),
            "candidates": candidates,
            "kept": kept,
            "acceptance_rate": kept / candidates if candidates else None,
            "attempts_per_kept_test": attempts / kept if kept else None,
            "kept_after_repair": kept_after_repair(of_kind),
            "verdicts": {v.value: n for v, n in sorted(verdict_counts(of_kind).items())},
            "by_target_status": {
                s.value: {"candidates": c, "kept": k}
                for s, (c, k) in sorted(by_target_status(of_kind).items())
            },
            "requests": sum(len(r.requests) for r in of_kind),
            "requests_reused": sum(q.reused for r in of_kind for q in r.requests),
            "tokens": dict(tokens),
            "settings": [
                dict(s)
                for s in sorted(
                    {tuple((k, r.setup.get(k, "")) for k in REPLY_SETTINGS) for r in of_kind}
                )
            ],
            "classes_not_run": sorted(set(selected) - {r.target_class for r in of_kind}),
            "classes": [
                {
                    "class": r.target_class,
                    "candidates": len(r.ledger),
                    "kept": r.kept,
                    "score_before": r.score_before,
                    "score_after": r.score_after,
                }
                for r in of_kind
            ],
        }
    path = root / repo / "summary.json"
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(summary, indent=2) + "\n", encoding="utf-8")
    return path


def _result_json(r: GateResult) -> dict:
    c = r.candidate
    return {
        "test_class": c.test_class,
        "path": c.path.as_posix(),
        "target": asdict(c.target) if c.target else None,
        "attempts": c.attempts,
        "verdict": r.verdict.value,
        "detail": r.detail,
        "mutants_killed": [asdict(m) for m in r.mutants_killed],
        "source": c.source,
    }


def _run(run: dict) -> RunMetrics:
    ledger = [
        GateResult(
            Candidate(
                e["test_class"],
                Path(e["path"]),
                e["source"],
                _mutant(e["target"]) if e["target"] else None,
                e["attempts"],
            ),
            Verdict(e["verdict"]),
            e["detail"],
            tuple(_mutant(m) for m in e["mutants_killed"]),
        )
        for e in run["ledger"]
    ]
    requests = [
        Request(**{**q, "screen": Verdict(q["screen"]) if q["screen"] else None})
        for q in run["requests"]
    ]
    return RunMetrics(
        repo=run["repo"],
        target_class=run["class"],
        kind=RunKind(run["kind"]),
        setup=run["setup"],
        mutants_total=run["mutants_total"],
        undetected_before=run["undetected_before"],
        newly_detected=run["newly_detected"],
        ledger=ledger,
        requests=requests,
    )
