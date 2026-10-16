# Gestao-Acoes-Socioambientais

**Not in the corpus.** Baselined while choosing the corpus, which was chosen
by target count on 2026-10-16 (see `corpus/README.md`). No runs are made on it.

- **Source:** [PedroZef/Gestao-Acoes-Socioambientais](https://github.com/PedroZef/Gestao-Acoes-Socioambientais) at
  [`b551781`](https://github.com/PedroZef/Gestao-Acoes-Socioambientais/commit/b551781a532ba4f85bacb58ab1edaac02e386e77).
  MIT.
- **Why chosen:** All 5 commits AI co-authored ([commit](https://github.com/PedroZef/Gestao-Acoes-Socioambientais/commit/ce8cd97efd2824eeb1f21fd19224b239e4f6bbbd)). 2 test classes for 13 main classes; 12 have no test class of their own.
- **What it is:** A console app from an OOP course for managing social and environmental volunteer actions. Plain Java 21; Portuguese identifiers.

Reviewed 2026-10-16 on JDK 21.0.12.1 and Maven 3.9.16.

## Class exclusions

None (`exclusions.json` is empty).

Selection rule: exclude any class that needs a Spring context, a database, or
another live external service to run in a unit test. None of the 13 main classes does. Main code imports only the JDK and its own packages; no database, network, Spring, or process-launching code. `ui.MenuConsole` reads `System.in`, but it also takes a `Scanner` in its constructor, and its existing test drives it that way. Console input is not an external service.

## Failing existing tests

None. `mvn -B test` on an unmodified copy: 41 tests, 0 failures, 0 errors, 0 skipped (31 in `ImpactTest`, 10 in `MenuConsoleTest`). There are no disabled or
conditional tests. Nothing is left out of PIT runs (`failing-tests.json` is
empty).

## Baseline ledger

`baseline.json`, from `mutagate baseline --repo work/Gestao-Acoes-Socioambientais` on 2026-10-16.
It records the repo commit and tool versions it was measured on, then every
mutant.

| Measure | Value |
|---|---|
| Mutants | 209 |
| KILLED | 153 |
| SURVIVED | 37 |
| NO_COVERAGE | 18 |
| TIMED_OUT | 1 |
| Baseline mutation score | 154 / 209 = 73.7% |
| Classes selected | 7 of 9 with mutants |
| Targets (undetected mutants in selected classes) | 55 |

Not selected (every mutant detected): `Plantio`, `Validacao`.
`MenuConsole` alone holds 34 of the 55 targets.
