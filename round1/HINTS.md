# Progressive hints — Round 1. Use a tier only when stuck 3+ minutes.
Format per failing area: T1 = nudge, T2 = the layer, T3 = one step from the fix.

## onDueDatePaymentGetsBonus
T1: Read the DisplayName literally. What does "exactly ON the due date" require?
T2: It's one boolean expression in the service. Which comparison excludes equality?
T3: `isBefore(due)` is exclusive. The spec is inclusive. Express "on or before."

## silver/gold/platinum boundary tests (all three)
T1: 25,000 is supposed to BE silver. What does the code say about exactly 25,000?
T2: tierMultiplier's comparisons. Strict vs inclusive.
T3: Every `>` in tierMultiplier should be `>=`.

## capAppliesToFinalAward / bonusCannotPushPastCap
T1: Both tests agree the cap is the LAST word. What happens after the cap in code?
T2: Order of operations in calculate(): where does Math.min sit relative to the bonus?
T3: base -> tier -> bonus -> THEN Math.min(cap). Move the cap to the end.

## nullPayment / unknownMember / nonPositiveAmount (IAE tests)
T1: The tests demand a SPECIFIC exception type. What's thrown now instead?
T2: calculateForPayment does no checks and resolves the member with orElse(null).
T3: Three fail-fast checks at the top; replace orElse(null) with orElseThrow(IAE).

## MemberRepositoryTest — both ERROR
T1: Read the first line of the stack trace. It names the thing it can't find.
T2: Panache query strings use what kind of name — column or field?
T3: find("member_id", ...) -> find("memberId", ...) or findByIdOptional(id).

## PointsResourceTest — silver 3125 / whale 50,000 wrong numbers
T1: Your unit tests (with an explicit cap) pass these same formulas. What differs end-to-end?
T2: Where does the RUNNING app get its cap?
T3: application.properties: points.monthly-cap is off by a factor of ten.
