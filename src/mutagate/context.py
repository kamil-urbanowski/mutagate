"""Assemble the source context a model needs to target one mutant.

Pure logic over source supplied by `pit.py`.
"""

from dataclasses import dataclass
from pathlib import Path

from .mutants import Mutant


@dataclass
class MutantContext:
    """Everything the model sees for one request.

    For a control request `mutant` and `method_source` are None; the rest is
    the same.
    """

    mutant: Mutant | None
    class_name: str
    class_source: str
    method_source: str | None
    collaborator_signatures: list[str]
    existing_tests: list[str]
    test_package: str
    test_dir: Path  # where the new test belongs, relative to the repository root


def build_context(
    sources: dict[Path, str], class_name: str, mutant: Mutant | None = None
) -> MutantContext:
    """Gather the enclosing method and class, the interfaces of its
    collaborators, and the existing tests for that class.

    This is a retrieval problem, not a file dump. Omit `mutant` for a control
    request.
    """
    raise NotImplementedError
