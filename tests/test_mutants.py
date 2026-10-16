import pytest

from mutagate.mutants import (
    Mutant,
    Status,
    for_class,
    is_detected,
    killed_by,
    mutation_score,
    newly_detected,
    select_classes,
    select_targets,
)


def mutant(cls="pkg.Foo", line=1, status=Status.KILLED):
    return Mutant("Foo.java", cls, "m", "()V", line, "M", (0,), "d", status)


@pytest.mark.parametrize(
    "status",
    [
        Status.KILLED,
        Status.TIMED_OUT,
        Status.MEMORY_ERROR,
        Status.RUN_ERROR,
        Status.NON_VIABLE,
        Status.EQUIVALENT,
    ],
)
def test_detected_statuses(status):
    assert is_detected(mutant(status=status))


@pytest.mark.parametrize("status", [Status.SURVIVED, Status.NO_COVERAGE])
def test_undetected_statuses(status):
    assert not is_detected(mutant(status=status))


@pytest.mark.parametrize("status", [Status.NOT_STARTED, Status.STARTED])
def test_unfinished_status_raises(status):
    with pytest.raises(ValueError, match="incomplete"):
        is_detected(mutant(status=status))


def test_mutation_score():
    ms = [
        mutant(line=1),
        mutant(line=2, status=Status.SURVIVED),
        mutant(line=3, status=Status.NO_COVERAGE),
        mutant(line=4),
    ]
    assert mutation_score(ms) == 0.5
    with pytest.raises(ValueError):
        mutation_score([])


def test_nested_classes_belong_to_outer_class():
    ms = [mutant("pkg.Foo"), mutant("pkg.Foo$1"), mutant("pkg.Foo$Inner"), mutant("pkg.FooBar")]
    assert [m.mutated_class for m in for_class(ms, "pkg.Foo")] == [
        "pkg.Foo",
        "pkg.Foo$1",
        "pkg.Foo$Inner",
    ]


def test_select_classes_and_targets():
    ms = [
        mutant("pkg.A"),
        mutant("pkg.B$1", status=Status.SURVIVED),
        mutant("pkg.C", status=Status.NO_COVERAGE),
        mutant("pkg.D", status=Status.SURVIVED),
    ]
    assert select_classes(ms, {"pkg.D"}) == ["pkg.B", "pkg.C"]
    assert select_targets(ms, "pkg.B") == [ms[1]]
    assert select_targets(ms, "pkg.A") == []


def killed(line, *tests, status=Status.KILLED):
    return Mutant("Foo.java", "pkg.Foo", "m", "()V", line, "M", (0,), "d", status, tests)


def test_killed_by_forms():
    m = killed(1, "pkg.ATest#x", "pkg.BTest$Inner#y", "pkg.CTest")
    assert killed_by(m, "pkg.ATest")
    assert killed_by(m, "pkg.BTest")
    assert killed_by(m, "pkg.CTest")
    assert not killed_by(m, "pkg.A")
    assert not killed_by(killed(1, "pkg.ATest2#x"), "pkg.ATest")


def test_newly_detected_counts_only_kept_kills_of_undetected_mutants():
    baseline = [
        mutant(line=1, status=Status.SURVIVED),
        mutant(line=2, status=Status.NO_COVERAGE),
        mutant(line=3, status=Status.SURVIVED),
        mutant(line=4, status=Status.KILLED),
    ]
    verification = [
        killed(1, "pkg.KeptTest#x"),
        killed(2, "pkg.RejectedTest#x"),
        mutant(line=3, status=Status.SURVIVED),
        killed(4, "pkg.FooTest#old", "pkg.KeptTest#x"),
    ]
    assert newly_detected(baseline, verification, {"pkg.KeptTest"}) == [verification[0]]
