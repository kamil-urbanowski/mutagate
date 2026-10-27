"""The Claude Code provider, against a stand-in `claude` script: no model is called."""

import json
import sys

import pytest

from mutagate.providers.base import ProviderError
from mutagate.providers.claude_code import MODEL, ClaudeCodeProvider

FAKE_CLAUDE = """\
import json, os, sys
scenario = json.load(open(os.environ["FAKE_CLAUDE_SCENARIO"]))
args = sys.argv[1:]
if args == ["--version"]:
    print("9.9.9 (Claude Code)")
elif args[:2] == ["auth", "status"]:
    print(json.dumps(scenario["auth"]))
else:
    with open(scenario["log"], "a") as log:
        log.write(json.dumps({"args": args, "stdin": sys.stdin.read(), "env": dict(os.environ)}) + "\\n")
    print(json.dumps(scenario["result"]))
    sys.exit(scenario.get("exit", 0))
"""

LOGGED_IN = {"loggedIn": True, "authMethod": "claude.ai", "subscriptionType": "pro"}


def success(reply="the reply", **changes):
    result = {
        "type": "result",
        "subtype": "success",
        "is_error": False,
        "result": reply,
        "num_turns": 1,
        "stop_reason": "end_turn",
        "usage": {
            "input_tokens": 2,
            "cache_creation_input_tokens": 900,
            "cache_read_input_tokens": 0,
            "output_tokens": 40,
            "output_tokens_details": {"thinking_tokens": 25},
        },
        "modelUsage": {MODEL: {"inputTokens": 2, "outputTokens": 40}},
    }
    return {**result, **changes}


@pytest.fixture
def claude(tmp_path, monkeypatch):
    """A stand-in `claude`, and a function that sets what it answers."""
    script = tmp_path / "claude"
    script.write_text(f"#!{sys.executable}\n{FAKE_CLAUDE}")
    script.chmod(0o755)
    scenario = tmp_path / "scenario.json"
    log = tmp_path / "calls.jsonl"
    monkeypatch.setenv("FAKE_CLAUDE_SCENARIO", str(scenario))

    def answer(result=None, auth=LOGGED_IN, exit=0):
        scenario.write_text(
            json.dumps({"auth": auth, "result": result or success(), "log": str(log), "exit": exit})
        )

    def calls():
        return [json.loads(line) for line in log.read_text().splitlines()] if log.exists() else []

    answer()
    return ClaudeCodeProvider(executable=str(script)), answer, calls


def test_reply_tokens_and_call(claude, monkeypatch):
    provider, _, calls = claude
    monkeypatch.setenv("ANTHROPIC_API_KEY", "sk-not-to-be-used")
    monkeypatch.setenv("CLAUDE_CODE_USE_BEDROCK", "1")
    monkeypatch.setenv("CLAUDE_EFFORT", "max")
    monkeypatch.setenv("CLAUDE_CONFIG_DIR", "/the/config")

    assert provider.complete("the shared part", "the task") == "the reply"
    assert provider.last_usage == {
        "input_tokens": 2,
        "cache_creation_input_tokens": 900,
        "cache_read_input_tokens": 0,
        "output_tokens": 40,
        "thinking_tokens": 25,
    }
    (call,) = calls()
    assert call["stdin"] == "the task"
    args = call["args"]
    assert args[0] == "-p"
    assert args[args.index("--system-prompt") + 1] == "the shared part"
    assert args[args.index("--model") + 1] == MODEL
    assert args[args.index("--effort") + 1] == "medium"
    assert args[args.index("--tools") + 1] == ""
    assert args[args.index("--output-format") + 1] == "json"
    assert {"--safe-mode", "--no-session-persistence", "--disable-slash-commands"} <= set(args)
    # No API key or cloud provider can reach Claude Code; side calls are off.
    assert "ANTHROPIC_API_KEY" not in call["env"]
    assert "CLAUDE_CODE_USE_BEDROCK" not in call["env"]
    assert "CLAUDE_EFFORT" not in call["env"]
    assert call["env"]["CLAUDE_CONFIG_DIR"] == "/the/config"
    assert call["env"]["CLAUDE_CODE_DISABLE_NONESSENTIAL_TRAFFIC"] == "1"


# Claude Code 2.1.311's prompt-caching switches: none starts with ANTHROPIC_ or CLAUDE.
CACHING_SWITCHES = [
    "DISABLE_PROMPT_CACHING",
    "DISABLE_PROMPT_CACHING_FABLE",
    "DISABLE_PROMPT_CACHING_HAIKU",
    "DISABLE_PROMPT_CACHING_MYTHOS",
    "DISABLE_PROMPT_CACHING_OPUS",
    "DISABLE_PROMPT_CACHING_SONNET",
    "ENABLE_PROMPT_CACHING_1H",
    "ENABLE_PROMPT_CACHING_1H_BEDROCK",
    "FORCE_PROMPT_CACHING_5M",
]


def test_caching_switches_are_removed(claude, monkeypatch):
    provider, _, calls = claude
    for name in CACHING_SWITCHES:
        monkeypatch.setenv(name, "1")
    monkeypatch.setenv("CLAUDE_CONFIG_DIR", "/the/config")

    provider.complete("the shared part", "the task")
    (call,) = calls()
    assert not set(CACHING_SWITCHES) & set(call["env"])
    assert call["env"]["CLAUDE_CONFIG_DIR"] == "/the/config"


def test_version_and_settings(claude):
    provider, _, _ = claude
    assert provider.version == "9.9.9"
    assert provider.settings["model"] == MODEL
    assert provider.settings["effort"] == "medium"
    # A label, not the text, which differs per class.
    assert provider.settings["system_prompt"] == "the shared part of each request"


@pytest.mark.parametrize(
    "auth",
    [
        {"loggedIn": True, "authMethod": "apiKey"},
        {"loggedIn": True, "authMethod": "console"},
        {"loggedIn": False},
    ],
)
def test_refuses_anything_but_a_claude_ai_login(claude, auth):
    provider, answer, calls = claude
    answer(auth=auth)
    with pytest.raises(ProviderError):
        provider.complete("the shared part", "the task")
    assert calls() == []  # nothing was sent


@pytest.mark.parametrize(
    "result, message",
    [
        (success("You've hit your usage limit", is_error=True), "usage limit"),
        (success(subtype="error_during_execution"), "failed"),
        (success(stop_reason="max_tokens"), "ended early"),
        (success(stop_reason="refusal"), "ended early"),
        (success(num_turns=2), "one turn"),
        (success(modelUsage={}), "alone"),
        (success(modelUsage={"claude-other": {"outputTokens": 5}}), "alone"),
        (success(modelUsage={MODEL: {"outputTokens": 0}}), "alone"),
    ],
)
def test_anything_but_a_whole_reply_stops_the_run(claude, result, message):
    provider, answer, _ = claude
    answer(result)
    with pytest.raises(ProviderError, match=message):
        provider.complete("the shared part", "the task")


def test_unreadable_output_stops_the_run(claude):
    provider, answer, _ = claude
    answer(result="not json")  # printed as a JSON string, not a result object
    with pytest.raises(ProviderError):
        provider.complete("the shared part", "the task")


def test_missing_command(tmp_path):
    provider = ClaudeCodeProvider(executable=str(tmp_path / "no-such-claude"))
    with pytest.raises(ProviderError, match="not found"):
        provider.complete("the shared part", "the task")
