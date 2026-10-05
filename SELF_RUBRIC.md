# Score yourself like they will (1–4 each; 3 = hire bar)

**Diagnosis** — 1: edits code before reading failures. 2: chases failures one
by one, no grouping. 3: reads all output first, names the layer from the
signature, usually right. 4: also predicts the cascade ("fixing binding will
expose a tx error") and is right.

**Prioritization** — 1: random order. 2: file order. 3: fast unit loop first,
wiring next, config last. 4: dependency-aware (fixes the bug that GATES other
tests first) and says so.

**Code quality of fixes** — 1: hacks that pass tests (hardcoded values).
2: correct but sloppy. 3: minimal, idiomatic, matches codebase style.
4: also leaves the code better (names the invariant in a comment, tightens a
test) WITHOUT scope creep.

**Verification** — 1: declares done without rerunning. 2: reruns whole suite
every save (slow). 3: reruns the affected class per fix, full suite at end.
4: also adds/adjusts a missing boundary test when the suite under-specifies.

**Communication** — 1: silent. 2: narrates actions ("now I'm opening...").
3: narrates REASONING (signature -> layer -> hypothesis -> confirm). 4: also
flags uncertainty honestly and recovers out loud when wrong.

**Part B judgment** — 1: builds the first idea end-to-end, runs out of time.
2: builds something but can't say what was cut. 3: states the slice, cuts
scope out loud, ships demoable + tested. 4: also names the production-grade
design in one sentence and why the hour version differs.

24 max. 18+ with no 1s = you're ready. Any dimension at 1-2: drill that
dimension specifically, not the whole mock.
