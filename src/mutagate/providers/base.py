"""Every model call goes through here, so swapping providers is one class."""

from typing import Protocol


class ProviderError(Exception):
    """The model could not be reached or the call failed.

    Stops the run. Never recorded as a rejected test: it says nothing about the
    model's tests.
    """


class Provider(Protocol):
    name: str

    def complete(self, prompt: str) -> str:
        """Send a prompt, return the response text. Raises ProviderError on failure."""
        ...


def load(name: str) -> Provider:
    """Resolve a provider by name from configuration."""
    raise NotImplementedError
