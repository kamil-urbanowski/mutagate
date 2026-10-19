"""Offline provider: writes the prompt to a file and waits for a response file.

For environments where no model API is reachable. The human is the network
connection. Slower and manual, but it means no policy has to change for
someone to run the tool. The first real calls are made this way.

One exchange per request: MutaGate writes `prompt.txt`, the person pastes it
into a chat app and saves the model's reply as `response.txt`, and MutaGate
reads it and goes on.
"""

import sys
import time
from pathlib import Path

from .base import ProviderError

PROMPT_FILE = "prompt.txt"
RESPONSE_FILE = "response.txt"


class OfflineProvider:
    name = "offline"
    version = ""
    last_usage = None  # a person passing files reports no tokens

    def __init__(self, exchange_dir: Path, poll_seconds: float = 1.0):
        self.exchange_dir = exchange_dir
        self.poll_seconds = poll_seconds
        self.settings: dict[str, str] = {}  # the model and settings are the person's choice

    def complete(self, prompt: str) -> str:
        """Write prompt.txt, wait for response.txt, return its contents.

        A leftover response.txt is removed before the prompt is written, and
        the response is removed once read, so every reply answers the prompt
        just written. An empty response.txt counts as not saved yet. There is
        no time limit; stop the run with Ctrl-C.
        """
        prompt_file = self.exchange_dir / PROMPT_FILE
        response_file = self.exchange_dir / RESPONSE_FILE
        try:
            self.exchange_dir.mkdir(parents=True, exist_ok=True)
            response_file.unlink(missing_ok=True)
            prompt_file.write_text(prompt, encoding="utf-8")
            print(
                f"Prompt written to {prompt_file}. Save the model's reply to {response_file}.",
                file=sys.stderr,
            )
            while not (response_file.is_file() and response_file.stat().st_size > 0):
                time.sleep(self.poll_seconds)
            reply = response_file.read_text(encoding="utf-8")
            response_file.unlink()
        except (OSError, UnicodeDecodeError) as e:
            raise ProviderError(f"offline exchange in {self.exchange_dir} failed: {e}") from e
        return reply
