"""Assemble the source a model needs to target one mutant. Design: `docs/design/context.md`.

Pure logic over source supplied by `pit.py`: it reads no files and runs nothing.
"""

import re
import textwrap
from dataclasses import dataclass
from pathlib import Path

import tree_sitter_java
from tree_sitter import Language, Node, Parser, Tree

from .mutants import Mutant, outer_class
from .pit import MAIN_SOURCES, TEST_SOURCES

TYPE_DECLARATIONS = frozenset(
    {
        "class_declaration",
        "interface_declaration",
        "enum_declaration",
        "record_declaration",
        "annotation_type_declaration",
    }
)
CALLABLES = frozenset(
    {"method_declaration", "constructor_declaration", "compact_constructor_declaration"}
)
_FIELDS = frozenset({"field_declaration", "constant_declaration"})

_PARSER = Parser(Language(tree_sitter_java.language()))


@dataclass
class MutantContext:
    """Everything the model sees for one request.

    For a control request `mutant` is None and `method_source` is the method
    the request names; the rest is the same.
    """

    mutant: Mutant | None
    class_name: str
    class_source: str
    method_source: str | None
    collaborator_signatures: list[str]  # one per collaborator file
    existing_tests: list[str]  # one per test file, in full
    test_package: str
    test_dir: Path  # where the new test belongs, relative to the repository root


def build_context(
    sources: dict[Path, str],
    class_name: str,
    mutant: Mutant | None = None,
    method: str | None = None,
) -> MutantContext:
    """Gather the enclosing method and class, the signatures of its
    collaborators (one level deep), and the existing tests that mention the class.

    `sources` are the copy's Java files from `pit.read_sources`, read before
    any candidate is added, so every request sees the repo's own tests only.
    For a control request, omit `mutant` and give `method`, the source of the
    method the request names: an entry of `control_methods`.
    """
    path = _source_path(class_name)
    source = class_source(sources, class_name)
    package, _, simple = class_name.rpartition(".")

    if mutant is None:
        if method is None:
            raise ValueError("a control request names a method, from control_methods")
        method_source = method
    else:
        if method is not None:
            raise ValueError("a targeted request's method is the one its mutant is in")
        if outer_class(mutant) != class_name:
            raise ValueError(f"the mutant in {mutant.mutated_class} is not part of {class_name}")
        if mutant.source_file != path.name:
            raise ValueError(f"the mutant is in {mutant.source_file}, not {path.name}")
        method_source = enclosing_method(source, mutant.line)

    collaborators = [
        p
        for p in sources
        if p != path
        and p.is_relative_to(MAIN_SOURCES)
        and p.stem.isidentifier()  # leaves out package-info.java
        and _mentions(source, p.stem)
    ]
    tests = [p for p in sources if p.is_relative_to(TEST_SOURCES) and _mentions(sources[p], simple)]
    return MutantContext(
        mutant=mutant,
        class_name=class_name,
        class_source=source,
        method_source=method_source,
        collaborator_signatures=[
            signatures(sources[p]) for p in sorted(collaborators, key=lambda p: (p.stem, p))
        ],
        existing_tests=[sources[p] for p in sorted(tests)],
        test_package=package,
        test_dir=TEST_SOURCES.joinpath(*package.split(".")),
    )


def class_source(sources: dict[Path, str], class_name: str) -> str:
    """The class's source file, which must be at
    `src/main/java/<package path>/<Class>.java`."""
    path = _source_path(class_name)
    if path not in sources:
        raise ValueError(f"source of {class_name} not found at {path}")
    return sources[path]


def enclosing_method(source: str, line: int) -> str:
    """The outermost method or constructor containing `line` (1-based).

    PIT names constructors `<init>` and lambdas `lambda$...`, which are not
    names in the source, so the code is found by line. A mutant in a lambda or
    an anonymous class gets the method that contains it. A line in no method
    or constructor (a field initializer or an initializer block) raises
    ValueError; no corpus target has one.
    """
    node = _outermost(parse_java(source).root_node, line - 1)
    if node is None:
        raise ValueError(f"line {line} is in no method or constructor")
    return _dedented(source, node)


def control_methods(source: str) -> list[str]:
    """The methods a control request can name, in the order they are named,
    each as its source, dedented like `method_source`.

    Every method and constructor with a body that a test in the class's own
    package can call: not private, and declared in the class, in a named
    nested type that is not private, or in an enum constant's body. Longest
    first, ties in source order. Raises ValueError if there is none, or if the
    file declares more than one top-level type: which one is the class is not
    known here, and no corpus file has two.
    """
    types = [n for n in parse_java(source).root_node.named_children if n.type in TYPE_DECLARATIONS]
    if len(types) > 1:
        raise ValueError("the file declares more than one top-level type; not handled yet")
    methods = _callables(types[0].child_by_field_name("body")) if types else []
    if not methods:
        raise ValueError("the class has no method a test can call")
    # Lines from the first annotation to the closing brace; sorted() keeps ties
    # in source order.
    methods = sorted(methods, key=lambda n: n.start_point[0] - n.end_point[0])
    return [_dedented(source, n) for n in methods]


