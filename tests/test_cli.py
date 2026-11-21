from pathlib import Path

from mutagate import cli, report
from mutagate.mutants import Mutant, Status

COUNTER = """\
package pkg;

public class Counter {
    private int count;

    public void add(int n) {
        if (n < 0) {
            throw new IllegalArgumentException();
        }
        count += n;
    }

    public void reset() {
        count = 0;
    }

    public int count() {
        return count;
    }
}
"""

SOURCES = {Path("src/main/java/pkg/Counter.java"): COUNTER}


def targets(n: int) -> list[Mutant]:
    return [
        Mutant("Counter.java", "pkg.Counter", "add", "(I)V", 7, "M", (i,), "d", Status.SURVIVED)
        for i in range(n)
    ]


def named(n_targets: int) -> list[str]:
    _, labels = cli._requests(report.RunKind.CONTROL, SOURCES, "pkg.Counter", targets(n_targets))
    return labels


def test_control_names_each_method_once_when_targets_outnumber_methods():
    assert named(5) == ["public void add(int n)", "public void reset()", "public int count()"]


def test_control_makes_no_more_requests_than_targets():
    assert named(2) == ["public void add(int n)", "public void reset()"]
