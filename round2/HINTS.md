# Progressive hints — Round 2.

## The wall: five e2e tests all 500 with "amount must be positive"
T1: You sent amount_cents=200000. The service saw 0. Where did it get lost?
T2: Compare the JSON keys in the test body to the field names in AwardRequest.
T3: @JsonProperty("amount_cents") etc. on every AwardRequest field.

## Still 500 (not 400) after amounts bind
T1: A translator from IAE->400 exists in the codebase. Is it ON?
T2: Annotations are labels frameworks scan for. Find the mapper class. What label is missing?
T3: Add @Provider above GlobalExceptionMapper.

## New failure after the above: TransactionRequiredException
T1: Reads worked all along. What's different about THIS code path?
T2: It's the first WRITE. Writes need something opened around them.
T3: @Transactional (jakarta.transaction) on AwardService.award().

## Replay test: second POST -> primary-key violation / history size 2
T1: The processor retries. What should the SECOND identical request do?
T2: Look up before insert — on which field?
T3: awards.findByIdOptional(paymentId).orElseGet(persist-new). Say why the PK
    is the real guarantee anyway.

## unknownMemberHistory: empty 200, wanted 400
T1: "No rows" and "no such member" are different answers. Who checks the second?
T2: historyFor never consults members at all.
T3: Inject MemberLookup; orElseThrow(IAE) before querying awards.

## historyIsMostRecentFirst: right rows, wrong end first
T1: Which direction does Comparator.comparing sort by default?
T2: One word is missing from the chain.
T3: .reversed() right after comparing(awardedOn), BEFORE thenComparing.

## lastDayOfGrace (unit): due+3 treated late
T1: "Within 3 days, inclusive." Which comparison is exclusive at the edge?
T2: isBefore(due.plusDays(3)) — test the boundary by hand with due+3.
T3: !paymentDate.isAfter(due.plusDays(GRACE_DAYS)).

## SeedDataTest red while GraceWindowTest green
T1: Logic says gold is 1.5x and the unit tests agree. So what's NOT logic?
T2: Where does mem_gold's lifetime balance come from at runtime?
T3: import.sql seeds mem_gold at 5,000. Spec says 50,000.
