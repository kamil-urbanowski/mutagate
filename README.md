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
2. passes against the unmodified code, every time it runs, and
3. kills the specific mutant it was written for.

Anything else is discarded. A program makes that call, not a person.

## Status

Targeted runs work end to end: MutaGate finds the mutants a class's tests
miss, asks a model for one test per mutant, and keeps only the tests that
kill their own mutant. An untargeted control run, with as many requests and
no mutant named, measures what targeting adds.

## Results

Every selected class of
[codegen](https://github.com/describeadmin/codegen) at `7697931`, with Claude
Sonnet 5.5 at medium effort through Claude Code. One repository so far; the
rest of the corpus follows.

| Measure | Targeted | Control |
|---|---|---|
| Mutation score, whole module | 60.4% → 96.5% (139 → 222 of 230 mutants) | 60.4% → 83.9% (139 → 193) |
| Tests kept | 80 of 91 generated (88%) | 62 of 91 (68%) |
| Attempts per kept test | 1.25 | 1.47 |

The control asks for a test of one of the class's methods in place of a
mutant, with as many requests as the targeted run. Every mutant its tests
killed, a targeted test killed too; targeting killed 29 more.

Each request sends what every request about the class shares (the rules, the
class, its collaborators, its tests) as the system prompt, so later requests
read it from Claude's prompt cache. Against a first run without caching, which
kept the same 80 of 91 and reached 96.1%, input written to the cache fell 71%,
from 1,040,192 to 300,605 tokens, with 762,851 read from it instead.

Measured on Java 21, Maven 3.9.16, and PIT 1.30.0. Every run's details,
including each model reply and every kept test, are in [`results/`](results/).

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
```

Needs only the install. It recomputes the Results table above from the
committed runs and prints it, with one line per class.

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
```

The integration tests need git, Maven, and JDK 21. They clone
GameZoneUnicesar into `work/` on first use and take a few minutes.
