"""Offline provider: writes the prompt to a file and waits for a response file.

For environments where no model API is reachable. The human is the network
connection. Slower and manual, but it means no policy has to change for
someone to run the tool.
"""

from pathlib import Path


class OfflineProvider:
    name = "offline"

    def __init__(self, exchange_dir: Path):
        self.exchange_dir = exchange_dir

    def complete(self, prompt: str) -> str:
        """Write prompt.txt, wait for response.txt, return its contents."""
        raise NotImplementedError
