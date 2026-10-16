# as-you-like-it

**Not in the corpus.** Baselined while choosing the corpus, which was chosen
by target count on 2026-10-16 (see `corpus/README.md`). No runs are made on it.

- **Source:** [kencoba/as-you-like-it](https://github.com/kencoba/as-you-like-it) at
  [`bc1ade7`](https://github.com/kencoba/as-you-like-it/commit/bc1ade7198a0cd9fc9266c5ef8b89aeb75857429).
  MIT.
- **Why chosen:** 3 of 4 commits co-authored by Claude, from commit #1 ([commit](https://github.com/kencoba/as-you-like-it/commit/105522af59b14b147cbbde919032c541c65e740d)). 7 test classes for 27 main classes; 20 have no test class of their own.
- **What it is:** A domain-driven design teaching exercise: assignments, submissions, and likes, with services and in-memory repositories. Plain Java 21, no runtime dependencies.

Reviewed 2026-10-16 on JDK 21.0.12.1 and Maven 3.9.16.

## Class exclusions

None (`exclusions.json` is empty).

Selection rule: exclude any class that needs a Spring context, a database, or
another live external service to run in a unit test. None of the 27 main classes does. Main code imports only the JDK and its own packages; the repositories are in-memory. No file, database, network, Spring, or process-launching code.

## Failing existing tests

None. `mvn -B test` on an unmodified copy: 38 tests, 0 failures, 0 errors, 0 skipped (11 in `LikeServiceTest`, 10 in `SubmissionServiceTest`, 9 in `SubmissionTest`, 4 in `EntryTest`, 2 in `AssignmentTest`, 1 each in `SubmitterTest` and `AsYouLikeItScenarioTest`). There are no disabled or
conditional tests. Nothing is left out of PIT runs (`failing-tests.json` is
empty).

## Baseline ledger

`baseline.json`, from `mutagate baseline --repo work/as-you-like-it` on 2026-10-16.
It records the repo commit and tool versions it was measured on, then every
mutant.

| Measure | Value |
|---|---|
| Mutants | 197 |
| KILLED | 106 |
| SURVIVED | 68 |
| NO_COVERAGE | 22 |
| TIMED_OUT | 1 |
| Baseline mutation score | 107 / 197 = 54.3% |
| Classes selected | 10 of 19 with mutants |
| Targets (undetected mutants in selected classes) | 90 |

Not selected (every mutant detected): `AssignmentId`, `LikeId`, `Submission`, `SubmissionId`, `SubmittedWorkId`, `Submitter`, `SubmitterId`, `InMemoryAssignmentRepository`, `InMemorySubmissionRepository`.
`AsYouLikeItScenario` alone holds 64 of the 90 targets.
