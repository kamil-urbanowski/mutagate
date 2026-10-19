"""Claude Code provider: each request is one call to Claude Code's scripted mode.

`claude -p` sends one prompt to the model, prints the reply, and exits. It runs
on the account Claude Code is logged in to. A failed call, a hit usage limit
included, stops the run.

Cost guards, for Kamil's own development, testing, and measured runs: they
keep every request on his Claude Pro plan, which his runs must never exceed.
They are not a rule for everyone who uses MutaGate. Someone else may have a
larger or smaller plan, an API key, or a company account; relaxing the guards
for them needs a switch that is not built yet. The guards:

- refuse to send anything unless Claude Code is logged in with a claude.ai
  account, which is a subscription, never an API key;
- remove every API key, auth token, and cloud-provider switch from the
  environment `claude` runs in, so it cannot fall back on one.

Each call has tools off, our own system prompt, an empty working directory,
and no saved session, and leaves out the user's settings, CLAUDE.md files,
auto-memory, skills, and side calls such as a session title. Claude Code still
adds about 600 tokens of its own to each request: a line on what it is, the
account's email, machine details, the date, and the model's name.
"""

import json
import os
import subprocess
import tempfile

from .base import ProviderError

MODEL = "claude-sonnet-5-5"
EFFORT = "medium"
SYSTEM_PROMPT = "You write JUnit 5 tests for existing Java code."

# How long one request may take before the run stops.
TIMEOUT_SECONDS = 600

# The command to run: this variable's value if set, else `claude` on the PATH.
EXECUTABLE_VARIABLE = "MUTAGATE_CLAUDE"

# Never passed to Claude Code: API keys, auth tokens, and base URLs
# (`ANTHROPIC_*`); the switches that send requests to a cloud provider
# (`CLAUDE_CODE_USE_*`), any of which could bill a request outside the Pro
# plan; and any other Claude Code setting or session variable, such as an
# effort level set by a Claude Code session MutaGate runs in. Only the
# location of Claude Code's own configuration, and so of its login, is kept.
_REMOVED_PREFIXES = ("ANTHROPIC_", "CLAUDE")
_KEPT = frozenset({"CLAUDE_CONFIG_DIR"})

_SETTINGS = json.dumps({"autoMemoryEnabled": False})


class ClaudeCodeProvider:
    name = "claude-code"

    def __init__(
        self,
        model: str = MODEL,
        effort: str = EFFORT,
        executable: str | None = None,
        timeout: float = TIMEOUT_SECONDS,
    ):
        self.executable = executable or os.environ.get(EXECUTABLE_VARIABLE) or "claude"
        self.timeout = timeout
        self.settings = {"model": model, "effort": effort, "system_prompt": SYSTEM_PROMPT}
        self.last_usage: dict[str, int] | None = None
        self._version: str | None = None
        self._logged_in = False

    @property
    def version(self) -> str:
        """Claude Code's version, such as `2.1.301`."""
        if self._version is None:
            code, out, err = self._run(["--version"])
            if code != 0 or not out.strip():
                raise ProviderError(f"`claude --version` failed: {(err or out).strip()}")
            self._version = out.split()[0]
        return self._version

    def complete(self, prompt: str) -> str:
        """One call to `claude -p`; the reply text. Raises ProviderError on any failure."""
        self._check_login()
        args = [
            "-p",
            "--model", self.settings["model"],
            "--effort", self.settings["effort"],
            "--system-prompt", self.settings["system_prompt"],
            "--tools", "",
            "--settings", _SETTINGS,
            "--safe-mode",
            "--disable-slash-commands",
            "--no-session-persistence",
            "--output-format", "json",
        ]  # fmt: skip
        with tempfile.TemporaryDirectory(prefix="mutagate-claude-") as cwd:
            _, out, err = self._run(args, prompt, cwd)
        reply, self.last_usage = _reply(out or err, self.settings["model"])
        return reply

    def _check_login(self) -> None:
        """Refuse anything but a claude.ai login: a subscription, never an API key."""
        if self._logged_in:
            return
        _, out, err = self._run(["auth", "status"])
        try:
            status = json.loads(out)
        except ValueError as e:
            raise ProviderError(
                f"could not read `claude auth status`: {(out or err).strip()}"
            ) from e
        if not status.get("loggedIn"):
            raise ProviderError("Claude Code is not logged in; run `claude` and log in")
        if status.get("authMethod") != "claude.ai":
            raise ProviderError(
                f"Claude Code is logged in with {status.get('authMethod')!r}, not a claude.ai "
                "subscription. MutaGate never runs on an API key."
            )
        self._logged_in = True

    def _run(
        self, args: list[str], stdin: str | None = None, cwd: str | None = None
    ) -> tuple[int, str, str]:
        try:
            done = subprocess.run(
                [self.executable, *args],
                input=stdin,
                cwd=cwd,
                env=_environment(),
                capture_output=True,
                text=True,
                timeout=self.timeout,
                check=False,
            )
        except FileNotFoundError as e:
            raise ProviderError(
                f"Claude Code not found: install the `claude` command, or set "
                f"{EXECUTABLE_VARIABLE} to its path"
            ) from e
        except subprocess.TimeoutExpired as e:
            raise ProviderError(f"Claude Code did not answer within {self.timeout} seconds") from e
        except OSError as e:
            raise ProviderError(f"could not run Claude Code: {e}") from e
        return done.returncode, done.stdout, done.stderr


def _environment() -> dict[str, str]:
    env = {k: v for k, v in os.environ.items() if k in _KEPT or not k.startswith(_REMOVED_PREFIXES)}
    env["CLAUDE_CODE_DISABLE_NONESSENTIAL_TRAFFIC"] = "1"  # no side calls, such as a title
    return env


def _reply(output: str, model: str) -> tuple[str, dict[str, int]]:
    """The reply text and its tokens, from `claude -p --output-format json`.

    Anything but a whole reply from `model` is a ProviderError: a failed call,
    a hit usage limit, a reply cut short or refused, or text that no model
    wrote (Claude Code's own messages report no model usage).
    """
    try:
        result = json.loads(output)
    except ValueError:
        result = None
    if not isinstance(result, dict):
        raise ProviderError(f"Claude Code printed no result: {output.strip()[-2000:]}")
    if result.get("is_error") or result.get("subtype") != "success":
        detail = result.get("result") or result.get("subtype")
        raise ProviderError(f"Claude Code call failed: {detail}")
    if result.get("stop_reason") != "end_turn":
        raise ProviderError(f"the reply ended early: {result.get('stop_reason')}")
    if result.get("num_turns") != 1:
        raise ProviderError(f"expected one turn, got {result.get('num_turns')}")
    used = result.get("modelUsage") or {}
    if set(used) != {model} or not used[model].get("outputTokens"):
        raise ProviderError(f"expected a reply from {model} alone; models used: {sorted(used)}")

    usage = result.get("usage") or {}
    tokens = {
        key: usage.get(key, 0)
        for key in (
            "input_tokens",
            "cache_creation_input_tokens",
            "cache_read_input_tokens",
            "output_tokens",
        )
    }
    tokens["thinking_tokens"] = (usage.get("output_tokens_details") or {}).get("thinking_tokens", 0)
    return result.get("result") or "", tokens
