# Round 1 — Session script (read aloud to yourself, then start the timer)

"Thanks for joining. Here's the format: this Codespace has a small Quarkus
service — a rent-points calculator. The test suite is failing. You'll have
30 minutes to get it green. You can use anything you'd use at work: docs,
search, your AI tools. I care about HOW you work as much as the result, so
please think out loud — tell me what a failure means to you before you chase
it. I may ask questions while you work; that's normal, not a sign something
is wrong. Ready? Run the tests."

## Checkpoints (glance at the clock)
- 0–3 min: full `mvn test`, read ALL output, group failures aloud. No edits yet.
- by 10 min: unit-test logic bugs fixed or clearly in progress.
- by 20 min: repository + validation done; only config/e2e stragglers left.
- 28 min: STOP fixing. Full run. Summarize: what you fixed, what remains, what
  you'd do next. (An honest "here's the one I'd fix next and why" scores
  better than a silent half-fix.)

## After the timer
Open ANSWER_KEY_ROUND1.md. For every bug you missed, say the failure
signature out loud once ("expected-but-was on a boundary -> inclusive/exclusive").
