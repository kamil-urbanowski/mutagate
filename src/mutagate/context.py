"""Assemble the source context a model needs to target one mutant."""

from dataclasses import dataclass
from pathlib import Path

from .mutants import Mutant


@dataclass
class MutantContext:
    mutant: Mutant
    class_source: str
    method_source: str
    collaborator_signatures: list[str]
    existing_tests: str
    test_package: str
    test_file_path: Path


def build_context(repo: Path, mutant: Mutant) -> MutantContext:
    """Gather the enclosing method and class, the interfaces of its
    collaborators, and the existing tests for that class.

    This is a retrieval problem, not a file dump. What goes in here is one of
    the ablations.
    """
    raise NotImplementedError
