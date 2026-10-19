# MutaGate

Mutation-gated test generation for Java test suites.

Mutation testing finds places where code could be broken without any test
failing. A model writes a test aimed at that specific gap. The test is kept
only if it provably catches the break.

## Why

Line coverage says a test executed a line, not that it would notice if the
line were wrong. Mutation testing measures the real thing: introduce a small
defect, see whether any test fails. A defect nothing catches is a measured gap.

PIT finds those gaps but cannot fill them. MutaGate closes the loop.

## The gate

A generated test is kept only if it:

1. compiles,
2. passes against the unmodified code, every time it runs, and
3. kills the specific mutant it was written for.

Anything else is discarded. A program makes that call, not a person.

## Status

Targeted runs work end to end: MutaGate finds the mutants a class's tests
miss, asks a model for one test per mutant, and keeps only the tests that
kill their own mutant. The untargeted control run, the comparison that shows
what targeting adds, is not built yet.

## Results

First measured run: every selected class of
[codegen](https://github.com/describeadmin/codegen) at `7697931`, with Claude
Sonnet 5.5 at medium effort through Claude Code. One repository so far; the
rest of the corpus follows.

| Measure | Result |
|---|---|
| Mutation score, whole module | 60.4% → 96.1% (139 → 221 of 230 mutants) |
| Tests kept | 80 of 91 generated (88%) |
| Attempts per kept test | 1.20 |

Measured on Java 21, Maven 3.9.16, and PIT 1.30.0. Every run's details,
including each model reply and every kept test, are in [`results/`](results/).
