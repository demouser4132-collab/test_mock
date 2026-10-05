# Part B — Agentic half (30 minutes). Pick ONE brief per practice session.

You're allowed (expected) to use your AI tooling exactly as you would in the
real interview. The interviewer is watching HOW you direct it: scoping,
verification, and what you choose NOT to build.

## Brief A — Webhook hardening (ops is paging)
The processor's webhooks sometimes arrive twice within milliseconds, and
occasionally with a member that doesn't exist YET (member creation lags a
few seconds). Today: duplicates are handled, unknown member = 400, and the
processor treats 400 as permanent failure (no retry) — so those payments
never earn points. Product wants no member to lose points over a race.
Decide and implement what /points/award should do about the unknown-member
race. Constraints: the processor retries 5xx with backoff for 24h; it never
retries 4xx. Say out loud what you'd build with a queue in production, and
what the in-process approximation is for this hour.

## Brief B — Referral linking (product's favorite)
Marketing wants "member A referred member B -> A gets 2,500 points when B's
FIRST rent payment lands." The linking mechanism (how we know A referred B)
is explicitly TBD by another team. Build the maximum useful, testable slice
that doesn't invent the TBD part, and defend the cut line.

## Brief C — Monthly statement (support's weekly ask)
Support wants GET /points/statement/{memberId}/{yyyy-MM}: total points that
month, number of payments, on-time count, and the award list. Build it with
a test. Bonus judgment: where does month boundary logic belong, and what
happens to statements if the points formula changes next quarter?

Scoring yourself: did you (1) restate the goal in one sentence before coding,
(2) write/adjust a test, (3) say at least one "I'm deliberately not doing X
because Y," (4) finish something demoable?
