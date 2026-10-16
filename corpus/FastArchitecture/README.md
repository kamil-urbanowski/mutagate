# FastArchitecture

**Not in the corpus.** Baselined while choosing the corpus, which was chosen
by target count on 2026-10-16 (see `corpus/README.md`). No runs are made on it.

- **Source:** [andrestubbe/FastArchitecture](https://github.com/andrestubbe/FastArchitecture) at
  [`c957238`](https://github.com/andrestubbe/FastArchitecture/commit/c957238e13a5ed7af226fc3533ae18c89ac35e6a).
  MIT.
- **Why chosen:** AGENTS.md from commit #3 of 11 ([commit](https://github.com/andrestubbe/FastArchitecture/commit/bd220a9be6e50c6dabe4b17bb1a3f1cbaabd9cab)). One test class for 13 main classes; 12 have no test class of their own.
- **What it is:** An architecture standard for human and AI co-engineering, with a validator that checks a Java project's layers against a manifest of allowed dependencies and reports violations. Plain Java 17, no runtime dependencies.

Reviewed 2026-10-16 on JDK 21.0.12.1 and Maven 3.9.16.

## Class exclusions

None (`exclusions.json` is empty).

Selection rule: exclude any class that needs a Spring context, a database, or
another live external service to run in a unit test. None of the 13 main classes does. Main code imports only the JDK and its own package. It reads source files and manifests from local paths; no database, network, Spring, or process-launching code.

## Failing existing tests

None. `mvn -B test` on an unmodified copy: 9 tests, 0 failures, 0 errors, 0 skipped (all 9 in `ArchitectureValidatorTest`). The second test file is a benchmark with a `main` method, not a JUnit test. There are no disabled or
conditional tests. Nothing is left out of PIT runs (`failing-tests.json` is
empty).

## Baseline ledger

`baseline.json`, from `mutagate baseline --repo work/FastArchitecture` on 2026-10-16.
It records the repo commit and tool versions it was measured on, then every
mutant.

| Measure | Value |
|---|---|
| Mutants | 156 |
| KILLED | 75 |
| SURVIVED | 31 |
| NO_COVERAGE | 50 |
| Baseline mutation score | 75 / 156 = 48.1% |
| Classes selected | 11 of 11 with mutants |
| Targets (undetected mutants in selected classes) | 81 |
