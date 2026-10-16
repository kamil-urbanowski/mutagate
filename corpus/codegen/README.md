# codegen

- **Source:** [describeadmin/codegen](https://github.com/describeadmin/codegen) at
  [`7697931`](https://github.com/describeadmin/codegen/commit/7697931d89755fe35b6cd149d774babd85af9bd5).
  Apache-2.0.
- **Why chosen:** 23 of 28 commits AI co-authored, from commit #2 ([commit](https://github.com/describeadmin/codegen/commit/bec9fa09980539b5a76a59bfca7f71297f9ca6d7)). 5 test files for 11 main classes; 10 have no test class of their own.
- **What it is:** Generates Java, SQL, and Vue code from a YAML spec. Plain Java 17; its only runtime dependency is snakeyaml. Chinese comments and test display names.

Reviewed 2026-10-16 on JDK 21.0.12.1 and Maven 3.9.16.

## Class exclusions

None (`exclusions.json` is empty).

Selection rule: exclude any class that needs a Spring context, a database, or
another live external service to run in a unit test. None of the 11 main classes does. Main code imports only the JDK, snakeyaml, and its own packages; no database, network, Spring, or process-launching code. It reads spec files from local paths and reads one environment variable (`CODEGEN_LAYOUT`) as an optional setting.

## Failing existing tests

None. `mvn -B test` on an unmodified copy: 48 tests, 0 failures, 0 errors, 0 skipped (20 in `SpecLoaderTest`, including one `@Nested` class; 15 in `FrontendGeneratorTest`; 13 in `GeneratorTest`). There are no disabled or
conditional tests. Nothing is left out of PIT runs (`failing-tests.json` is
empty).

## Baseline ledger

`baseline.json`, from `mutagate baseline --repo work/codegen` on 2026-10-16.
It records the repo commit and tool versions it was measured on, then every
mutant.

| Measure | Value |
|---|---|
| Mutants | 230 |
| KILLED | 139 |
| SURVIVED | 43 |
| NO_COVERAGE | 48 |
| Baseline mutation score | 139 / 230 = 60.4% |
| Classes selected | 10 of 11 with mutants |
| Targets (undetected mutants in selected classes) | 91 |

Not selected (every mutant detected): `SpecException`.
`CodegenCli` alone holds 46 of the 91 targets.
