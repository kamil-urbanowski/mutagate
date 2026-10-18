"""Everything touching the target repository and its build.

The only module that touches the target repository or runs external processes.
All work happens on a temporary copy; the original is never written to.

PIT runs through Maven with `-D` properties. The copy's build configuration is
left unchanged with one exception: PIT cannot run JUnit 5 tests without its
JUnit 5 plugin, so the pinned `pitest-maven` (1.30.0) is added to the copy's
`pom.xml`, with `pitest-junit5-plugin` (1.2.3) as its dependency.

    mvn -B test-compile org.pitest:pitest-maven:mutationCoverage \
        -DtargetClasses=... -DoutputFormats=XML -DfullMutationMatrix=true \
        -DtimestampedReports=false

`fullMutationMatrix` makes the report list every test that killed each mutant,
not just the first. Batched verification depends on it.

A class-scoped run sets `targetClasses=pkg.Outer,pkg.Outer$*` so nested and
anonymous classes are included. A bare `pkg.Outer*` would also match other
classes that share the prefix.
"""

import re
import shutil
import subprocess
import tempfile
import xml.etree.ElementTree as ET
from datetime import datetime
from pathlib import Path

from .gate import Candidate
from .mutants import UNFINISHED, Mutant, Status

PITEST_MAVEN_VERSION = "1.30.0"
PITEST_JUNIT5_PLUGIN_VERSION = "1.2.3"

# Every repo is built and measured on this JDK; any update within it is accepted.
JAVA_MAJOR_VERSION = 21

REPORT_FILE = "mutations.xml"

MAIN_SOURCES = Path("src/main/java")
TEST_SOURCES = Path("src/test/java")


class PitError(Exception):
    """A PIT run failed outright. Carries the build output."""

    def __init__(self, message: str, output: str = ""):
        super().__init__(message)
        self.output = output


# --------------------------------------------------------------------------
# The temporary copy
# --------------------------------------------------------------------------


def make_workspace(repo: Path) -> Path:
    """Copy `repo` to a fresh temporary directory and return the copy's root.

    The repo's own build output (`target/`) is not copied. The pinned PIT plugin
    is added to the copy's `pom.xml`. Refuses to start unless Maven runs on the
    pinned JDK, so no build or PIT run can use another. If making the copy fails
    partway, the copy is removed before the error is raised.
    """
    repo = repo.resolve()
    check_toolchain(repo)
    # The real path (on macOS, /private/var rather than /var), as Maven prints it.
    workspace = Path(tempfile.mkdtemp(prefix="mutagate-")).resolve() / repo.name
    try:
        shutil.copytree(
            repo, workspace, ignore=lambda d, names: ["target"] if Path(d) == repo else []
        )
        add_pit_plugin(workspace / "pom.xml")
    except BaseException:
        remove_workspace(workspace)
        raise
    return workspace


def remove_workspace(workspace: Path) -> None:
    """Delete a copy made by `make_workspace`, with its temporary directory."""
    shutil.rmtree(workspace.parent)


def add_pit_plugin(pom: Path) -> None:
    """Add the pinned `pitest-maven` plugin and its JUnit 5 plugin to `pom`.

    Raises PitError if the pom already declares `pitest-maven`: its own settings
    would mix with the pinned ones, and that case is not handled yet.
    """
    tree = ET.parse(pom, ET.XMLParser(target=ET.TreeBuilder(insert_comments=True)))
    project = tree.getroot()
    ns = project.tag[1 : project.tag.index("}")] if project.tag.startswith("{") else ""
    if ns:
        ET.register_namespace("", ns)

    def q(name: str) -> str:
        return f"{{{ns}}}{name}" if ns else name

    def child(parent: ET.Element, name: str) -> ET.Element:
        found = parent.find(q(name))
        return found if found is not None else ET.SubElement(parent, q(name))

    plugins = child(child(project, "build"), "plugins")
    for plugin in plugins.findall(q("plugin")):
        if plugin.findtext(q("artifactId")) == "pitest-maven":
            raise PitError(f"{pom} already declares pitest-maven")

    plugin = ET.SubElement(plugins, q("plugin"))
    for name, value in [
        ("groupId", "org.pitest"),
        ("artifactId", "pitest-maven"),
        ("version", PITEST_MAVEN_VERSION),
    ]:
        ET.SubElement(plugin, q(name)).text = value
    dependency = ET.SubElement(ET.SubElement(plugin, q("dependencies")), q("dependency"))
    for name, value in [
        ("groupId", "org.pitest"),
        ("artifactId", "pitest-junit5-plugin"),
        ("version", PITEST_JUNIT5_PLUGIN_VERSION),
    ]:
        ET.SubElement(dependency, q(name)).text = value

    tree.write(pom, encoding="UTF-8", xml_declaration=True)


