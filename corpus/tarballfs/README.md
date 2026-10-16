# tarballfs

- **Source:** [iachimoe/tarballfs](https://github.com/iachimoe/tarballfs) at
  [`9d0d8eb`](https://github.com/iachimoe/tarballfs/commit/9d0d8eb2b3a52801b34531675750076fd46a1b5f).
  Unlicense.
- **Why chosen:** the README says the code "has been entirely generated using
  Cursor AI" and "has not been extensively reviewed". It is a single Maven
  module with JUnit 5 tests, and 22 of its 25 main classes have no test class
  of their own.
- **What it is:** a read-only Java NIO file system for tar, gzip, and bzip2
  archives. Plain Java 17; its only runtime dependency is commons-compress.

Reviewed 2026-10-16 on JDK 21.0.12.1 and Maven 3.9.16.

## Class exclusions

None (`exclusions.json` is empty).

Selection rule: exclude any class that needs a Spring context, a database, or
another live external service to run in a unit test. None of the 25 main classes
does. Their imports are limited to the JDK and commons-compress, and the main
sources contain no Spring, JDBC, socket, HTTP, or process-launching code. The
classes read archive files and in-memory bytes only.

## Failing existing tests

None. `mvn -B test` on an unmodified copy: 53 tests, 0 failures, 0 errors,
0 skipped (24 in `TarFileSystemProviderTest`, 15 in
`GzipFileSystemProviderTest`, 14 in `Bzip2FileSystemProviderTest`). There are
no disabled or conditional tests. Nothing is left out of PIT runs
(`failing-tests.json` is empty).

## Baseline ledger

`baseline.json`, from `mutagate baseline --repo work/tarballfs` on 2026-10-16.
It records the repo commit and tool versions it was measured on, then every
mutant.

| Measure | Value |
|---|---|
| Mutants | 722 |
| KILLED | 288 |
| SURVIVED | 66 |
| NO_COVERAGE | 368 |
| Baseline mutation score | 288 / 722 = 39.9% |
| Classes selected | 24 of 25 |
| Targets (undetected mutants in selected classes) | 434 |

The one class not selected, `CompressorFormatVerifier`, has every mutant
killed. The counts match the hand run of 2026-10-02 mutant for mutant.
