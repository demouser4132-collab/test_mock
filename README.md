# Bilt Mock v3 — two timed rounds + drills

- `round1/` — the original 6-bug session (config/repo/service). Clean repo,
  run it as your first full 30/30 rehearsal. Key: round1/ANSWER_KEY_ROUND1.md
- `round2/` — the extended session: 7 new defects across REST, JSON
  contracts, transactions, idempotency, sorting, dates, and seed data, plus
  three Part-B briefs. Run it second; it covers everything round1 doesn't.
- `INTERVIEW_DRILLS.md` — the opening ritual, the failure-signature table,
  and the narration script. Read before each round.
- Per round: `SESSION.md` (the interviewer's opening script + time checkpoints
  — read it aloud, then start) and `HINTS.md` (3-tier progressive hints; use a
  tier only after 3 stuck minutes — hints beat answers for learning).
- `SELF_RUBRIC.md` — grade yourself 1–4 on six dimensions after each round.
- `MOCK_ANSWERS.md` — consolidated key for BOTH rounds + model Part-B
  answers. Open only after your timers end.

Setup per round: push the round folder (or the whole repo) to a private
GitHub repo -> Codespaces -> `mvn test`.
