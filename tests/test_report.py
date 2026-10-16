import json

from mutagate import report
from mutagate.mutants import Mutant, Status


def test_baseline_round_trip(tmp_path):
    mutants = [
        Mutant(
            "Foo.java",
            "pkg.Foo$1",
            "<init>",
            "()V",
            12,
            "M",
            (4, 9),
            "d",
            Status.KILLED,
            ("pkg.FooTest#adds",),
        ),
        Mutant("Foo.java", "pkg.Foo", "get", "()I", 20, "M", (5,), "d", Status.NO_COVERAGE),
    ]
    setup = {"repo_commit": "abc", "java": "21"}
    path = report.write_baseline("demo", mutants, setup, root=tmp_path)

    assert path == tmp_path / "demo" / "baseline.json"
    saved = json.loads(path.read_text())
    assert saved["repo_commit"] == "abc"
    assert saved["mutants"][0]["status"] == "KILLED"

    loaded = report.read_baseline("demo", root=tmp_path)
    assert loaded == mutants
    assert [(m.status, m.killing_tests) for m in loaded] == [
        (m.status, m.killing_tests) for m in mutants
    ]


def test_exclusions_and_failing_tests(tmp_path):
    (tmp_path / "demo").mkdir()
    (tmp_path / "demo" / "exclusions.json").write_text('{"pkg.Db": "needs a database"}')
    (tmp_path / "demo" / "failing-tests.json").write_text('["pkg.FlakyTest"]')

    assert report.read_exclusions("demo", root=tmp_path) == {"pkg.Db": "needs a database"}
    assert report.read_failing_tests("demo", root=tmp_path) == {"pkg.FlakyTest"}
