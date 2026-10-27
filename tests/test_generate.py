from pathlib import Path

import pytest

from mutagate import generate
from mutagate.context import MutantContext
from mutagate.gate import Candidate, GateResult, Verdict
from mutagate.mutants import Mutant, Status
from mutagate.providers.base import ProviderError

NAME = "pkg.ShopMutaGate1Test"

CLASS_SOURCE = """\
package pkg;

public class Shop {
    public long total(long a, long b) {
        return a + b;
    }
}
"""

TEST = """\
package pkg;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ShopMutaGate1Test {
    @Test
    void addsBothAmounts() {
        assertEquals(5, new Shop().total(2, 3));
    }

    private static long helper() {
        return 1;
    }

    static class Fixture {}
}
"""


def reply(source=TEST, before="Here is the test.\n\n", after="\n"):
    return f"{before}```java\n{source}```{after}"


def target(status=Status.SURVIVED):
    return Mutant(
        "Shop.java",
        "pkg.Shop",
        "total",
        "(JJ)J",
        5,
        "M",
        (0,),
        "Replaced long addition with subtraction",
        status,
    )


def context(mutant=None):
    return MutantContext(
        mutant=target() if mutant is None else mutant,
        class_name="pkg.Shop",
        class_source=CLASS_SOURCE,
        method_source="public long total(long a, long b) {\n    return a + b;\n}",
        collaborator_signatures=["package pkg;\nclass Item {\n  long price();\n}"],
        existing_tests=["package pkg;\n\nclass ShopTest {}\n"],
        test_package="pkg",
        test_dir=Path("src/test/java/pkg"),
    )


class FakeProvider:
    name = "fake"

    def __init__(self, *replies):
        self.replies = list(replies)
        self.prompts = []

    def complete(self, prompt):
        self.prompts.append(prompt)
        return self.replies.pop(0)


# --------------------------------------------------------------------------
# Names
# --------------------------------------------------------------------------


def test_test_class_name():
    assert generate.test_class_name("pkg.Shop", 3) == "pkg.ShopMutaGate3Test"
    with pytest.raises(ValueError):
        generate.test_class_name("pkg.Shop", 0)


# --------------------------------------------------------------------------
# Usable replies
# --------------------------------------------------------------------------


def test_usable_reply():
    assert generate.extract_test(reply(), NAME) == TEST


@pytest.mark.parametrize(
    "text",
    [
        TEST.replace("@Test", "@org.junit.jupiter.api.Test"),
        TEST.replace("    @Test", "    // not a @Test\n    @Test").replace(
            "5,", '"@Test".length() + 0,'
        ),
        TEST.replace(
            "    @Test\n    void addsBothAmounts() {\n        assertEquals(5, new Shop().total(2, 3));\n    }",
            "    @Nested\n    class Inner {\n        @Test\n        void adds() {}\n    }",
        ),
    ],
    ids=["qualified annotation", "@Test in a comment and a string", "test in a nested class"],
)
def test_usable_variants(text):
    assert generate.extract_test(reply(text), NAME) == text


def test_windows_line_endings():
    assert generate.extract_test(reply().replace("\n", "\r\n"), NAME) == TEST


def test_longer_fence_and_no_language():
    assert generate.extract_test(f"````\n{TEST}````", NAME) == TEST


@pytest.mark.parametrize(
    "text",
    [
        TEST,  # no code block at all
        reply() + reply(),  # two code blocks
        reply(TEST.replace("package pkg;", "package other;")),
        reply(TEST.replace("package pkg;\n", "")),
        reply(TEST.replace("class ShopMutaGate1Test", "class ShopTest")),
        reply(TEST + "\nclass Helper {}\n"),
        reply(TEST.replace("@Test\n", "")),
        reply(TEST.replace("private static long helper()", "@Test\n    void second()")),
        reply(TEST.replace("private static long helper()", "@ParameterizedTest\n    long p()")),
        reply(TEST.replace("@Test", "@RepeatedTest(3)")),
        reply(TEST.replace("private static long helper()", "@TestFactory\n    long f()")),
    ],
    ids=[
        "no block",
        "two blocks",
        "other package",
        "no package",
        "other class name",
        "second top-level class",
        "no test",
        "two tests",
        "parameterized",
        "repeated",
        "test factory",
    ],
)
def test_unusable_replies(text):
    assert generate.extract_test(text, NAME) is None


