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
2. passes against the unmodified code, and
3. kills the specific mutant it was written for.

Anything else is discarded. A program makes that call, not a person.

## Status

Early. Nothing works yet.

## Results

Benchmark output lives in `results/`.
