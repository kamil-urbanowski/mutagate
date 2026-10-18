from pathlib import Path

import pytest

from mutagate.context import build_context, enclosing_method, signatures
from mutagate.mutants import Mutant, Status

SHOP = """\
package pkg;

import java.util.List;
import pkg.util.Money;

public class Shop {
    private final List<Item> items;
    private int visits = 0;

    public Shop(List<Item> items) {
        this.items = items;
    }

    public long total() {
        return items.stream()
            .filter(i -> i.price() > 0)
            .mapToLong(Item::price)
            .sum();
    }

    Runnable counter() {
        return new Runnable() {
            @Override
            public void run() {
                visits++;
            }
        };
    }

    static class Ledger {
        int balance(int a, int b) {
            return a - b;
        }
    }
}
"""

ITEM = """\
package pkg;

public record Item(String name, long price) {
    public Item {
        if (price < 0) throw new IllegalArgumentException();
    }

    private static String secret() {
        return "x";
    }

    public String label() {
        return name + ":" + price;
    }
}
"""

MONEY = """\
package pkg.util;

/** Amounts in cents. */
public final class Money {
    public static final long ZERO = 0L;
    private final long cents;

    public Money(long cents) {
        this.cents = cents;
    }

    // a comment
    public long cents() {
        return cents;
    }

    static {
        System.out.println("loaded");
    }

    public enum Currency { USD, EUR }
}
"""

SOURCES = {
    Path("src/main/java/pkg/Shop.java"): SHOP,
    Path("src/main/java/pkg/Item.java"): ITEM,
    Path("src/main/java/pkg/util/Money.java"): MONEY,
    Path("src/main/java/pkg/Unused.java"): "package pkg;\n\nclass Unused {}\n",
    Path("src/main/java/pkg/package-info.java"): "package pkg;\n",
    Path("src/test/java/pkg/ShopTest.java"): "package pkg;\n\nclass ShopTest { Shop s; }\n",
    Path("src/test/java/pkg/ItemTest.java"): "package pkg;\n\nclass ItemTest { Item i; }\n",
    Path("src/test/java/pkg/ShopperTest.java"): "package pkg;\n\nclass ShopperTest {}\n",
    Path("src/test/java/other/Fixtures.java"): "package other;\n\nclass Fixtures { pkg.Shop s; }\n",
}


def mutant(line, cls="pkg.Shop", method="m", source_file="Shop.java"):
    return Mutant(source_file, cls, method, "()V", line, "M", (0,), "d", Status.SURVIVED)


# --------------------------------------------------------------------------
# The method the mutant is in
# --------------------------------------------------------------------------


def test_constructor_found_by_line():
    assert enclosing_method(SHOP, 11) == (
        "public Shop(List<Item> items) {\n    this.items = items;\n}"
    )


def test_lambda_gets_its_enclosing_method():
    assert enclosing_method(SHOP, 16).startswith("public long total() {")


def test_anonymous_class_gets_the_method_that_contains_it():
    assert enclosing_method(SHOP, 25).startswith("Runnable counter() {")


def test_nested_class_method_is_its_own_method():
    assert enclosing_method(SHOP, 32) == "int balance(int a, int b) {\n    return a - b;\n}"


def test_line_in_no_method_raises():
    with pytest.raises(ValueError, match="no method or constructor"):
        enclosing_method(SHOP, 8)


# --------------------------------------------------------------------------
# Signatures
# --------------------------------------------------------------------------


def test_signatures_keep_declarations_only():
    assert signatures(MONEY) == (
        "package pkg.util;\n"
        "public final class Money {\n"
        "  public static final long ZERO = 0L;\n"
        "  public Money(long cents);\n"
        "  public long cents();\n"
        "  public enum Currency {\n"
        "    USD,\n"
        "    EUR,\n"
        "  }\n"
        "}"
    )


def test_signatures_of_a_record_leave_out_private_members():
    assert signatures(ITEM) == (
        "package pkg;\n"
        "public record Item(String name, long price) {\n"
        "  public Item;\n"
        "  public String label();\n"
        "}"
    )


def test_signature_spanning_lines_keeps_its_shape():
    wide = (
        "package pkg;\n\npublic class Wide {\n    @Deprecated\n"
        "    public Wide(int a,\n                int b) {\n    }\n}\n"
    )
    assert signatures(wide) == (
        "package pkg;\n"
        "public class Wide {\n"
        "  @Deprecated\n"
        "  public Wide(int a,\n"
        "              int b);\n"
        "}"
    )


# --------------------------------------------------------------------------
# The whole context
# --------------------------------------------------------------------------


def test_targeted_context():
    context = build_context(SOURCES, "pkg.Shop", mutant(16, method="lambda$total$0"))
    assert context.class_source == SHOP
    assert context.method_source.startswith("public long total() {")
    # Item and Money are named in Shop.java; Unused is not, and Shop is itself.
    assert context.collaborator_signatures == [signatures(ITEM), signatures(MONEY)]
    # Whole-word mentions only: ShopperTest does not mention Shop.
    assert context.existing_tests == [
        SOURCES[Path("src/test/java/other/Fixtures.java")],
        SOURCES[Path("src/test/java/pkg/ShopTest.java")],
    ]
    assert context.test_package == "pkg"
    assert context.test_dir == Path("src/test/java/pkg")


def test_mutant_in_an_anonymous_class_belongs_to_the_outer_class():
    context = build_context(SOURCES, "pkg.Shop", mutant(25, cls="pkg.Shop$1", method="run"))
    assert context.method_source.startswith("Runnable counter() {")


def test_control_context_has_no_mutant_or_method():
    targeted = build_context(SOURCES, "pkg.Shop", mutant(16))
    control = build_context(SOURCES, "pkg.Shop")
    assert control.mutant is None
    assert control.method_source is None
    assert control.collaborator_signatures == targeted.collaborator_signatures
    assert control.existing_tests == targeted.existing_tests


def test_test_dir_follows_a_nested_package():
    context = build_context(SOURCES, "pkg.util.Money")
    assert context.test_package == "pkg.util"
    assert context.test_dir == Path("src/test/java/pkg/util")
    assert context.collaborator_signatures == []
    assert context.existing_tests == []


@pytest.mark.parametrize(
    "class_name, m, message",
    [
        ("pkg.Missing", None, "not found"),
        ("Shop", None, "not in a package"),
        ("pkg.Shop", mutant(16, cls="pkg.Item"), "not part of pkg.Shop"),
        ("pkg.Shop", mutant(16, source_file="Other.java"), "not Shop.java"),
    ],
)
def test_bad_requests_raise(class_name, m, message):
    with pytest.raises(ValueError, match=message):
        build_context(SOURCES, class_name, m)
