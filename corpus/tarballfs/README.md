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

None.

Selection rule: exclude any class that needs a Spring context, a database, or
another live external service to run in a unit test. None of the 25 main classes
does. Their imports are limited to the JDK and commons-compress, and the main
sources contain no Spring, JDBC, socket, HTTP, or process-launching code. The
classes read archive files and in-memory bytes only.

## Failing existing tests

None. `mvn -B test` on an unmodified copy: 53 tests, 0 failures, 0 errors,
0 skipped (24 in `TarFileSystemProviderTest`, 15 in
`GzipFileSystemProviderTest`, 14 in `Bzip2FileSystemProviderTest`). There are
no disabled or conditional tests. Nothing is left out of PIT runs.

## Baseline ledger

Not yet recorded. It will come from `pit.py`'s whole-module run.
