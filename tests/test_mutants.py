import pytest

from mutagate.mutants import (
    Mutant,
    Status,
    for_class,
    is_detected,
    mutation_score,
    select_classes,
    select_targets,
)


def mutant(cls="pkg.Foo", line=1, status=Status.KILLED):
    return Mutant("Foo.java", cls, "m", "()V", line, "M", (0,), "d", status)


def test_detected_statuses():
    assert is_detected(mutant(status=Status.KILLED))
    assert not is_detected(mutant(status=Status.SURVIVED))
    assert not is_detected(mutant(status=Status.NO_COVERAGE))


def test_undecided_status_raises():
    with pytest.raises(ValueError, match="TIMED_OUT"):
        is_detected(mutant(status=Status.TIMED_OUT))


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
