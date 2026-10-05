# Round 2 — Rent Points Service: Awarding & History (60-minute session)

Round 1's calculation bugs are already fixed here. The team has since added
**persistence**: awards are stored, replayable, and queryable. The payment
processor POSTs a webhook per successful rent payment; mobile and support
read history.

## Format (same as the real thing)
- **Part A (30 min):** `mvn test` — make the suite green. 8 defects are
  live across REST, serialization, transactions, service logic, sorting,
  dates, and seed data. Trust the specs in the Javadoc and this README over
  the current code.
- **Part B (30 min):** open `PART2.md` and build/decide per the brief.

## Specs that matter
1. Webhook contract (fixed by the processor, snake_case):
   `payment_id, member_id, amount_cents, payment_date, due_date`.
2. Awarding is **idempotent on payment_id** — replays return the original
   award; exactly one row per payment, ever.
3. `awardedOn` = the payment date.
4. History: most recent first, ties by paymentId.
5. On-time = paymentDate <= dueDate + 3 days (inclusive grace).
6. Validation errors -> HTTP 400 `{"error": ...}` via the global exception
   mapper (endpoints have no try/catch by design).
7. Seed balances: blue 24,999 / silver 25,000 / gold 50,000 /
   plat 100,000 / whale 500,000.

## Run
- Tests: `mvn test`
- Dev mode: `mvn quarkus:dev` then curl the endpoints.

Timer starts when you first run the tests. Answer key: `../MOCK_ANSWERS.md`
(also ANSWER_KEY_ROUND1.md inside round1/). Don't peek.