def read_sources(workspace: Path) -> dict[Path, str]:
    """Every Java file in the copy, main and test, keyed by path relative to the root."""
    sources = {}
    for path in sorted(workspace.rglob("*.java")):
        relative = path.relative_to(workspace)
        if relative.parts[0] != "target":
            sources[relative] = path.read_text(encoding="utf-8")
    return sources


def add_candidate(workspace: Path, candidate: Candidate) -> None:
    """Write a candidate's test file into the copy."""
    if candidate.source is None:
        raise ValueError(f"{candidate.test_class} has no source to write")
    path = workspace / candidate.path
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(candidate.source, encoding="utf-8")


def remove_candidate(workspace: Path, candidate: Candidate) -> None:
    """Delete a candidate's test file from the copy."""
    (workspace / candidate.path).unlink(missing_ok=True)


# --------------------------------------------------------------------------
# Plain builds, for the screen
# --------------------------------------------------------------------------


def compile_tests(workspace: Path) -> tuple[bool, str]:
    """Compile main and test sources. Returns success and the build output."""
    return _mvn(workspace, ["test-compile"])


def run_test(workspace: Path, test_class: str, times: int = 1) -> list[tuple[bool, str]]:
    """Run one test class against unmodified code `times` times.

    Returns success and the build output for each run.
    """
    return [_mvn(workspace, ["test", f"-Dtest={test_class}"]) for _ in range(times)]


# --------------------------------------------------------------------------
# PIT
# --------------------------------------------------------------------------


def run_pit(workspace: Path, target_class: str | None, excluded_tests: set[str]) -> Path:
    """Run PIT on the copy and return the directory holding its report.

    `target_class` None mutates the whole module (the baseline run); a class
    name scopes the run to that class (verification). `excluded_tests` are the
    repo's failing tests recorded in `corpus/`, left out of every run.
    Raises PitError when the run fails.

    Classes and tests are named from the packages in the copy's source tree,
    not left to PIT's default, which assumes packages start with the groupId.
    """
    if target_class is None:
        target_classes = [f"{p}.*" for p in _packages(workspace / MAIN_SOURCES)]
    else:
        target_classes = [target_class, f"{target_class}$*"]
    target_tests = [f"{p}.*" for p in _packages(workspace / TEST_SOURCES)]

    args = [
        "test-compile",
        "org.pitest:pitest-maven:mutationCoverage",
        f"-DtargetClasses={','.join(target_classes)}",
        f"-DtargetTests={','.join(target_tests)}",
        "-DoutputFormats=XML",
        "-DfullMutationMatrix=true",
        "-DtimestampedReports=false",
    ]
    if excluded_tests:
        args.append(f"-DexcludedTestClasses={','.join(sorted(excluded_tests))}")

    ok, output = _mvn(workspace, args)
    if not ok:
        raise PitError("PIT run failed", output)
    report_dir = workspace / "target" / "pit-reports"
    if not (report_dir / REPORT_FILE).is_file():
        raise PitError(f"PIT wrote no {REPORT_FILE}", output)
    return report_dir


def parse_report(report_dir: Path) -> list[Mutant]:
    """Read PIT's XML report into Mutant records, including their killing tests.

    Raises PitError if PIT left any mutant unfinished (NOT_STARTED or STARTED):
    the run is incomplete and its results cannot be scored.
    """
    root = ET.parse(report_dir / REPORT_FILE).getroot()
    mutants = [_mutant(element) for element in root.iter("mutation")]
    unfinished = [m for m in mutants if m.status in UNFINISHED]
    if unfinished:
        raise PitError(
            f"PIT run incomplete: {len(unfinished)} of {len(mutants)} mutants unfinished"
            f" (first: {unfinished[0].mutated_class} line {unfinished[0].line})"
        )
    return mutants


def check_toolchain(cwd: Path) -> tuple[str, str]:
    """Maven's version and the Java version Maven runs on, read from `mvn -v`.

    Raises PitError unless Java's major version is JAVA_MAJOR_VERSION. Maven
    uses `JAVA_HOME`, which can differ from the `java` on the PATH.
    """
    ok, output = _run(["mvn", "-v"], cwd)
    maven = re.search(r"Apache Maven (\S+)", output)
    java = re.search(r"Java version: ([^,\s]+)", output)
    if not ok or not maven or not java:
        raise PitError("could not read Maven and Java versions from `mvn -v`", output)
    major = re.match(r"\d+", java.group(1))
    if major is None or int(major.group()) != JAVA_MAJOR_VERSION:
        raise PitError(
            f"Maven is running on Java {java.group(1)}; MutaGate requires Java "
            f"{JAVA_MAJOR_VERSION}. Point JAVA_HOME at a JDK {JAVA_MAJOR_VERSION}.",
            output,
        )
    return maven.group(1), java.group(1)


