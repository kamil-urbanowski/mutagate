# Results

Committed run output, one folder per repository:

```
<repo>/summary.json            whole-module headline scores and metrics, per run kind
<repo>/targeted/<class>.json   one targeted run of one class
<repo>/targeted/kept/          the kept tests, at their paths in the repository
```

Each run file holds the setup it was measured on (the repository's and
MutaGate's commits; the Java, Maven, and PIT versions; the model and its
settings), the class's mutation score before and after, the acceptance rate,
attempts per kept test, the run ledger (one verdict per candidate test, with
its source), and every request to the model with its reply and the tokens it
used. Only tests that passed the gate are written to `kept/`.

Control runs, once built, go in `<repo>/control/` the same way, and the
summary reports them separately.
