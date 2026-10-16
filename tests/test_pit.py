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


def test_workspace_is_a_copy_without_build_output(tmp_path):
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


def test_unrecognized_test_name_raises(tmp_path):
    nested = (
        "pkg.FooTest.[engine:junit-jupiter]/[class:pkg.FooTest]/[nested-class:Inner]/[method:x()]"
    )
    (tmp_path / pit.REPORT_FILE).write_text(
        REPORT.replace(
            "pkg.FooTest.[engine:junit-jupiter]/[class:pkg.FooTest]/[method:adds()]", nested
        )
    )
    with pytest.raises(ValueError, match="unrecognized test name"):
        pit.parse_report(tmp_path)
