package com.mock.points;

import com.mock.points.entity.MemberEntity;
import com.mock.points.repo.MemberLookup;
import com.mock.points.service.PointsAward;
import com.mock.points.service.PointsService;
import com.mock.points.service.RentPayment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pure unit tests for the Round 2 grace-window rule. No Quarkus, no DB:
 * the service is constructed directly with a stub member lookup.
 *
 * SPEC: a payment is on time if paymentDate <= dueDate + 3 days (inclusive).
 */
class GraceWindowTest {

    private static final LocalDate DUE = LocalDate.of(2026, 9, 5);

    private final MemberLookup stub = memberId ->
            Optional.of(new MemberEntity(memberId, 0)); // BLUE, 1.0x

    private final PointsService service = new PointsService(stub, 50_000);

    private PointsAward awardFor(LocalDate paid) {
        return service.calculateForPayment(
                new RentPayment("pay_g", "mem_any", 100_000, paid, DUE));
    }

    @Test
    @DisplayName("Payment ON the due date is on time")
    void onDueDate() {
        assertTrue(awardFor(DUE).onTimeBonusApplied());
    }

    @Test
    @DisplayName("Payment 3 days after the due date is still on time (last day of grace)")
    void lastDayOfGrace() {
        PointsAward award = awardFor(DUE.plusDays(3));
        assertTrue(award.onTimeBonusApplied(), "dueDate + 3 days is inside the grace window");
        assertEquals(1250, award.totalPoints());
    }

    @Test
    @DisplayName("Payment 4 days after the due date is late")
    void dayAfterGrace() {
        PointsAward award = awardFor(DUE.plusDays(4));
        assertFalse(award.onTimeBonusApplied());
        assertEquals(1000, award.totalPoints());
    }
}