# --------------------------------------------------------------------------
# The request
# --------------------------------------------------------------------------


def test_prompt_puts_shared_parts_first_and_the_target_last():
    prompt = generate.build_prompt(context(), NAME)
    assert prompt.startswith(generate.RULES)
    order = [
        "## The class under test: pkg.Shop",
        CLASS_SOURCE.rstrip(),
        "class Item {\n  long price();\n}",
        "## Existing tests that mention Shop",
        "class ShopTest {}",
        "## Your task",
        "on line 5, and the existing tests run this code, but none of them failed",
        "PIT's description of the change: Replaced long addition with subtraction",
        ">    5 |         return a + b;",
        "The line is in this method:",
        "Package: pkg\nClass name: ShopMutaGate1Test",
    ]
    positions = [prompt.index(part) for part in order]
    assert positions == sorted(positions)


def test_target_line_is_marked_among_repeats():
    ctx = context()
    ctx.class_source = "class A {\n  void f() {\n    x++;\n    x++;\n    x++;\n  }\n}\n"
    ctx.mutant = Mutant("A.java", "pkg.A", "f", "()V", 4, "M", (0,), "d", Status.SURVIVED)
    prompt = generate.build_prompt(ctx, NAME)
    assert (
        "     1 | class A {\n"
        "     2 |   void f() {\n"
        "     3 |     x++;\n"
        ">    4 |     x++;\n"
        "     5 |     x++;\n"
        "     6 |   }\n"
        "     7 | }\n"
    ) in prompt


def test_prompt_explains_an_uncovered_target():
    prompt = generate.build_prompt(context(target(Status.NO_COVERAGE)), NAME)
    assert "and no existing test runs this code." in prompt


def test_prompt_without_collaborators_or_tests_says_so():
    ctx = context()
    ctx.collaborator_signatures, ctx.existing_tests = [], []
    prompt = generate.build_prompt(ctx, NAME)
    assert "None in this project." in prompt
    assert "## Existing tests that mention Shop\n\nNone." in prompt


def test_code_with_backticks_gets_a_longer_fence():
    ctx = context()
    ctx.class_source = CLASS_SOURCE.replace("public class", "/** ```x``` */\npublic class")
    assert "````java\n" in generate.build_prompt(ctx, NAME)


def test_bad_requests_raise():
    with pytest.raises(ValueError, match="undetected"):
        generate.build_prompt(context(target(Status.KILLED)), NAME)
    with pytest.raises(ValueError, match="package"):
        generate.build_prompt(context(), "other.ShopMutaGate1Test")
    with pytest.raises(ValueError, match="package"):
        generate.build_prompt(control(), "other.ShopMutaGate1Test")
    with pytest.raises(ValueError, match="names a method"):
        generate.build_prompt(control(method=None), NAME)


METHOD = "public long total(long a, long b) {\n    return a + b;\n}"


def control(method=METHOD):
    ctx = context()
    ctx.mutant, ctx.method_source = None, method
    return ctx


def test_control_request_differs_only_in_its_task():
    targeted = generate.build_prompt(context(), NAME)
    shared = targeted[: targeted.index("## Your task")]
    prompt = generate.build_prompt(control(), NAME)
    assert prompt == shared + (
        "## Your task\n\n"
        "Write a test for this method of Shop:\n\n"
        f"```java\n{METHOD}\n```\n\n"
        "Test behavior of this method that the existing tests above do not already\n"
        "check. The test must pass against the code as it is now.\n\n"
        "Package: pkg\nClass name: ShopMutaGate1Test"
    )
    assert "PIT" not in prompt  # nothing from PIT: no excerpt, line, or change


# --------------------------------------------------------------------------
# Generating and repairing
# --------------------------------------------------------------------------


