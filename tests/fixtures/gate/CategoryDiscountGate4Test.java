package com.gamezone.model;

import com.gamezone.testutil.TestDataFactory;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Hand-written candidate for the gate check on CategoryDiscount.
 * Targets getPercentage (line 37) but expects the wrong value.
 * Expected: FAILED_ON_UNMODIFIED_CODE.
 */
class CategoryDiscountGate4Test {

    private static CategoryDiscount discount(String category) {
        return new CategoryDiscount("P1", "Gate check", LocalDate.of(2026, 1, 1),
            LocalDate.of(2026, 12, 31), 20, category);
    }

    private static Sale saleOf(Product... products) {
        return TestDataFactory.sampleSale("S1", TestDataFactory.sampleCustomer("C1"),
            TestDataFactory.sampleSeller("E1"), List.of(products), LocalDate.of(2026, 6, 1));
    }

    @Test
    void expectsTheWrongPercentage() {
        assertEquals(25.0, discount("CONSOLE").getPercentage());
    }
}
