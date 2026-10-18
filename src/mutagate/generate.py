"""Ask a model for a test aimed at one specific mutant.

Reaches the model only through a Provider. A ProviderError is not caught here;
it stops the run.

Each request asks for one JUnit 5 test class holding exactly one test method.
The prompt puts what is the same for every request first (the rules, then the
class and its context) and the target last. A reply is usable only if it holds
exactly one code block that declares the requested package and class, with no
other top-level type, and exactly one `@Test` method and no parameterized,
repeated, or dynamic tests. Nothing in a reply is fixed or renamed: a reply
that breaks a rule is rejected, and gets the one repair attempt like any other
screen failure.
"""

import re
import textwrap

from tree_sitter import Node

from .context import TYPE_DECLARATIONS, MutantContext, declared_package, parse_java
from .gate import Candidate, GateResult, Verdict
from .mutants import Status
from .providers.base import Provider

# PIT names parameterized, repeated, and dynamic tests in forms
# `pit.parse_report` does not read, so a reply using them is not usable.
BANNED_ANNOTATIONS = frozenset({"ParameterizedTest", "RepeatedTest", "TestFactory", "TestTemplate"})

# How many lines on each side of the target line the prompt shows, numbered.
EXCERPT_LINES = 3

# Only a screen failure gets the repair attempt.
SCREEN_VERDICTS = frozenset(
    {
        Verdict.NO_USABLE_TEST,
        Verdict.DID_NOT_COMPILE,
        Verdict.FAILED_ON_UNMODIFIED_CODE,
        Verdict.NONDETERMINISTIC,
    }
)

RULES = """\
Write one JUnit 5 test for a Java class in an existing Maven project.

Rules for your reply:
- Reply with the complete test file in one code block, and nothing else.
- Use the package and class name given at the end.
- Exactly one method annotated with @Test. Do not use @ParameterizedTest,
  @RepeatedTest, @TestFactory, or @TestTemplate.
- No other top-level class in the file. Put any helper class inside the test
  class.
- The test must compile, and pass every time it runs against the code as it
  is now.
- Use JUnit 5 (org.junit.jupiter.api). Use another test library only if the
  existing tests below already use it."""

# What the existing suite did about a target, for the model.
_STATUS = {
    Status.SURVIVED: "the existing tests run this code, but none of them failed after the change",
    Status.NO_COVERAGE: "no existing test runs this code",
}

_TASK = (
    "Write a test that passes against the code as it is now, and would fail if this change "
    "were made."
)

# The repair request after NO_USABLE_TEST: a note, and the rules again.
_NO_USABLE_TEST = """\
## Your previous reply

Your previous reply held no usable test. Reply again, following every rule:
- exactly one code block;
- the package and class name given above, and no other top-level class;
- exactly one method annotated with @Test, and no @ParameterizedTest,
  @RepeatedTest, @TestFactory, or @TestTemplate."""

_FIX = "Fix the test. Reply with the complete corrected file, with the same package and class name."

# What went wrong with the previous attempt, for the repair request.
_FAILED = {
    Verdict.DID_NOT_COMPILE: "It did not compile. The build reported:",
    Verdict.FAILED_ON_UNMODIFIED_CODE: "It failed against the code as it is now. The build reported:",
    Verdict.NONDETERMINISTIC: "It passed some runs and failed others against the code as it is now:",
}

# One fenced code block: an opening fence of three or more backticks, then the
# code, then a closing fence of the same length.
_FENCE = re.compile(r"^[ \t]*(`{3,})[^\n`]*\n(.*?)^[ \t]*\1[ \t]*$", re.MULTILINE | re.DOTALL)


def test_class_name(class_name: str, index: int) -> str:
    """A fully qualified test class name, unique within the class's batch.

    `index` starts at 1: the target's position among the class's targets, or
    the request's number in a control run. The same name is kept through the
    repair attempt.
    """
    if index < 1:
        raise ValueError(f"index starts at 1, got {index}")
    return f"{class_name}MutaGate{index}Test"


def build_prompt(context: MutantContext, test_class: str) -> str:
    """The request for one JUnit 5 test class holding exactly one test method.

    Without a mutant in the context, this is the untargeted control request,
    which is designed with the control (build order step 5).
    """
    if context.mutant is None:
        raise NotImplementedError("the control request is designed at build order step 5")
    if test_class.rpartition(".")[0] != context.test_package:
        raise ValueError(f"{test_class} is not in the class's package, {context.test_package}")
    return "\n\n".join([RULES, _class_sections(context), _target_section(context, test_class)])


def extract_test(reply: str, test_class: str) -> str | None:
    """Pull the test file out of a model reply.

    None when the reply holds no usable test: not exactly one code block, not
    the requested package and class, another top-level type, or not exactly
    one `@Test` method. Parameterized, repeated, and dynamic tests are not
    usable either. Windows line endings are read the same as Unix ones.
    """
    blocks = _FENCE.findall(reply.replace("\r\n", "\n").replace("\r", "\n"))
    if len(blocks) != 1:
        return None
    source = blocks[0][1]
    package, _, simple = test_class.rpartition(".")
    tree = parse_java(source)
    types = [n for n in tree.root_node.named_children if n.type in TYPE_DECLARATIONS]
    if declared_package(tree) != package or len(types) != 1:
        return None
    if types[0].type != "class_declaration" or _name(types[0]) != simple:
        return None
    annotations = [
        _annotations(n) for n in _descendants(tree.root_node) if n.type == "method_declaration"
    ]
    if sum("Test" in names for names in annotations) != 1:
        return None
    if any(names & BANNED_ANNOTATIONS for names in annotations):
        return None
    return source.rstrip() + "\n"


