# MOCK ANSWERS — both rounds. Open only after your timers end.

# ROUND 1 (6 bugs) — condensed
Full version with rationale lives in round1/ANSWER_KEY_ROUND1.md.
1. **Config:** `points.monthly-cap=5000` -> `50000` (unit tests can't catch it; only e2e).
2. **Repository:** `find("member_id", id)` -> field name `find("memberId", id)` or `findByIdOptional(id)` — Panache queries take ENTITY FIELD names.
3. **Tier boundaries:** `>` -> `>=` (spec inclusive).
4. **On-time:** `isBefore(due)` -> `!paymentDate.isAfter(due)` (on the due date counts).
5. **Cap order:** cap the FINAL award: base -> tier -> bonus -> `Math.min(cap)`.
6. **Validation:** null payment / amount <= 0 / unknown member -> IllegalArgumentException (orElseThrow, not orElse(null)).
Solve order that scores well: all failures first -> logic on the fast unit loop -> repo stack trace -> config last.


## Round 1 — what the FIRST run should show (calibrate yourself)
19 tests total. Expect **12 failures + 2 errors**:
- PointsServiceTest (13): 8 fail — onDueDate bonus, all three tier boundaries,
  both cap tests, and all three validation tests (NPE/no-throw instead of IAE).
  5 pass (base floor, early bonus, late no-bonus, just-below-silver, breakdown).
- MemberRepositoryTest (2): both ERROR with "could not resolve attribute
  'member_id'" — stack trace, not assertion.
- PointsResourceTest (4): all 4 fail (wrong numbers from cap=5000 + boundaries
  + no-bonus-on-due-date; 500 on unknown member; 200 on negative amount).
If your wall looks different, your environment is off — stop and fix that first.


## Round 2 — what the FIRST run should show
9 tests total. Expect **7 red**:
- GraceWindowTest: 1 of 3 fails (lastDayOfGrace).
- AwardResourceTest: all 5 fail. FOUR of them are 500s whose stack traces all
  say IllegalArgumentException: "amount must be positive" — that single
  misleading wall is two bugs stacked (JSON fields never bind -> amount is 0;
  mapper unregistered -> IAE becomes 500). unknownMemberHistory fails
  differently: an empty 200 where 400 was expected.
- SeedDataTest: fails with the same 500.
The lesson of the wall: when many e2e tests share ONE exception message,
you are probably looking at a cascade, not many bugs.

# ROUND 2 (8 bugs)

**R2-A. 500s everywhere on validation paths; zeroAmount & unknownMember tests want 400.**
`GlobalExceptionMapper` exists but is NOT registered — the `@Provider`
annotation is missing, so JAX-RS never sees it.
Fix: annotate the class:
```java
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<IllegalArgumentException> { ... }
```
Lesson: "written" != "registered". When a mapper/filter/interceptor silently
does nothing, check its registration annotation first.

**R2-B. webhookContract test: basePoints 0 / 400 "amount must be positive" on a $2,000 payment.**
The processor sends snake_case (`amount_cents`); `AwardRequest` has camelCase
public fields and no mapping, so every field deserializes to null/0.
Fix (either):
```java
public class AwardRequest {
    @JsonProperty("payment_id")   public String paymentId;
    @JsonProperty("member_id")    public String memberId;
    @JsonProperty("amount_cents") public long amountCents;
    @JsonProperty("payment_date") public String paymentDate;
    @JsonProperty("due_date")     public String dueDate;
}
```
or globally in application.properties:
`quarkus.jackson.property-naming-strategy=SNAKE_CASE` (beware: that also
changes RESPONSE field names and would break other tests — the targeted
@JsonProperty fix is correct here; saying why is interview gold).

**R2-C. First POST /points/award blows up with TransactionRequiredException.**
`AwardService.award()` persists without a transaction.
Fix: `@Transactional` (jakarta.transaction.Transactional) on `award(...)`.
Lesson: reads work without a tx; WRITES need one; Quarkus does not open one
for you on plain resource methods.

**R2-D. Replay test: second POST 500s with a primary-key violation (after
R2-C it surfaces as ConstraintViolation) and/or history size is 2.**
No idempotency check. Fix in `AwardService.award()`:
```java
@Transactional
public PointsAwardEntity award(RentPayment payment) {
    PointsAward calc = pointsService.calculateForPayment(payment);
    return awards.findByIdOptional(calc.paymentId())
            .orElseGet(() -> {
                PointsAwardEntity e = new PointsAwardEntity(
                        calc.paymentId(), calc.memberId(), calc.basePoints(),
                        calc.tierAdjustedPoints(), calc.onTimeBonusApplied(),
                        calc.totalPoints(), payment.getPaymentDate());
                awards.persist(e);
                return e;
            });
}
```
Say out loud: the PK is the real guarantee under concurrent replays; the
check-then-insert is the fast path. (In production: catch the constraint
violation and re-read, or use an upsert.)

**R2-E. lastDayOfGrace unit test fails: due+3 treated as late.**
`isBefore(due.plusDays(3))` excludes the boundary day (off-by-one on an
inclusive spec). Fix:
```java
boolean onTime = !payment.getPaymentDate()
        .isAfter(payment.getDueDate().plusDays(GRACE_DAYS));
```
Rule of thumb: inclusive upper bounds are `!isAfter(limit)`, never
`isBefore(limit.plusX)`.

**R2-F. historyIsMostRecentFirst: rows correct, order reversed.**
Comparator sorts ascending. Fix:
```java
.sorted(Comparator.comparing((PointsAwardEntity a) -> a.awardedOn)
        .reversed()
        .thenComparing(a -> a.paymentId))
```
(If you want ties newest-stable: reverse FIRST, then thenComparing — order of
reversed() matters: `comparing(...).reversed().thenComparing(...)` gives
descending date, ascending paymentId within a date, which is what the test
expects for distinct dates; mention the subtlety.)

**R2-G. SeedDataTest: gold member gets 1.0x end to end while GraceWindowTest
is green.** Logic fine -> data wrong. `import.sql` seeds mem_gold at 5,000
(typo) vs spec 50,000. Fix the seed row.
Lesson: when unit tests pass and e2e disagrees on NUMBERS, suspect config
and seed data before touching code.


**R2-H. unknownMemberHistory test: GET /points/history/mem_nobody returns an
empty 200; test (and spec) want 400.**
`AwardService.historyFor()` never validates the member exists — it just
queries awards and returns []. Fix: inject the lookup and fail fast:
```java
@Inject
MemberLookup members;

public List<PointsAwardEntity> historyFor(String memberId) {
    members.findByMemberId(memberId)
            .orElseThrow(() -> new IllegalArgumentException(
                    "unknown member: " + memberId));
    return awards.findAllForMember(memberId).stream()
            .sorted(Comparator.comparing((PointsAwardEntity a) -> a.awardedOn)
                    .reversed()
                    .thenComparing(a -> a.paymentId))
            .toList();
}
```
Lesson: "no rows" and "no such member" are different answers; an empty 200
for a nonexistent member hides typos from every caller. (Needs R2-A's
@Provider fix to surface as a 400.)

## Efficient Round-2 solve order (the cascade, as you'd actually observe it)
Bugs hide behind bugs here — fixing one REVEALS the next. The observable chain:
1. **B (@JsonProperty)** or **A (@Provider)** first — the wall of identical
   "amount must be positive" 500s points at both. Fixing B makes payloads
   parse; fixing A turns raw IAEs into clean 400s with messages. Do both
   before judging anything else.
2. Re-run. **C now surfaces**: the first real POST hits
   TransactionRequiredException (you could not see it while amount=0 aborted
   before the persist).
3. Re-run. **D surfaces**: the replay test now reaches its second POST and
   dies on the primary-key violation (idempotency).
4. **H**: unknownMemberHistory still returns an empty 200 — add the
   existence check (now that A is fixed, it becomes a proper 400).
5. **E** (grace off-by-one) any time — it's a pure unit failure, independent.
6. **F** (history order) once the three POSTs succeed.
7. **G** (seed) last: logic green, SeedDataTest still red -> data.
The meta-skill being drilled: expect the red wall to peel in LAYERS, and
re-run after every fix instead of batching blind changes.

# PART B — model answers

**Brief A (webhook race).** Correct shape: unknown member on /award should
NOT be a 4xx (processor never retries 4xx -> points lost forever). Return
503 (retryable) for "member not found YET," keep 400 only for structurally
invalid payloads (non-positive amount). In-hour implementation: catch the
unknown-member case in AwardResource/mapper and map to 503 with Retry-After;
add a test asserting 503. Production sentence: "Real fix is accept-then-queue:
202 immediately, persist the raw event, process async with retries and a DLQ;
the processor's retry window stops being our correctness mechanism."

**Brief B (referrals).** Build: a `Referral` entity (referrerId, referredId,
bonusPaid boolean) + a hook in award(): if this is the member's FIRST award
(history empty before insert) and a referral row exists, grant 2,500 to the
referrer as a synthetic PointsAwardEntity (paymentId = "ref_" + payment id ->
idempotent for free). Stub the TBD: a plain POST /referrals admin endpoint
seeding the link, clearly labeled temporary. Cut line: no UI, no attribution
windows, no multi-level — one test proving first-payment triggers exactly one
bonus, replay-safe.

**Brief C (statement).** GET /points/statement/{memberId}/{yyyy-MM}: parse
with YearMonth, filter awards where awardedOn is in that month, aggregate in
the service (count, onTimeCount, totalPoints) and return awards list.
Judgment answers: month logic belongs in the service (testable), not SQL yet
(H2 scale) — note the GROUP BY migration path; statements stay correct under
formula changes BECAUSE awards are persisted facts (recompute never rewrites
history) — that's the line interviewers want to hear.
