# ANSWER KEY — open only after both timers end

## Part 1: six planted bugs, across three layers

The design point of v2: three bugs live OUTSIDE `PointsService.java`. A
candidate who only stares at the service fixes the unit tests and stalls —
the remaining reds require navigating config and the repository layer.

### Layer 1 — Configuration

**Bug 1: `points.monthly-cap=5000` in `application.properties`; spec says 50,000.**
The unit test constructs `PointsService` with an explicit 50,000, so it stays
green — only the `@QuarkusTest` end-to-end cap test catches it (whale payment
returns a capped 5,000-range value instead of 50,000... after Bug 5 is fixed;
before that, other wrong numbers).
**Fix:** set `points.monthly-cap=50000`.
**Lesson:** config is code; production bugs live in .properties files too.

### Layer 2 — Repository

**Bug 2: `find("member_id", memberId)` in `MemberRepository` uses the column
naming style, but Panache queries take the ENTITY FIELD name (`memberId`).**
At runtime Hibernate can't resolve `member_id` as an attribute — both
`@QuarkusTest` classes fail on this before anything else in that path runs.
**Fix:** `find("memberId", memberId)` — or better, since `memberId` is the
`@Id`: `findByIdOptional(memberId)`.
**Lesson:** the failure message points at the repository, not the service —
read stack traces before code.

### Layer 3 — Service logic (same three as the classic version)

**Bug 3: tier boundaries exclusive (`>`), spec inclusive (`>=`).**
Fails: the three boundary tests.

**Bug 4: on-time uses strict `isBefore`, so paying ON the due date loses the
bonus; spec says on-or-before.** Fix: `!paymentDate.isAfter(dueDate)`.
Fails: onDueDatePaymentGetsBonus + the silver end-to-end test.

**Bug 5: cap applied BEFORE the bonus; spec caps the FINAL award.** An early
payer can exceed the cap by 25%. Fix: base -> tier -> bonus -> `min(cap)`.
Fails: both cap tests. This is the "money bug" — the one to be able to
explain in fintech terms (order of operations on caps is a real defect class).

**Bug 6: no validation.** Null payment NPEs; unknown member NPEs on
`orElse(null)` deref; non-positive amounts sail through. Spec requires
IllegalArgumentException for all three.
**Fix (in `calculateForPayment` / `calculate`):**
```java
if (payment == null) throw new IllegalArgumentException("payment is required");
if (payment.getAmountCents() <= 0) throw new IllegalArgumentException("amount must be positive");
MemberEntity member = members.findByMemberId(payment.getMemberId())
        .orElseThrow(() -> new IllegalArgumentException(
                "unknown member: " + payment.getMemberId()));
```

### Reference fixed service core

```java
public PointsAward calculate(RentPayment payment, MemberEntity member) {
    long basePoints = payment.getAmountCents() / 100;
    long tierAdjusted = (long) Math.floor(basePoints * tierMultiplier(member));

    boolean onTime = !payment.getPaymentDate().isAfter(payment.getDueDate());
    long withBonus = onTime ? (long) Math.floor(tierAdjusted * 1.25) : tierAdjusted;

    long total = Math.min(withBonus, monthlyCap);
    return new PointsAward(payment.getPaymentId(), payment.getMemberId(),
            basePoints, tierAdjusted, onTime, total);
}

private double tierMultiplier(MemberEntity member) {
    long lifetime = member.lifetimePoints;
    if (lifetime >= 100_000) return 2.0;
    if (lifetime >= 50_000)  return 1.5;
    if (lifetime >= 25_000)  return 1.25;
    return 1.0;
}
```

### Efficient solve order (what strong looks like)

1. `mvn test` — read ALL failures. Notice: unit failures = logic; both Quarkus
   classes failing with a Hibernate attribute error = repository; make a note
   to distrust config too since the cap is injected from it.
2. Fix service logic against `PointsServiceTest` on the fast loop (~15 min).
3. Fix the repository query (~3 min once you read the stack trace).
4. Run full suite; the surviving cap discrepancy leads you to
   `application.properties` (~3 min).
5. Full green, minutes to spare.

## Part 2: expected judgment (updated for the persistent stack)

**Build first — idempotent awarding.** Now with a real answer shape: a
`PointsAwardEntity` (paymentId as `@Id`, memberId, breakdown columns,
awardedAt) + `POST /points/award` that checks-then-inserts in a
`@Transactional` method, returning the existing row on replay. Mention (or
add) a DB uniqueness guarantee via the primary key — that's the real
protection against the webhook-retry race, and saying so out loud is worth
more than any amount of code.

**Build second — history.** `GET /points/history/{memberId}` reading the same
table. Composes for free with the award entity; serves support + mobile.

**Defer with reasons:** referrals (linking mechanism explicitly TBD — stub an
event hook), expiry (no requirements; one design sentence: entries carry
awardedAt so any future policy applies retroactively), rate limiting (gateway
concern, no incident), admin summary (weekly-use nice-to-have; a GROUP BY
query behind an endpoint if — and only if — time remains).

**The trap:** referral bonus is the feature product "wants"; the double-award
is the bug finance is bleeding on. Building shiny before correct is the
failure mode this half is designed to expose.
