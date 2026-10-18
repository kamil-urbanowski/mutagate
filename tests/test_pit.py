import tempfile
import xml.etree.ElementTree as ET

import pytest

from mutagate import pit
from mutagate.mutants import Status

POM = """<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <!-- a comment -->
  <groupId>com.example</groupId>
  <artifactId>demo</artifactId>
  <version>1.0</version>
  {build}
</project>
"""

NS = {"m": "http://maven.apache.org/POM/4.0.0"}

REPORT = """<?xml version="1.0" encoding="UTF-8"?>
<mutations partial="true">
<mutation detected='true' status='KILLED' numberOfTestsRun='2'>
  <sourceFile>Foo.java</sourceFile>
  <mutatedClass>pkg.Foo$1</mutatedClass>
  <mutatedMethod>&lt;init&gt;</mutatedMethod>
  <methodDescription>()V</methodDescription>
  <lineNumber>12</lineNumber>
  <mutator>org.pitest.Math</mutator>
  <indexes><index>4</index><index>9</index></indexes>
  <blocks><block>0</block></blocks>
  <killingTests>pkg.FooTest.[engine:junit-jupiter]/[class:pkg.FooTest]/[method:adds()]|pkg.BarTest.[engine:junit-jupiter]/[class:pkg.BarTest]/[method:subtracts(org.junit.jupiter.api.TestInfo)]</killingTests>
  <succeedingTests></succeedingTests>
  <coveringTests></coveringTests>
  <description>Replaced integer addition with subtraction</description>
</mutation>
<mutation detected='false' status='NO_COVERAGE' numberOfTestsRun='0'>
  <sourceFile>Foo.java</sourceFile>
  <mutatedClass>pkg.Foo</mutatedClass>
  <mutatedMethod>get</mutatedMethod>
  <methodDescription>()I</methodDescription>
  <lineNumber>20</lineNumber>
  <mutator>org.pitest.PrimitiveReturns</mutator>
  <indexes><index>5</index></indexes>
  <blocks><block>0</block></blocks>
  <killingTests/>
  <succeedingTests/>
  <coveringTests/>
  <description>replaced int return with 0</description>
</mutation>
</mutations>
"""


def plugins_in(pom):
    return ET.parse(pom).getroot().findall("m:build/m:plugins/m:plugin", NS)


def test_add_pit_plugin_creates_build_section(tmp_path):
    pom = tmp_path / "pom.xml"
    pom.write_text(POM.format(build=""))
    pit.add_pit_plugin(pom)

    (plugin,) = plugins_in(pom)
    assert plugin.findtext("m:artifactId", namespaces=NS) == "pitest-maven"
    assert plugin.findtext("m:version", namespaces=NS) == pit.PITEST_MAVEN_VERSION
    dependency = plugin.find("m:dependencies/m:dependency", NS)
    assert dependency.findtext("m:artifactId", namespaces=NS) == "pitest-junit5-plugin"
    assert dependency.findtext("m:version", namespaces=NS) == pit.PITEST_JUNIT5_PLUGIN_VERSION
    text = pom.read_text()
    assert "ns0:" not in text and "<!-- a comment -->" in text


def test_add_pit_plugin_keeps_existing_plugins(tmp_path):
    pom = tmp_path / "pom.xml"
    pom.write_text(
        POM.format(
            build="""<build><plugins><plugin>
      <artifactId>maven-surefire-plugin</artifactId></plugin></plugins></build>"""
        )
    )
    pit.add_pit_plugin(pom)

    artifacts = [p.findtext("m:artifactId", namespaces=NS) for p in plugins_in(pom)]
    assert artifacts == ["maven-surefire-plugin", "pitest-maven"]


def test_add_pit_plugin_refuses_existing_pitest(tmp_path):
    pom = tmp_path / "pom.xml"
    pom.write_text(
        POM.format(
            build="""<build><plugins><plugin>
      <groupId>org.pitest</groupId><artifactId>pitest-maven</artifactId>
      </plugin></plugins></build>"""
        )
    )
    with pytest.raises(pit.PitError):
        pit.add_pit_plugin(pom)