def describe_setup(repo: Path) -> dict[str, str]:
    """The repo commit and tool versions a baseline was measured on, and the date."""
    maven, java = check_toolchain(repo)
    ok, commit = _run(["git", "rev-parse", "HEAD"], repo)
    if not ok:
        raise PitError(f"could not read the commit of {repo}", commit)
    return {
        "repo_commit": commit.strip(),
        "date": datetime.now().astimezone().date().isoformat(),
        "java": java,
        "maven": maven,
        "pitest_maven": PITEST_MAVEN_VERSION,
        "pitest_junit5_plugin": PITEST_JUNIT5_PLUGIN_VERSION,
    }


# --------------------------------------------------------------------------
# Helpers
# --------------------------------------------------------------------------

_PACKAGE = re.compile(r"^\s*package\s+([\w.]+)\s*;", re.MULTILINE)

# How the JUnit 5 plugin names a test in PIT's report, e.g.
# pkg.FooTest.[engine:junit-jupiter]/[class:pkg.FooTest]/[method:name()]
# with any number of [nested-class:Inner] segments before the method, and no
# method at all when the kill is credited to the class as a whole.
_TEST_NAME = re.compile(
    r"[\w.$]+\.\[engine:junit-jupiter\]/\[class:(?P<cls>[\w.$]+)\]"
    r"(?P<nested>(?:/\[nested-class:[\w$]+\])*)"
    r"(?:/\[method:(?P<method>[\w$]+)\([^)]*\)\])?"
)
_NESTED_CLASS = re.compile(r"\[nested-class:([\w$]+)\]")


def _run(command: list[str], cwd: Path) -> tuple[bool, str]:
    result = subprocess.run(
        command, cwd=cwd, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True, check=False
    )
    return result.returncode == 0, result.stdout


def _mvn(workspace: Path, args: list[str]) -> tuple[bool, str]:
    """Run Maven in the copy. File paths in the output start at the repo root.

    The copy's location is random and machine-specific, so it is removed:
    errors read `src/test/java/...`, the same in every run.
    """
    ok, output = _run(["mvn", "-B", *args], workspace)
    for root in sorted({str(workspace.resolve()), str(workspace)}, key=len, reverse=True):
        output = output.replace(f"{root}/", "").replace(root, ".")
    return ok, output


def _packages(source_root: Path) -> list[str]:
    """Every package declared under a source root. Classes must be in a package."""
    packages = set()
    for path in source_root.rglob("*.java"):
        match = _PACKAGE.search(path.read_text(encoding="utf-8"))
        if match is None:
            raise PitError(f"{path} has no package declaration; not supported")
        packages.add(match.group(1))
    return sorted(packages)


def _mutant(element: ET.Element) -> Mutant:
    def child(tag: str) -> ET.Element:
        found = element.find(tag)
        if found is None:
            raise ValueError(f"PIT report entry has no <{tag}>")
        return found

    def text(tag: str) -> str:
        return child(tag).text or ""

    killing = text("killingTests")
    return Mutant(
        source_file=text("sourceFile"),
        mutated_class=text("mutatedClass"),
        mutated_method=text("mutatedMethod"),
        method_description=text("methodDescription"),
        line=int(text("lineNumber")),
        mutator=text("mutator"),
        indexes=tuple(int(i.text) for i in child("indexes").iterfind("index")),
        description=text("description"),
        status=Status(element.get("status")),
        killing_tests=tuple(sorted({_test_name(name) for name in killing.split("|") if name})),
    )


def _test_name(name: str) -> str:
    """PIT's name for a test, as `fully.qualified.TestClass#method`.

    A nested test class is named as Java names it: `pkg.FooTest$Inner#method`.
    A kill credited to a whole test class, because its setup failed under the
    mutant, is the class name alone: `pkg.FooTest`. Parameterized tests and any
    other form have not been seen in a report yet; they raise rather than guess.
    """
    match = _TEST_NAME.fullmatch(name)
    if match is None:
        raise ValueError(f"unrecognized test name in PIT report: {name}")
    test_class = match["cls"] + "".join(
        f"${inner}" for inner in _NESTED_CLASS.findall(match["nested"])
    )
    return f"{test_class}#{match['method']}" if match["method"] else test_class
