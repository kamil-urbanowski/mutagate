# Corpus

Notes on the repositories used as targets. The repositories themselves are not
committed here; record how to obtain each one and why it was chosen.

Direction: public open-source Java on Maven with JUnit 5 tests, prioritizing
AI-generated codebases with thin test suites.

Each repository also gets a folder, `corpus/<repo>/`, holding:

- its baseline ledger, from one whole-module PIT run;
- its class exclusions, each with a one-line reason, decided before any run;
- its existing tests that fail on unmodified code, left out of every PIT run.

| Repo | Source | Why chosen | Baseline mutation score |
|---|---|---|---|