def declaration_line(method: str) -> str:
    """The line of a method's source that holds its name, trimmed and without
    an opening brace: `public double total(Sale sale)` after any annotations."""
    # A lone constructor does not parse; a record's body takes every kind.
    record = parse_java(f"record R() {{\n{method}\n}}").root_node.named_children[0]
    members = record.child_by_field_name("body").named_children
    node = next(n for n in members if n.type in CALLABLES)
    row = node.child_by_field_name("name").start_point[0] - 1
    return method.split("\n")[row].strip().removesuffix("{").rstrip()


def signatures(source: str) -> str:
    """A Java file's declarations without bodies: what code using it can call.

    Its package line, then each type's declaration and its non-private
    constructors, methods, fields, enum constants, and nested types. Private
    members, method bodies, initializer blocks, and comments are left out.
    """
    tree = parse_java(source)
    lines = []
    package = declared_package(tree)
    if package is not None:
        lines.append(f"package {package};")
    for node in tree.root_node.named_children:
        if node.type in TYPE_DECLARATIONS:
            lines.extend(_type_signature(node, ""))
    return "\n".join(lines)


def parse_java(source: str) -> Tree:
    """Parse Java with the pinned grammar. Syntax errors become error nodes in
    the tree; this never raises."""
    return _PARSER.parse(source.encode("utf-8"))


def declared_package(tree: Tree) -> str | None:
    """The name in a file's `package` line, or None without one."""
    for node in tree.root_node.named_children:
        if node.type == "package_declaration":
            for child in node.named_children:
                if child.type in ("scoped_identifier", "identifier"):
                    return child.text.decode("utf-8")
    return None


# --------------------------------------------------------------------------
# Helpers
# --------------------------------------------------------------------------


def _mentions(text: str, name: str) -> bool:
    return re.search(rf"\b{re.escape(name)}\b", text) is not None


def _source_path(class_name: str) -> Path:
    package, _, simple = class_name.rpartition(".")
    if not package:
        raise ValueError(f"{class_name} is not in a package")
    return MAIN_SOURCES.joinpath(*package.split("."), f"{simple}.java")


def _dedented(source: str, node: Node) -> str:
    """The whole lines `node` spans, dedented."""
    lines = source.split("\n")[node.start_point[0] : node.end_point[0] + 1]
    return textwrap.dedent("\n".join(text.rstrip("\r") for text in lines))


def _callables(body: Node) -> list[Node]:
    """The methods and constructors with a body in a type's `body`, in source
    order, with those of its nested types that are not private and of its enum
    constants' bodies. Never those inside another method's body."""
    found = []
    for member in _members(body):
        inner = member.child_by_field_name("body")
        if inner is None or _is_private(member):
            continue  # abstract and interface methods have no body, nor do fields
        if member.type in CALLABLES:
            found.append(member)
        elif member.type in TYPE_DECLARATIONS or member.type == "enum_constant":
            found.extend(_callables(inner))
    return found


def _outermost(node: Node, row: int) -> Node | None:
    """The first method or constructor, in source order, whose lines include
    `row` (0-based), without looking inside one."""
    for child in node.children:
        if child.start_point[0] <= row <= child.end_point[0]:
            if child.type in CALLABLES:
                return child
            found = _outermost(child, row)
            if found is not None:
                return found
    return None


def _type_signature(node: Node, indent: str) -> list[str]:
    lines = _indented(node, f"{_head(node)} {{", indent)
    inner = indent + "  "
    for member in _members(node.child_by_field_name("body")):
        if _is_private(member):
            continue
        if member.type in TYPE_DECLARATIONS:
            lines.extend(_type_signature(member, inner))
        elif member.type == "enum_constant":
            lines.extend(_indented(member, f"{_head(member)},", inner))
        elif member.type in CALLABLES or member.type == "annotation_type_element_declaration":
            lines.extend(_indented(member, f"{_head(member)};", inner))
        elif member.type in _FIELDS:
            lines.extend(_indented(member, member.text.decode("utf-8").strip(), inner))
    lines.append(f"{indent}}}")
    return lines


def _indented(node: Node, text: str, indent: str) -> list[str]:
    """Text taken from `node`, re-indented: its later lines lose the node's
    original indentation, and every line gets `indent`."""
    first, *rest = [line.rstrip("\r") for line in text.split("\n")]
    column = node.start_point[1]
    return [indent + first] + [indent + _drop_indent(line, column) for line in rest]


def _drop_indent(line: str, width: int) -> str:
    """`line` without up to `width` characters of leading whitespace."""
    stripped = line.lstrip(" \t")
    return line[min(len(line) - len(stripped), width) :]


def _members(body: Node) -> list[Node]:
    """A type's members, including an enum's declarations after its constants."""
    members = []
    for child in body.named_children:
        if child.type == "enum_body_declarations":
            members.extend(child.named_children)
        else:
            members.append(child)
    return members


def _head(node: Node) -> str:
    """A declaration up to its body: `public int size()` from a method."""
    body = node.child_by_field_name("body")
    end = body.start_byte if body is not None else node.end_byte
    return node.text[: end - node.start_byte].decode("utf-8").strip().rstrip(";").rstrip()


def _is_private(node: Node) -> bool:
    modifiers = next((c for c in node.children if c.type == "modifiers"), None)
    return modifiers is not None and any(c.type == "private" for c in modifiers.children)
