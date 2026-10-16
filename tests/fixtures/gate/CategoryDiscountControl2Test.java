package com.gamezone.model;

import com.gamezone.testutil.TestDataFactory;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Hand-written candidate for the gate check on CategoryDiscount.
 * Control candidate (no target). Repeats what the existing suite already checks:
 * a video game discount. Expected: NO_NEW_KILL.
 */
class CategoryDiscountControl2Test {

    private static CategoryDiscount discount(String category) {
        return new CategoryDiscount("P1", "Gate check", LocalDate.of(2026, 1, 1),
            LocalDate.of(2026, 12, 31), 20, category);
    }

    private static Sale saleOf(Product... products) {
        return TestDataFactory.sampleSale("S1", TestDataFactory.sampleCustomer("C1"),
            TestDataFactory.sampleSeller("E1"), List.of(products), LocalDate.of(2026, 6, 1));
    }

    @Test
    void videoGameDiscountIsTwentyPercent() {
        VideoGame game = TestDataFactory.sampleVideoGame("VG1");
        game.setPrice(50000);

        assertEquals(10000.0, discount("VIDEOGAME").calculateDiscount(saleOf(game)), 0.001);
    }
}
