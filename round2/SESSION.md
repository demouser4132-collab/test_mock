# Round 2 — Session script

"Same format, newer slice of the codebase: awards are now persisted and a
payment processor POSTs us webhooks. The suite is red. 30 minutes, think
out loud, use your tools. One hint about this codebase's style: validation
errors are supposed to become 400s via a global mechanism, not try/catch
in endpoints. Go."

## What's genuinely different from Round 1 (know this going in)
- The failures CASCADE: several bugs hide behind one wall of identical 500s.
  Expect to fix, re-run, and see NEW failures appear. That's progress,
  not regression — say so out loud when it happens.
- Two bugs are "the code is fine, the wiring/label is missing" bugs.
  If behavior is absent rather than wrong, ask: is this REGISTERED?

## Checkpoints
- 0–3 min: full run. Notice the shared exception message across e2e tests.
  Say the cascade hypothesis aloud before editing.
- by 12 min: JSON binding + exception mapping fixed; re-run done.
- by 20 min: transaction + idempotency fixed.
- by 27 min: order/grace/seed/history-validation swept; full green run.
- 28–30: summarize. If not green: name each remaining red and its suspected
  layer in one sentence each.

## After the timer
Part B: open PART2.md, pick ONE brief, fresh 30-minute timer.