def test_workspace_is_a_copy_without_build_output(tmp_path, monkeypatch):
    monkeypatch.setattr(pit, "check_toolchain", lambda cwd: ("3.9.16", "21.0.12.1"))
    repo = tmp_path / "demo"
    (repo / "target").mkdir(parents=True)
    (repo / "target" / "stale.class").write_text("")
    source = repo / "src" / "main" / "java" / "pkg" / "target"
    source.mkdir(parents=True)
    (source / "Foo.java").write_text("package pkg.target; class Foo {}")
    (repo / "pom.xml").write_text(POM.format(build=""))

    workspace = pit.make_workspace(repo)
    try:
        assert workspace.name == "demo"
        assert workspace == workspace.resolve()
        assert not (workspace / "target").exists()
        assert (workspace / "src/main/java/pkg/target/Foo.java").exists()
        assert len(plugins_in(workspace / "pom.xml")) == 1
        assert plugins_in(repo / "pom.xml") == []
        assert list(pit.read_sources(workspace)) == [
            workspace.joinpath("src/main/java/pkg/target/Foo.java").relative_to(workspace)
        ]
    finally:
        pit.remove_workspace(workspace)
    assert not workspace.exists()


def test_failed_workspace_leaves_nothing_behind(tmp_path, monkeypatch):
    monkeypatch.setattr(pit, "check_toolchain", lambda cwd: ("3.9.16", "21.0.12.1"))
    scratch = tmp_path / "tmp"
    scratch.mkdir()
    monkeypatch.setattr(tempfile, "tempdir", str(scratch))
    repo = tmp_path / "demo"
    repo.mkdir()
    (repo / "pom.xml").write_text(
        POM.format(
            build="""<build><plugins><plugin>
      <groupId>org.pitest</groupId><artifactId>pitest-maven</artifactId>
      </plugin></plugins></build>"""
        )
    )

    with pytest.raises(pit.PitError):
        pit.make_workspace(repo)
    assert list(scratch.iterdir()) == []


def test_parse_report(tmp_path):
    (tmp_path / pit.REPORT_FILE).write_text(REPORT)
    killed, uncovered = pit.parse_report(tmp_path)

    assert killed.mutated_class == "pkg.Foo$1"
    assert killed.mutated_method == "<init>"
    assert killed.line == 12
    assert killed.indexes == (4, 9)
    assert killed.status is Status.KILLED
    assert killed.killing_tests == ("pkg.BarTest#subtracts", "pkg.FooTest#adds")

    assert uncovered.status is Status.NO_COVERAGE
    assert uncovered.indexes == (5,)
    assert uncovered.killing_tests == ()


@pytest.mark.parametrize(
    "name, expected",
    [
        (
            "pkg.FooTest.[engine:junit-jupiter]/[class:pkg.FooTest]/[method:adds()]",
            "pkg.FooTest#adds",
        ),
        (
            (
                "pkg.FooTest.[engine:junit-jupiter]/[class:pkg.FooTest]/[nested-class:Inner]"
                "/[method:saves(java.nio.file.Path)]"
            ),
            "pkg.FooTest$Inner#saves",
        ),
        (
            (
                "pkg.FooTest.[engine:junit-jupiter]/[class:pkg.FooTest]/[nested-class:A]"
                "/[nested-class:B]/[method:x()]"
            ),
            "pkg.FooTest$A$B#x",
        ),
        ("pkg.FooTest.[engine:junit-jupiter]/[class:pkg.FooTest]", "pkg.FooTest"),
    ],
)
def test_test_names(name, expected):
    assert pit._test_name(name) == expected


