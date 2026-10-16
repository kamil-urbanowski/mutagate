# GameZoneUnicesar

- **Source:** [adbautistar/GameZoneUnicesar](https://github.com/adbautistar/GameZoneUnicesar) at
  [`8ca31bf`](https://github.com/adbautistar/GameZoneUnicesar/commit/8ca31bf7c630796d096a219d5411eb6c7e98ffdc).
  MIT.
- **Why chosen:** CLAUDE.md in commit #1; 155 of 170 commits AI co-authored ([commit](https://github.com/adbautistar/GameZoneUnicesar/commit/82862dc5f59263f98ce1a83bc8e1cd2c892c0bf2)). 15 test files for 35 main classes; 29 have no test class of their own.
- **What it is:** A video-game store domain: products, sales, warranties, returns, and promotions, with a console menu. Plain Java 17, no runtime dependencies (Mockito and AssertJ in tests).

Reviewed 2026-10-16 on JDK 21.0.12.1 and Maven 3.9.16.

## Class exclusions

None (`exclusions.json` is empty).

Selection rule: exclude any class that needs a Spring context, a database, or
another live external service to run in a unit test. None of the 35 main classes does. Main code imports only the JDK and its own packages; no database, network, Spring, or process-launching code. The `persistence` package saves to plain text files at a path given to each repository's constructor, which its existing tests point at temporary files. Local files are not a database. `ui.ConsoleMenu` reads `System.in`, which is not an external service.

## Failing existing tests

None. `mvn -B test` on an unmodified copy: 88 tests, 0 failures, 0 errors, 0 skipped, across 13 test classes; `RepositoriesTest` holds 5 `@Nested` classes of 2 tests each. There are no disabled or
conditional tests. Nothing is left out of PIT runs (`failing-tests.json` is
empty).

## Baseline ledger

`baseline.json`, from `mutagate baseline --repo work/GameZoneUnicesar` on 2026-10-16.
It records the repo commit and tool versions it was measured on, then every
mutant.

| Measure | Value |
|---|---|
| Mutants | 668 |
| KILLED | 250 |
| SURVIVED | 65 |
| NO_COVERAGE | 353 |
| Baseline mutation score | 250 / 668 = 37.4% |
| Classes selected | 25 of 35 with mutants |
| Targets (undetected mutants in selected classes) | 418 |

Not selected (every mutant detected): `Accessory`, `BasicWarranty`, `Cable`, `Console`, `Controller`, `ExtendedWarranty`, `Memory`, `Product`, `Promotion`, `VideoGame`.
`ConsoleMenu` alone holds 301 of the 418 targets.
