"""Every model call goes through here. Design: `docs/design/providers.md`."""

import hashlib
import json
from dataclasses import dataclass
from pathlib import Path
from typing import Protocol

# Where `load` points the offline provider's prompt and reply files.
OFFLINE_DIR = Path(".mutagate/offline")

# Where replies are saved for reuse by a re-run, one folder per repository.
REPLIES_DIR = Path(".mutagate/replies")


class ProviderError(Exception):
    """The model could not be reached or the call failed.

    Stops the run. Never recorded as a rejected test: it says nothing about the
    model's tests.
    """


class Provider(Protocol):
    name: str
    settings: dict[str, str]  # what shapes a reply, such as the model; recorded with each run
    version: str  # the provider's own version, recorded with each run; "" if none
    last_usage: dict[str, int] | None  # tokens the last request used, if the provider says

    def complete(self, shared: str, task: str) -> str:
        """Send a request, return the response text. Raises ProviderError on failure.

        `shared` is identical for every request about a class, so a provider
        may cache it; `task` is the rest. `shared`, a blank line, then `task`
        is the whole request as one text.
        """
        ...


def load(name: str) -> Provider:
    """The provider of that name, with the settings the measured runs use."""
    if name == "claude-code":
        from .claude_code import ClaudeCodeProvider

        return ClaudeCodeProvider()
    if name == "offline":
        from .offline import OfflineProvider

        return OfflineProvider(OFFLINE_DIR)
    raise ValueError(f"unknown provider: {name}")


@dataclass
class Exchange:
    """One request to the model and its reply."""

    reply: str
    tokens: dict[str, int] | None  # as the provider reported them; None if it reports none
    reused: bool  # read from the saved replies instead of sent


class SavedReplies:
    """A provider that saves every reply as it arrives, and answers a request
    already answered from its saved reply instead of sending it again.

    A run stopped partway, by a usage limit for example, can then be re-run
    without spending the same requests twice. A request is the same only if
    both its parts, the provider, and its settings are all the same. Every
    request, sent or reused, is kept in `exchanges` for the run's record.
    """

    def __init__(self, provider: Provider, directory: Path):
        self.provider = provider
        self.directory = directory
        self.exchanges: list[Exchange] = []

    @property
    def name(self) -> str:
        return self.provider.name

    @property
    def settings(self) -> dict[str, str]:
        return self.provider.settings

    @property
    def version(self) -> str:
        return self.provider.version

    @property
    def last_usage(self) -> dict[str, int] | None:
        return self.exchanges[-1].tokens if self.exchanges else None

    def complete(self, shared: str, task: str) -> str:
        path = self.directory / f"{self._key(shared, task)}.json"
        try:
            if path.is_file():
                saved = json.loads(path.read_text(encoding="utf-8"))
                exchange = Exchange(saved["reply"], saved["tokens"], reused=True)
            else:
                reply = self.provider.complete(shared, task)
                exchange = Exchange(reply, self.provider.last_usage, reused=False)
                self.directory.mkdir(parents=True, exist_ok=True)
                # Written whole or not at all, so a crash cannot leave half a reply.
                partial = path.with_suffix(".partial")
                partial.write_text(
                    json.dumps({"reply": reply, "tokens": exchange.tokens}), encoding="utf-8"
                )
                partial.replace(path)
        except (OSError, ValueError, KeyError) as e:
            raise ProviderError(f"saved replies in {self.directory}: {e}") from e
        self.exchanges.append(exchange)
        return exchange.reply

    def _key(self, shared: str, task: str) -> str:
        # Not the provider's version: an update between a stop and a re-run must not resend.
        # Each part its own field: the same text split another way is another request,
        # as Claude sees one part as the system prompt and the other as the message.
        request = {"provider": self.name, "settings": self.settings, "shared": shared, "task": task}
        return hashlib.sha256(json.dumps(request, sort_keys=True).encode("utf-8")).hexdigest()
