# MutaGate

Mutation-gated test generation for Java test suites.

Mutation testing finds places where code could be broken without any test
failing. A language model writes a test aimed at that specific gap. The test
is kept only if it provably catches the break.

On two AI-generated Java repositories, the kept tests raised the mutation
score, the share of small planted defects that the tests catch, from 60.4% to
96.5% and from 39.9% to 96.7% ([Results](#results)).

## Why

Line coverage says a test executed a line, not that it would notice if the
line were wrong. Mutation testing measures the real thing: introduce a small
defect, called a mutant, and see whether any test fails. A test that fails
kills the mutant. A mutant no test kills is a measured gap.

[PIT](https://pitest.org), an open-source mutation-testing tool for Java,
finds those gaps but cannot fill them. MutaGate closes the loop.

## The gate

A generated test is kept only if it:

1. compiles,
2. passes against the unmodified code, every time it runs, and
3. kills the specific mutant it was written for.

A test that fails check 1 or 2 gets one repair: the model sees what went
wrong and tries once more. A test that still fails, or that misses its
mutant, is discarded. A program makes that call, not a person.

## Example

One of PIT's mutants in tarballfs changes the last line of
`SingleMemberArchivePaths.pathExists`:

```diff
- return entryName.indexOf('/') < 0;
+ return entryName.indexOf('/') <= 0;
```

With the change, an entry name that starts with a slash, such as `/name`,
counts as existing. The existing tests run this line, but none fails on the
change. The model's first try passed the gate:

```java
@Test
void pathExistsHandlesSlashPositions() {
  assertTrue(SingleMemberArchivePaths.pathExists("name"));
  assertFalse(SingleMemberArchivePaths.pathExists("/name"));
  assertFalse(SingleMemberArchivePaths.pathExists("dir/name"));
}
```

On the mutant, the second assertion fails, so the test kills it
([full file](results/tarballfs/targeted/kept/src/test/java/io/github/iachimoe/tarballfs/SingleMemberArchivePathsMutaGate1Test.java)).

## Status

Targeted runs work end to end: MutaGate finds the mutants a class's tests
miss, asks a model for one test per mutant, and keeps only the tests that
kill their own mutant. An untargeted control run, with one request per
method and no mutant named, measures what targeting adds.

## Results

Every selected class of two repositories,
[codegen](https://github.com/describeadmin/codegen) at `7697931` and
[tarballfs](https://github.com/iachimoe/tarballfs) at `9d0d8eb`, with Claude
Sonnet 5.5 at medium effort through Claude Code. The rest of the corpus
follows.

Both were written mostly by AI and have thin test suites, the case MutaGate
focuses on: tarballfs's README says it was entirely generated with Cursor, 23
of codegen's 28 commits have an AI co-author, and most classes in each have no
test class of their own. How they were chosen is in [`corpus/`](corpus/).

| Repository | Run | Mutation score, whole module | Tests kept | Kept tests that added a new kill | Attempts per kept test |
|---|---|---|---|---|---|
| codegen | Targeted | 60.4% → 96.5% (139 → 222 of 230 mutants) | 80 of 91 generated (88%) | 53 | 1.25 |
| codegen | Control | 60.4% → 83.9% (139 → 193) | 17 of 27 (63%) | 12 | 1.59 |
| tarballfs | Targeted | 39.9% → 96.7% (288 → 698 of 722 mutants) | 407 of 434 generated (94%) | 189 | 1.14 |
| tarballfs | Control | 39.9% → 92.7% (288 → 669) | 190 of 243 (78%) | 117 | 1.35 |

The control asks for a test of one of the class's methods in place of a
mutant: one request per method, longest first, and never more requests than
the class has targets. Compare mutants killed, not tests kept: a kept test
adds a new kill only if it kills a mutant no earlier kept test in its class
had killed.

On codegen, every mutant the control's tests killed, a targeted test killed
too; targeting killed 29 more. A first control run named methods again until
it matched the targeted run's 91 requests; it killed the same 54 mutants, so
its 64 repeated requests added nothing but cost.

On tarballfs the gap is narrower; why is not yet known. Targeting killed 31
mutants the control missed. The control killed 2 that targeting missed: with
the targeted tests, both of those mutants timed out, and PIT names no test for
a timeout, so the gate credited none.

Each request sends what every request about the class shares (the rules, the
class, its collaborators, its tests) as the system prompt, so later requests
read it from Claude's prompt cache. On codegen, against a first run without
caching, which kept the same 80 of 91 and reached 96.1%, input written to the
cache fell 71%, from 1,040,192 to 300,605 tokens, with 762,851 read from it
instead. On tarballfs, every request after a class's first read that system
prompt from the cache; the targeted run read 2,404,864 input tokens from it
and wrote 600,813.

Measured on Java 21, Maven 3.9.16, and PIT 1.30.0. Every run's details,
including each model reply and every kept test, are in [`results/`](results/).

## Limits

- Each result is from one run. Model replies vary, so another run can give
  different numbers; how much they vary is not measured.
- Some mutants change nothing a test can observe, so no test can kill them.
  They still count as missed.
- The gate keeps only tests that catch something. Whether a test is readable
  and worth keeping is left to whoever merges it.
- A kept test must pass on the code as it is, so where that code is wrong,
  the test can lock the wrong behavior in.
- A class's candidate tests are checked together, in one PIT run. If that run
  fails, all of them are rejected, good ones included. A mutant that times out
  earns no credit, since PIT names no test for a timeout.

## Running it

### Requirements

- Python 3.12 or newer.
- JDK 21 and Maven, with Maven running on JDK 21: `mvn -v` must report
  `Java version: 21`. MutaGate stops otherwise; point `JAVA_HOME` at a JDK 21.
- git.
- For model calls: [Claude Code](https://claude.com/claude-code), logged in
  with a Claude subscription. An API key is refused for now. If `claude` is
  not on your PATH, set `MUTAGATE_CLAUDE` to its path.

PIT needs no install: Maven downloads it on first use. MutaGate works on a
temporary copy of the target repository and never modifies the original.
Developed and measured on macOS; other systems are untested.

### Install

```sh
git clone https://github.com/kamil-urbanowski/mutagate.git
cd mutagate
python3 -m venv .venv
source .venv/bin/activate
pip install -e ".[dev]"
```

Run every command from this folder: MutaGate reads and writes `corpus/`,
`results/`, and `.mutagate/` here.

### See the committed results

```sh
mutagate summarize codegen
mutagate summarize tarballfs
```

Needs only the install. Each recomputes its repository's rows of the Results
table above from the committed runs and prints them, with one line per class.

### Run it on codegen

1. Clone codegen into `work/` at the commit its baseline was measured on. The
   folder name must match the repository's folder in [`corpus/`](corpus/).

   ```sh
   git clone https://github.com/describeadmin/codegen.git work/codegen
   git -C work/codegen checkout 7697931
   ```

2. Run one class. `Layout` is the smallest: its tests miss one mutant.

   ```sh
   mutagate run --repo work/codegen --target io.github.describeadmin.codegen.model.Layout
   ```

   MutaGate asks the model for a test aimed at each mutant the class's tests
   miss, and keeps only the tests that pass the gate. It prints each test's
   verdict and the class's mutation score before and after, and replaces that
   class's committed run in `results/`. Model replies vary, so the numbers can
   differ from the committed run.

3. Recompute the whole-module scores: `mutagate summarize codegen`.

Requests count against your Claude plan's usage limits: one per missed mutant,
plus one retry when its test fails the gate's first two checks. The committed
codegen run covered all 10 classes, 91 missed mutants, in 100 requests;
`mutagate summarize codegen` lists the class names. Every reply is saved in
`.mutagate/replies/`, so if a run stops partway, at a usage limit for example,
running the same command again sends only the requests not yet answered.

To use a local model instead, on an Apple Silicon Mac, install with
`pip install -e ".[local]"` and add `--provider local`. MutaGate then runs
gpt-oss-20b, OpenAI's open-weight model, on your machine through
[mlx-lm](https://github.com/ml-explore/mlx-lm), using none of your Claude
plan. The first run downloads its 12 GB of weights, and while it runs it takes
about 13 GB of memory (measured on a 24 GB M4 Pro). Its runs are saved apart
from Claude's, in `results/<repo>/local/`, and
`mutagate summarize <repo> --provider local` sums them up.

To use another model, or no Claude Code, add `--provider offline`. MutaGate
then writes each prompt to `.mutagate/offline/prompt.txt` and waits for you to
save the model's reply to `.mutagate/offline/response.txt`.

### On another repository

MutaGate needs a single-module Maven project that builds on JDK 21, with
JUnit 5 tests, code in `src/main/java`, tests in `src/test/java`, and every
class in a package. A `pom.xml` that already declares PIT is not handled yet.

1. Clone it into `work/<name>`.
2. Record, by fully qualified name, in `corpus/<name>/`:
   - `exclusions.json`: each class to leave out with a one-line reason, or
     `{}`. The corpus leaves out classes that need a Spring context, a
     database, or another live service to run in a unit test.
   - `failing-tests.json`: existing test classes that fail on the unmodified
     code, or `[]`.

   MutaGate refuses to run without both.
3. `mutagate baseline --repo work/<name>` runs PIT across the whole project,
   saves `corpus/<name>/baseline.json`, and prints the baseline mutation score
   and the classes selected for runs.
4. `mutagate run` on each selected class, then `mutagate summarize <name>`.

### Tests

```sh
pytest                          # unit tests: a few seconds, no Java or model
MUTAGATE_INTEGRATION=1 pytest   # adds real Maven builds and PIT runs; still no model
MUTAGATE_LOCAL_MODEL=1 pytest tests/test_local_model.py   # one request to the local model
```

The integration tests need git, Maven, and JDK 21. They clone
GameZoneUnicesar into `work/` on first use and take a few minutes.

## License

MIT. See [LICENSE](LICENSE).
