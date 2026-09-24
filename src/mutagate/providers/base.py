"""Every model call goes through here, so swapping providers is one class."""

from typing import Protocol


class Provider(Protocol):
    name: str

    def complete(self, prompt: str) -> str:
        """Send a prompt, return the response text."""
        ...


def load(name: str) -> Provider:
    """Resolve a provider by name from configuration."""
    raise NotImplementedError
