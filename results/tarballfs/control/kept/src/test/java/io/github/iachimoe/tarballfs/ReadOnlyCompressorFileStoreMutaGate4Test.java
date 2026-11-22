package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

class ReadOnlyCompressorFileStoreMutaGate4Test {

  @Test
  void isReadOnlyReturnsTrueRegardlessOfArchiveAndType() {
    Path first = Paths.get("archive.tar.gz");
    Path second = Paths.get("some", "dir", "other.xz");

    ReadOnlyCompressorFileStore gzStore = new ReadOnlyCompressorFileStore(first, "gz");
    ReadOnlyCompressorFileStore xzStore = new ReadOnlyCompressorFileStore(second, "xz");
    ReadOnlyCompressorFileStore nullTypeStore = new ReadOnlyCompressorFileStore(first, null);

    assertTrue(gzStore.isReadOnly());
    assertTrue(xzStore.isReadOnly());
    assertTrue(nullTypeStore.isReadOnly());
  }
}
