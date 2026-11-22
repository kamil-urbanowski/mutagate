package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.PosixFileAttributeView;
import org.junit.jupiter.api.Test;

class ReadOnlyCompressorFileStoreMutaGate5Test {

  @Test
  void supportsFileAttributeViewByClass() {
    Path archive = Paths.get("archive.txt.gz");
    ReadOnlyCompressorFileStore store = new ReadOnlyCompressorFileStore(archive, "gz");

    assertTrue(store.supportsFileAttributeView(BasicFileAttributeView.class));
    assertTrue(store.supportsFileAttributeView(PosixFileAttributeView.class));
    assertFalse(store.supportsFileAttributeView(FileOwnerAttributeView.class));
    assertFalse(store.supportsFileAttributeView((Class<BasicFileAttributeView>) null));
  }
}
