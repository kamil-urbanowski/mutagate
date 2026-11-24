from pathlib import Path

import pytest
from click.testing import CliRunner

from mutagate import cli, report
from mutagate.gate import Candidate, GateResult, Verdict
from mutagate.mutants import Mutant, Status
from mutagate.providers.base import ProviderError
from mutagate.providers.claude_code import ClaudeCodeProvider
from mutagate.providers.local import LocalProvider

COUNTER = """\
package pkg;

public class Counter {
    private int count;

    public void add(int n) {
        if (n < 0) {
            throw new IllegalArgumentException();
        }
        count += n;
    }

    public void reset() {
        count = 0;
    }

    public int count() {
        return count;
    }
}
"""

SOURCES = {Path("src/main/java/pkg/Counter.java"): COUNTER}


def targets(n: int) -> list[Mutant]:
    return [
        Mutant("Counter.java", "pkg.Counter", "add", "(I)V", 7, "M", (i,), "d", Status.SURVIVED)
        for i in range(n)
    ]


def named(n_targets: int) -> list[str]:
    _, labels = cli._requests(report.RunKind.CONTROL, SOURCES, "pkg.Counter", targets(n_targets))
    return labels


def test_control_names_each_method_once_when_targets_outnumber_methods():
    assert named(5) == ["public void add(int n)", "public void reset()", "public int count()"]


def test_control_makes_no_more_requests_than_targets():
    assert named(2) == ["public void add(int n)", "public void reset()"]


# --------------------------------------------------------------------------
# The cascade and the Claude model option
# --------------------------------------------------------------------------


def judged(*verdicts: Verdict) -> list[GateResult]:
    return [
        GateResult(Candidate(f"pkg.CounterMutaGate{i}Test", Path("t"), "src", None), verdict)
        for i, verdict in enumerate(verdicts, start=1)
    ]


def test_a_targeted_second_pass_skips_mutants_a_kept_test_already_killed():
    a, b, c = targets(3)
    first = judged(Verdict.KEPT, Verdict.MISSED_TARGET, Verdict.NO_USABLE_TEST)
    # The first target's kept test killed the second target's mutant too.
    assert cli._second_pass(report.RunKind.TARGETED, first, {a, b}, [a, b, c]) == [2]


def test_a_control_second_pass_gets_the_requests_left_without_a_kept_test():
    first = judged(Verdict.KEPT, Verdict.NO_NEW_KILL, Verdict.NO_USABLE_TEST)
    assert cli._second_pass(report.RunKind.CONTROL, first, set(targets(3)), targets(3)) == [1, 2]


class Named:
    def __init__(self, name, version, settings):
        self.name, self.version, self.settings = name, version, settings


def test_a_cascade_setup_records_both_providers():
    local = Named("local", "mlx-lm 1", {"model": "gpt", "effort": "medium"})
    claude = Named("claude-code", "2.1", {"model": "claude-sonnet-5-5", "effort": "medium"})
    assert cli._setup(local, claude) == {
        "provider": "cascade",
        "first_provider": "local",
        "first_provider_version": "mlx-lm 1",
        "first_model": "gpt",
        "first_effort": "medium",
        "second_provider": "claude-code",
        "second_provider_version": "2.1",
        "second_model": "claude-sonnet-5-5",
        "second_effort": "medium",
    }
    assert cli._setup(claude, None) == {
        "provider": "claude-code",
        "provider_version": "2.1",
        "model": "claude-sonnet-5-5",
        "effort": "medium",
    }


def invoke(repo: Path, *options: str):
    return CliRunner().invoke(cli.main, ["run", "--repo", str(repo), "--target", "pkg.X", *options])


def test_the_claude_model_is_given_by_its_full_name(tmp_path):
    result = invoke(tmp_path, "--claude-model", "opus")
    assert result.exit_code == 2 and "full name" in result.output


def test_the_claude_model_is_for_claude_code_and_the_cascade_only(tmp_path):
    result = invoke(tmp_path, "--provider", "local", "--claude-model", "claude-opus-5-5")
    assert result.exit_code == 2 and "--provider claude-code or cascade" in result.output


@pytest.mark.parametrize("control", [False, True], ids=["targeted", "control"])
def test_the_cascade_is_the_local_model_then_the_chosen_claude_model(
    tmp_path, monkeypatch, control
):
    seen = {}

    def stop(repo, target, first, second=None):
        seen["first"], seen["second"] = first.provider, second.provider
        raise ProviderError("stopped here")

    monkeypatch.setattr(cli, "run_control" if control else "run_targeted", stop)
    options = ["--provider", "cascade", "--claude-model", "claude-opus-5-5"]
    result = invoke(tmp_path, *options, *(["--control"] if control else []))
    assert isinstance(seen["first"], LocalProvider)
    assert isinstance(seen["second"], ClaudeCodeProvider)
    assert seen["second"].settings["model"] == "claude-opus-5-5"
    assert "The run stopped" in result.output
