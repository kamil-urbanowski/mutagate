package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.PosixFileAttributeView;
import org.junit.jupiter.api.Test;

class ReadOnlyCompressorFileStoreMutaGate6Test {

  @Test
  void supportsFileAttributeViewByClass() {
    Path archive = Paths.get("archive.tar.gz");
    ReadOnlyCompressorFileStore store = new ReadOnlyCompressorFileStore(archive, "gzip");

    Class<? extends FileAttributeView> nullType = null;
    assertFalse(store.supportsFileAttributeView(nullType));
    assertTrue(store.supportsFileAttributeView(BasicFileAttributeView.class));
    assertTrue(store.supportsFileAttributeView(PosixFileAttributeView.class));
    assertFalse(store.supportsFileAttributeView(FileOwnerAttributeView.class));
  }
}
