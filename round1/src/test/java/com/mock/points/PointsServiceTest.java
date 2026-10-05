package com.mock.points;

import com.mock.points.entity.MemberEntity;
import com.mock.points.repo.MemberLookup;
import com.mock.points.service.PointsService;
import com.mock.points.service.RentPayment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * ACCEPTANCE CRITERIA for PointsService (unit level).
 *
 * These tests encode the intended business rules. Do not modify the tests —
 * make the implementation satisfy them.
 *
 * Plain JUnit with an in-memory MemberLookup: runs in milliseconds, no Quarkus
 * boot, no database. Run just this class for a fast loop:
 *   mvn test -Dtest=PointsServiceTest
 */
class PointsServiceTest {

    private static final LocalDate DUE = LocalDate.of(2026, 9, 5);
    private static final long SPEC_CAP = 50_000L;

    /** In-memory member store standing in for the repository. */
    private final Map<String, MemberEntity> store = new HashMap<>();
    private final MemberLookup lookup = id -> Optional.ofNullable(store.get(id));
    private final PointsService service = new PointsService(lookup, SPEC_CAP);

    private RentPayment payment(String memberId, long amountCents, LocalDate paid) {
        return new RentPayment("pay_1", memberId, amountCents, paid, DUE);
    }

    private void member(String id, long lifetimePoints) {
        store.put(id, new MemberEntity(id, lifetimePoints));
    }

    // ---------- Base points ----------

    @Test
    @DisplayName("Base: $1,850.75 late payment, BLUE tier -> 1850 points (floor of dollars)")
    void basePointsFloorsToWholeDollars() {
        member("m", 0);
        assertEquals(1850,
                service.calculateForPayment(payment("m", 185_075, DUE.plusDays(3))).totalPoints());
    }

    // ---------- On-time bonus ----------

    @Test
    @DisplayName("On-time: paying BEFORE the due date earns the 25% bonus")
    void earlyPaymentGetsBonus() {
        member("m", 0);
        assertEquals(2500,
                service.calculateForPayment(payment("m", 200_000, DUE.minusDays(2))).totalPoints());
    }

    @Test
    @DisplayName("On-time: paying exactly ON the due date also earns the 25% bonus")
    void onDueDatePaymentGetsBonus() {
        member("m", 0);
        assertEquals(2500,
                service.calculateForPayment(payment("m", 200_000, DUE)).totalPoints());
    }

    @Test
    @DisplayName("Late payment earns no bonus")
    void latePaymentNoBonus() {
        member("m", 0);
        assertEquals(2000,
                service.calculateForPayment(payment("m", 200_000, DUE.plusDays(1))).totalPoints());
    }

    // ---------- Tier multipliers & boundaries ----------

    @Test
    @DisplayName("Tier: exactly 25,000 lifetime points is SILVER (x1.25)")
    void silverBoundaryInclusive() {
        member("m", 25_000);
        assertEquals(1250,
                service.calculateForPayment(payment("m", 100_000, DUE.plusDays(1))).totalPoints());
    }

    @Test
    @DisplayName("Tier: exactly 50,000 lifetime points is GOLD (x1.5)")
    void goldBoundaryInclusive() {
        member("m", 50_000);
        assertEquals(1500,
                service.calculateForPayment(payment("m", 100_000, DUE.plusDays(1))).totalPoints());
    }

    @Test
    @DisplayName("Tier: exactly 100,000 lifetime points is PLATINUM (x2.0)")
    void platinumBoundaryInclusive() {
        member("m", 100_000);
        assertEquals(2000,
                service.calculateForPayment(payment("m", 100_000, DUE.plusDays(1))).totalPoints());
    }

    @Test
    @DisplayName("Tier: 24,999 lifetime points is still BLUE (x1.0)")
    void justBelowSilverIsBlue() {
        member("m", 24_999);
        assertEquals(1000,
                service.calculateForPayment(payment("m", 100_000, DUE.plusDays(1))).totalPoints());
    }

    // ---------- Cap ----------

    @Test
    @DisplayName("Cap: the FINAL award (after tier and bonus) never exceeds 50,000")
    void capAppliesToFinalAward() {
        member("m", 500_000);
        // 30000 base * 2.0 = 60000 -> bonus would be 75000 -> FINAL capped = 50000
        assertEquals(50_000,
                service.calculateForPayment(payment("m", 3_000_000, DUE.minusDays(1))).totalPoints());
    }

    @Test
    @DisplayName("Cap: bonus cannot push a sub-cap award past 50,000")
    void bonusCannotPushPastCap() {
        member("m", 150_000);
        // 22000 * 2.0 = 44000 (under cap); * 1.25 = 55000 -> FINAL capped = 50000
        assertEquals(50_000,
                service.calculateForPayment(payment("m", 2_200_000, DUE.minusDays(1))).totalPoints());
    }

    // ---------- Breakdown ----------

    @Test
    @DisplayName("Breakdown: base and tier-adjusted components are reported correctly")
    void breakdownComponentsAreCorrect() {
        member("m", 25_000);
        var award = service.calculateForPayment(payment("m", 200_000, DUE.plusDays(1)));
        assertEquals(2000, award.basePoints());
        assertEquals(2500, award.tierAdjustedPoints());
        assertEquals(2500, award.totalPoints());
    }

    // ---------- Validation ----------

    @Test
    @DisplayName("Validation: null payment throws IllegalArgumentException, not NPE")
    void nullPaymentThrowsIAE() {
        assertThrows(IllegalArgumentException.class,
                () -> service.calculateForPayment(null));
    }

    @Test
    @DisplayName("Validation: unknown memberId throws IllegalArgumentException, not NPE")
    void unknownMemberThrowsIAE() {
        // note: no member registered in the store
        assertThrows(IllegalArgumentException.class,
                () -> service.calculateForPayment(payment("mem_ghost", 100_000, DUE)));
    }

    @Test
    @DisplayName("Validation: zero or negative amount throws IllegalArgumentException")
    void nonPositiveAmountThrowsIAE() {
        member("m", 0);
        assertThrows(IllegalArgumentException.class,
                () -> service.calculateForPayment(payment("m", 0, DUE)));
        assertThrows(IllegalArgumentException.class,
                () -> service.calculateForPayment(payment("m", -500, DUE)));
    }
}