def test_duplicate_killing_tests_are_recorded_once(tmp_path):
    whole = "pkg.FooTest.[engine:junit-jupiter]/[class:pkg.FooTest]"
    (tmp_path / pit.REPORT_FILE).write_text(
        REPORT.replace("<killingTests>pkg.FooTest.", f"<killingTests>{whole}|{whole}|pkg.FooTest.")
    )
    killed, _ = pit.parse_report(tmp_path)
    assert killed.killing_tests == ("pkg.BarTest#subtracts", "pkg.FooTest", "pkg.FooTest#adds")


def test_unrecognized_test_name_raises(tmp_path):
    parameterized = (
        "pkg.FooTest.[engine:junit-jupiter]/[class:pkg.FooTest]"
        "/[test-template:x(int)]/[test-template-invocation:#1]"
    )
    (tmp_path / pit.REPORT_FILE).write_text(
        REPORT.replace(
            "pkg.FooTest.[engine:junit-jupiter]/[class:pkg.FooTest]/[method:adds()]", parameterized
        )
    )
    with pytest.raises(ValueError, match="unrecognized test name"):
        pit.parse_report(tmp_path)


@pytest.mark.parametrize("missing", ["<killingTests/>", "<indexes><index>5</index></indexes>"])
def test_missing_element_raises(tmp_path, missing):
    (tmp_path / pit.REPORT_FILE).write_text(REPORT.replace(missing, ""))
    with pytest.raises(ValueError, match="has no <"):
        pit.parse_report(tmp_path)


def test_unfinished_mutant_fails_the_run(tmp_path):
    (tmp_path / pit.REPORT_FILE).write_text(
        REPORT.replace("status='NO_COVERAGE'", "status='STARTED'")
    )
    with pytest.raises(pit.PitError, match="1 of 2 mutants unfinished"):
        pit.parse_report(tmp_path)


MVN_V = """Apache Maven 3.9.16 (2bdd9fddda4b155ebf8000e807eb73fd829a51d5)
Maven home: /opt/homebrew/Cellar/maven/3.9.16/libexec
Java version: {java}, vendor: Homebrew, runtime: /opt/homebrew/Cellar/openjdk/libexec
"""


@pytest.mark.parametrize("java", ["21.0.12.1", "21.0.13", "21"])
def test_toolchain_accepts_java_21(monkeypatch, tmp_path, java):
    monkeypatch.setattr(pit, "_run", lambda cmd, cwd: (True, MVN_V.format(java=java)))
    assert pit.check_toolchain(tmp_path) == ("3.9.16", java)


@pytest.mark.parametrize("java", ["27.0.1", "17.0.9", "1.8.0_392", "unknown"])
def test_toolchain_refuses_other_java(monkeypatch, tmp_path, java):
    monkeypatch.setattr(pit, "_run", lambda cmd, cwd: (True, MVN_V.format(java=java)))
    with pytest.raises(pit.PitError, match="requires Java 21"):
        pit.check_toolchain(tmp_path)


def test_toolchain_refuses_unreadable_output(monkeypatch, tmp_path):
    monkeypatch.setattr(pit, "_run", lambda cmd, cwd: (False, "mvn: command not found"))
    with pytest.raises(pit.PitError, match="could not read"):
        pit.check_toolchain(tmp_path)


def test_build_output_names_files_from_the_repo_root(monkeypatch, tmp_path):
    workspace = tmp_path / "demo"
    workspace.mkdir()
    real = str(workspace.resolve())
    output = (
        f"[ERROR] {real}/src/test/java/pkg/FooTest.java:[9,16] cannot find symbol\n"
        f"[ERROR] Please refer to {real}/target/surefire-reports\n"
        f"[INFO] Building in {real}\n"
    )
    monkeypatch.setattr(pit, "_run", lambda cmd, cwd: (False, output))
    ok, cleaned = pit.compile_tests(workspace)
    assert not ok
    assert cleaned.splitlines() == [
        "[ERROR] src/test/java/pkg/FooTest.java:[9,16] cannot find symbol",
        "[ERROR] Please refer to target/surefire-reports",
        "[INFO] Building in .",
    ]
