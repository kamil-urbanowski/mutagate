"""Ask a model for a test aimed at one specific mutant.

Reaches the model only through a Provider. A ProviderError is not caught here;
it stops the run.
"""

from .context import MutantContext
from .gate import Candidate, GateResult
from .providers.base import Provider


def test_class_name(class_name: str, index: int) -> str:
    """A fully qualified test class name, unique within the class's batch.

    The same name is kept through the repair attempt.
    """
    raise NotImplementedError


def build_prompt(context: MutantContext, test_class: str) -> str:
    """The request for one JUnit 5 test class holding exactly one test method.

    Without a mutant in the context, this is the untargeted control request.
    """
    raise NotImplementedError


def extract_test(reply: str, test_class: str) -> str | None:
    """Pull the test file out of a model reply.

    None when the reply holds no usable test: no code, or not exactly one test
    method.
    """
    raise NotImplementedError


def generate_test(context: MutantContext, provider: Provider, test_class: str) -> Candidate:
    """Ask for one test and wrap the reply as a Candidate."""
    raise NotImplementedError


def repair(
    context: MutantContext, provider: Provider, candidate: Candidate, failure: GateResult
) -> Candidate:
    """The single repair attempt, given the screen verdict that rejected it.

    Returns a new Candidate with the same name and target, and `attempts` of 2.
    """
    raise NotImplementedError
