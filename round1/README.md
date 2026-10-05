# Mock Interview v2: Rent Points Service (Quarkus, production-shaped)

Simulates a 2-part, 60-minute screen in a GitHub Codespace:
**Part 1 (30 min, by hand)** — make the failing suite pass. The bugs span
config, the repository layer, and service logic — navigation is part of the test.
**Part 2 (30 min, agents allowed)** — build on Part 1 from `PART2_NOTES.md`
(don't open it until Part 1 ends).

Layout (mirrors a typical production Quarkus service):

```
src/main/resources/application.properties   <- config (H2 datasource, points config)
src/main/resources/import.sql               <- seeded members
src/main/java/com/mock/points/
    entity/MemberEntity.java                <- Panache entity
    repo/MemberLookup.java                  <- lookup abstraction
    repo/MemberRepository.java              <- Panache repository
    service/RentPayment.java
    service/PointsAward.java                <- result record w/ breakdown
    service/PointsService.java              <- business rules (spec in Javadoc)
    rest/PointsResource.java                <- POST /points/preview
src/test/java/com/mock/points/
    PointsServiceTest.java                  <- fast plain-JUnit spec (run this constantly)
    MemberRepositoryTest.java               <- @QuarkusTest, hits H2
    PointsResourceTest.java                 <- @QuarkusTest, end to end
```

## One-time setup in GitHub Codespaces (do BEFORE any timer)

1. Create a new **private** GitHub repo (e.g. `rent-points-mock`) and push this
   project to it (everything including the `.devcontainer/` folder).
2. On the repo page: **Code ▸ Codespaces ▸ Create codespace on main**.
   The devcontainer preinstalls Java 17, Maven, the Java extension pack, and
   the Quarkus extension — the same tooling class the real interview uses.
3. Wait for the container build to finish, then in the terminal:
   `mvn test`
   The FIRST run downloads dependencies (several minutes) — let it finish
   completely before you consider starting a timer. Expected end state:
   **failures in all three test classes.** That's the starting position.
4. Optional but recommended: install Claude Code in the Codespace terminal for
   Part 2 (`npm install -g @anthropic-ai/claude-code`, then `claude` to log in).
5. Learn these before the timer (5 minutes):
   - Testing sidebar (beaker icon): run one test / one class / re-run failed
   - Cmd/Ctrl+P: open file by name        - Cmd/Ctrl+Shift+F: search project
   - F12: go to definition                - Shift+F12: find usages
   - Fast loop in terminal: `mvn test -Dtest=PointsServiceTest` (no Quarkus boot)

## Part 1 rules (hard 30:00 timer)

- Tests are the spec. **Do not modify tests.** The intended business rules are
  documented in `PointsService`'s Javadoc; the implementation, the repository,
  and the configuration do not all match them.
- Google fine; LLMs as search engine only; no agents; no pasting LLM code.
- Narrate out loud continuously.
- Suggested strategy: get `PointsServiceTest` fully green first (fast loop),
  then run the full suite and chase what's still red — the remaining failures
  are NOT in the service logic.
- Timer ends -> stop mid-keystroke, note where you were.

## Part 2 rules (second 30:00 timer)

- Open `PART2_NOTES.md`. Agents fully allowed.
- Minutes 0–5: read, decide, say the plan out loud (build / defer / design-note
  + reasons). Then drive the agent with architecture, not raw notes.
- Keep the original suite green. Leave 5 minutes for a final full `mvn test`
  and a 2-minute built/deferred/next summary spoken out loud.

## Self-grading

Part 1 targets: all 15 unit tests green inside ~20 min; all 21 tests green by
30 min is a strong performance. If unit tests are green but Quarkus tests are
red and you haven't looked at `application.properties` or the repository —
that's the navigation lesson this version exists to teach.

Process checklist (1 pt each): ran full suite first and read every failure /
noticed the failures cluster by layer / re-ran after each fix / narrated
continuously / never touched a test.

Part 2 checklist (1 pt each): prioritized the double-award (idempotency)
correctness issue / deferred at least one item out loud with a reason / gave
the agent constraints not raw notes / said what you reviewed carefully vs.
skimmed / ended green / coherent 2-minute summary.

8+ points: ready. Under 6: regenerate a fresh variant and rerun — don't repeat
this one from memory.

Full bug list and reference fixes: `ANSWER_KEY.md` (open only after both timers).
