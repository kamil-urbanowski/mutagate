package com.gamezone.model;

import com.gamezone.testutil.TestDataFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Hand-written candidate for the gate check on CategoryDiscount.
 * Targets getPercentage (line 37) but fails on every second run, by counting
 * its runs in a file under target/. Expected: NONDETERMINISTIC.
 */
class CategoryDiscountGate5Test {

    private static CategoryDiscount discount(String category) {
        return new CategoryDiscount("P1", "Gate check", LocalDate.of(2026, 1, 1),
            LocalDate.of(2026, 12, 31), 20, category);
    }

    private static Sale saleOf(Product... products) {
        return TestDataFactory.sampleSale("S1", TestDataFactory.sampleCustomer("C1"),
            TestDataFactory.sampleSeller("E1"), List.of(products), LocalDate.of(2026, 6, 1));
    }

    @Test
    void passesOnlyOnOddRuns() throws IOException {
        Path counter = Path.of("target", "gate-check-runs");
        int run = Files.exists(counter) ? Integer.parseInt(Files.readString(counter).trim()) + 1 : 1;
        Files.writeString(counter, Integer.toString(run));

        assertEquals(20.0, discount("CONSOLE").getPercentage());
        assertTrue(run % 2 == 1, "fails on even-numbered runs");
    }
}