def test_generate_wraps_the_reply_as_a_candidate():
    provider = FakeProvider(reply())
    candidate = generate.generate_test(context(), provider, NAME)
    assert candidate == Candidate(
        NAME, Path("src/test/java/pkg/ShopMutaGate1Test.java"), TEST, target(), 1
    )
    assert provider.prompts == [generate.build_prompt(context(), NAME)]


def test_control_candidate_has_no_target():
    candidate = generate.generate_test(control(), FakeProvider(reply()), NAME)
    assert candidate == Candidate(
        NAME, Path("src/test/java/pkg/ShopMutaGate1Test.java"), TEST, None, 1
    )


def test_unusable_reply_gives_a_candidate_without_source():
    candidate = generate.generate_test(context(), FakeProvider("I cannot help."), NAME)
    assert candidate.source is None
    assert candidate.attempts == 1


def test_provider_error_is_not_caught():
    class Failing:
        name = "failing"

        def complete(self, prompt):
            raise ProviderError("unreachable")

    with pytest.raises(ProviderError):
        generate.generate_test(context(), Failing(), NAME)


def first(source=TEST):
    return Candidate(NAME, Path("src/test/java/pkg/ShopMutaGate1Test.java"), source, target())


def test_repair_shows_the_failed_test_and_the_error():
    failed = first(TEST.replace("total(2, 3)", "sum(2, 3)"))
    failure = GateResult(failed, Verdict.DID_NOT_COMPILE, "cannot find symbol: method sum")
    provider = FakeProvider(reply())
    repaired = generate.repair(context(), provider, failed, failure)

    assert repaired == Candidate(NAME, failed.path, TEST, target(), 2)
    (prompt,) = provider.prompts
    assert prompt.startswith(generate.build_prompt(context(), NAME))
    assert "## Your previous attempt" in prompt
    assert "new Shop().sum(2, 3)" in prompt
    assert "It did not compile." in prompt
    assert "cannot find symbol: method sum" in prompt


def test_control_repair_follows_the_control_request():
    failed = Candidate(NAME, Path("src/test/java/pkg/ShopMutaGate1Test.java"), TEST, None)
    failure = GateResult(failed, Verdict.FAILED_ON_UNMODIFIED_CODE, "expected: <5> but was: <6>")
    provider = FakeProvider(reply())
    repaired = generate.repair(control(), provider, failed, failure)

    assert repaired == Candidate(NAME, failed.path, TEST, None, 2)
    (prompt,) = provider.prompts
    assert prompt.startswith(
        generate.build_prompt(control(), NAME) + "\n\n## Your previous attempt"
    )
    assert "It failed against the code as it is now." in prompt


def test_repair_after_no_usable_test_repeats_the_rules():
    failed = first(None)
    failure = GateResult(failed, Verdict.NO_USABLE_TEST, "the reply held no usable test")
    provider = FakeProvider(reply())
    generate.repair(context(), provider, failed, failure)

    (prompt,) = provider.prompts
    assert "Your previous reply held no usable test." in prompt
    assert "exactly one method annotated with @Test" in prompt
    assert "## Your previous attempt" not in prompt


@pytest.mark.parametrize(
    "candidate, verdict, message",
    [
        (first(), Verdict.MISSED_TARGET, "only a screen failure"),
        (first(), Verdict.KEPT, "only a screen failure"),
        (Candidate(NAME, Path("x"), TEST, target(), 2), Verdict.DID_NOT_COMPILE, "already"),
    ],
)
def test_repair_refuses_anything_but_one_screen_failure(candidate, verdict, message):
    with pytest.raises(ValueError, match=message):
        generate.repair(context(), FakeProvider(reply()), candidate, GateResult(candidate, verdict))


def test_repair_refuses_a_verdict_on_another_candidate():
    other = Candidate("pkg.ShopMutaGate2Test", Path("y"), TEST, target())
    with pytest.raises(ValueError, match="verdict is on"):
        generate.repair(
            context(), FakeProvider(reply()), first(), GateResult(other, Verdict.DID_NOT_COMPILE)
        )
