# Drills & the failure-signature reflex

## The 2-minute opening ritual (do it in the real interview, verbatim)
1. `mvn test` FIRST. Never open code before the first full run.
2. Read EVERY failure, not the first one. Group them out loud:
   "N unit failures = logic. Quarkus-test failures with stack traces = wiring:
   repo, config, serialization, transactions."
3. Only then open files, starting from the layer the loudest failure names.

## Failure-signature table (memorize — this is 80% of Part A speed)
| You see | It means | Look at |
|---|---|---|
| Assertion expected X got Y, plain JUnit | business logic | the service method under test |
| `could not resolve attribute 'foo'` / UnknownAttribute | Panache/HQL query uses column name, not entity FIELD name | repository `find("...")` strings |
| `TransactionRequiredException` | a write without an active tx | missing `@Transactional` on the mutating service/resource method |
| `ConstraintViolationException` / PK violation on replay | idempotency not implemented | check-then-insert (or upsert) on the natural key |
| JSON field is 0/null that you sent | naming mismatch | `@JsonProperty` vs the wire contract; or a global naming strategy |
| Endpoint returns 500, test wants 400 | exception not mapped | `@Provider` on the ExceptionMapper (is it REGISTERED, not just written?) |
| Right rows, wrong order | comparator direction | `.reversed()`, `thenComparing` for ties |
| Boundary date off by one | `isBefore` vs `!isAfter` | inclusive specs need `!isAfter(limit)` |
| Unit tests green, e2e numbers wrong | config or seed data | `application.properties`, `import.sql` |

## Bug cascades (Round-2's core lesson)
Some bugs HIDE others: a request that dies at validation never reaches the
persist that would show the transaction bug. So: many tests sharing ONE
exception message usually = a cascade, not many independent bugs. Fix the
gate, re-run, EXPECT new reds, and say "new failure, that's the next layer"
out loud instead of panicking.

## Narration script (practice saying it)
"Failure says {message}. That signature usually means {layer}. Checking
{file}... confirmed: {one-sentence diagnosis}. Fix is {one sentence}.
Re-running just this class first."

## Warm-up (10 min, morning of)
- Write from memory: a stream pipeline with filter/map/sorted/collect;
  an Optional chain with orElseThrow; `!date.isAfter(other)`.
- Say the Spring->Quarkus mapping table out loud once.
- One full `mvn test` on round2 to re-feel the output format.
