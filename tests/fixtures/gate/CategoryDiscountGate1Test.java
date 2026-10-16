package com.gamezone.model;

import com.gamezone.testutil.TestDataFactory;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Hand-written candidate for the gate check on CategoryDiscount.
 * Targets line 81 (console prices added, mutated to subtracted). Kills it. Expected: KEPT.
 */
class CategoryDiscountGate1Test {

    private static CategoryDiscount discount(String category) {
        return new CategoryDiscount("P1", "Gate check", LocalDate.of(2026, 1, 1),
            LocalDate.of(2026, 12, 31), 20, category);
    }

    private static Sale saleOf(Product... products) {
        return TestDataFactory.sampleSale("S1", TestDataFactory.sampleCustomer("C1"),
            TestDataFactory.sampleSeller("E1"), List.of(products), LocalDate.of(2026, 6, 1));
    }

    @Test
    void consoleDiscountIsTwentyPercentOfConsolePrices() {
        Console console = TestDataFactory.sampleConsole("CN1");
        console.setPrice(200000);

        assertEquals(40000.0, discount("CONSOLE").calculateDiscount(saleOf(console)), 0.001);
    }
}
