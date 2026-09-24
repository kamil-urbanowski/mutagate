"""Ask a model for a test aimed at one specific mutant."""

from .context import MutantContext
from .providers.base import Provider


def generate_test(context: MutantContext, provider: Provider) -> str:
    """Return the source of a single JUnit 5 test method targeting the mutant."""
    raise NotImplementedError


def repair(context: MutantContext, provider: Provider, candidate: str, failure: str) -> str:
    """One bounded retry, given the compiler or test output that rejected it."""
    raise NotImplementedError