def generate_test(context: MutantContext, provider: Provider, test_class: str) -> Candidate:
    """Ask for one test and wrap the reply as a Candidate."""
    reply = provider.complete(build_prompt(context, test_class))
    return _candidate(context, test_class, reply, attempts=1)


def repair(
    context: MutantContext, provider: Provider, candidate: Candidate, failure: GateResult
) -> Candidate:
    """The single repair attempt, given the screen verdict that rejected it.

    Returns a new Candidate with the same name and target, and `attempts` of 2.
    The request is the original one, followed by the failed test and the
    verdict's detail; after NO_USABLE_TEST, only a note that the reply held no
    usable test and the rules again.
    """
    if failure.verdict not in SCREEN_VERDICTS:
        raise ValueError(f"only a screen failure gets a repair, not {failure.verdict.value}")
    if failure.candidate.test_class != candidate.test_class:
        raise ValueError(
            f"the verdict is on {failure.candidate.test_class}, not {candidate.test_class}"
        )
    if candidate.attempts != 1:
        raise ValueError(f"{candidate.test_class} already had its repair attempt")
    prompt = build_prompt(context, candidate.test_class) + "\n\n" + _previous(candidate, failure)
    return _candidate(context, candidate.test_class, provider.complete(prompt), attempts=2)


# --------------------------------------------------------------------------
# Helpers
# --------------------------------------------------------------------------


def _candidate(context: MutantContext, test_class: str, reply: str, attempts: int) -> Candidate:
    return Candidate(
        test_class=test_class,
        path=context.test_dir / f"{test_class.rpartition('.')[2]}.java",
        source=extract_test(reply, test_class),
        target=context.mutant,
        attempts=attempts,
    )


def _class_sections(context: MutantContext) -> str:
    """The class, its collaborators, and its existing tests: the same for every
    request about the class."""
    simple = context.class_name.rpartition(".")[2]
    parts = [f"## The class under test: {context.class_name}", _block(context.class_source)]
    parts.append("## Classes it uses, as signatures (declarations without bodies)")
    parts += [_block(s) for s in context.collaborator_signatures] or ["None in this project."]
    parts.append(f"## Existing tests that mention {simple}")
    parts += [_block(t) for t in context.existing_tests] or ["None."]
    return "\n\n".join(parts)


def _target_section(context: MutantContext, test_class: str) -> str:
    mutant = context.mutant
    if mutant.status not in _STATUS:
        raise ValueError(f"a target is undetected; this one is {mutant.status.value}")
    package, _, simple = test_class.rpartition(".")
    change = (
        f"PIT, a mutation testing tool, made one small change to "
        f"{context.class_name.rpartition('.')[2]} on line {mutant.line}, and "
        f"{_STATUS[mutant.status]}."
    )
    return "\n\n".join(
        [
            "## Your task",
            change,
            f"PIT's description of the change: {mutant.description}",
            f"Line {mutant.line}, marked with >, and the lines around it:",
            _block(_excerpt(context.class_source, mutant.line), language=""),
            "The line is in this method:",
            _block(context.method_source),
            _TASK,
            f"Package: {package}\nClass name: {simple}",
        ]
    )


def _excerpt(source: str, line: int) -> str:
    """The lines around `line`, numbered, with `line` marked: its text alone
    can repeat within a method."""
    lines = source.removesuffix("\n").split("\n")
    first, last = max(1, line - EXCERPT_LINES), min(len(lines), line + EXCERPT_LINES)
    code = textwrap.dedent("\n".join(text.rstrip("\r") for text in lines[first - 1 : last]))
    return "\n".join(
        f"{'>' if n == line else ' '} {n:>4} | {text}"
        for n, text in enumerate(code.split("\n"), start=first)
    )


def _previous(candidate: Candidate, failure: GateResult) -> str:
    """What the repair request adds to the original request."""
    if failure.verdict is Verdict.NO_USABLE_TEST:
        return _NO_USABLE_TEST
    return "\n\n".join(
        [
            "## Your previous attempt",
            "This was your previous test:",
            _block(candidate.source),
            _FAILED[failure.verdict],
            _block(failure.detail, language=""),
            _FIX,
        ]
    )


def _block(code: str, language: str = "java") -> str:
    """A fenced code block, with a fence longer than any run of backticks in it."""
    longest = max((len(run) for run in re.findall(r"`+", code)), default=0)
    fence = "`" * max(3, longest + 1)
    return f"{fence}{language}\n{code.rstrip()}\n{fence}"


def _name(node: Node) -> str:
    return node.child_by_field_name("name").text.decode("utf-8")


def _annotations(method: Node) -> set[str]:
    """A method's annotation names, without their package: `Test` for
    `@org.junit.jupiter.api.Test`."""
    modifiers = next((c for c in method.children if c.type == "modifiers"), None)
    if modifiers is None:
        return set()
    return {
        _name(c).rpartition(".")[2]
        for c in modifiers.named_children
        if c.type in ("marker_annotation", "annotation")
    }


def _descendants(node: Node):
    stack = [node]
    while stack:
        current = stack.pop()
        yield current
        stack.extend(current.named_children)
