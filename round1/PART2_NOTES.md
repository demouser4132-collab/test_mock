# Part 2 — Product & Engineering Notes

*(Open this ONLY when your Part 1 timer ends. Agents fully allowed from here.
30 minutes. Working code required — not a design write-up. You will not finish
everything below. Choosing is the exercise.)*

---

Hey — dumping context from the product sync and some eng threads. Make of it
what you will before the demo this afternoon.

- CS keeps seeing **double-submitted payments** (user taps Pay twice, or the
  bank webhook retries). `/points/preview` is stateless so it's fine, but the
  moment we actually AWARD points, the same paymentId must never award twice.
  Finance is *very* unhappy about this in the old system. We have a database
  now — use it?

- Product wants a **referral bonus**: when a member's referral pays rent for
  the first time, the referrer gets a flat 2,500 points. No design for how
  referrals are linked yet — invite codes? phone contacts? TBD with growth.

- We need a **ledger/history** — members ask "why did I get 3,125 points?"
  Support wants the stored breakdown (base / tier / bonus / cap) per payment,
  and mobile would use it for the points detail screen. Feels related to the
  awarding work?

- Compliance flagged that points **expiry** rules differ by state (?!) —
  legal is "still looking into it." Nothing concrete yet.

- An **admin monthly summary** came up again: total points awarded this
  month, count of capped payments, average award. Ops would check it weekly,
  tops.

- Infra mentioned **rate limiting** the preview endpoint before mobile
  launch. No incident yet.

- Whatever ships must keep the existing test suite green.

---

*Deliverable: pick what matters, build it as working code on top of Part 1,
stub or design-note what you defer, and be ready to explain the calls you
made in ~2 minutes at the end.*
