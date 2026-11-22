package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.PosixFileAttributeView;
import org.junit.jupiter.api.Test;

class ReadOnlyCompressorFileStoreMutaGate7Test {

  @Test
  void supportsFileAttributeViewByClass() {
    ReadOnlyCompressorFileStore store =
        new ReadOnlyCompressorFileStore(Path.of("archive.tar.gz"), "gz");

    assertTrue(store.supportsFileAttributeView(BasicFileAttributeView.class));
    assertFalse(store.supportsFileAttributeView(FileOwnerAttributeView.class));
    assertFalse(store.supportsFileAttributeView((Class<BasicFileAttributeView>) null));
  }
}
