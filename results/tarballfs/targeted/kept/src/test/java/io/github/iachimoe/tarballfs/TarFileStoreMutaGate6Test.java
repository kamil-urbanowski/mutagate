package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.PosixFileAttributeView;
import org.junit.jupiter.api.Test;

class TarFileStoreMutaGate6Test {

  @Test
  void supportsFileAttributeViewByClass() {
    TarFileStore store = new TarFileStore(null);

    assertTrue(store.supportsFileAttributeView(BasicFileAttributeView.class));
    assertTrue(store.supportsFileAttributeView(PosixFileAttributeView.class));
    assertFalse(store.supportsFileAttributeView(FileOwnerAttributeView.class));
    assertFalse(store.supportsFileAttributeView(FileAttributeView.class));
    assertFalse(store.supportsFileAttributeView((Class<? extends FileAttributeView>) null));
  }
}
