package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.PosixFileAttributeView;
import org.junit.jupiter.api.Test;

class TarFileStoreMutaGate7Test {

  interface CustomBasicView extends BasicFileAttributeView {}

  interface UnrelatedView extends FileAttributeView {}

  @Test
  void supportsFileAttributeViewByClassChecksBasicAssignability() {
    TarFileStore store = new TarFileStore(null);

    assertFalse(store.supportsFileAttributeView((Class<? extends FileAttributeView>) null));

    assertTrue(store.supportsFileAttributeView(BasicFileAttributeView.class));

    assertTrue(store.supportsFileAttributeView(PosixFileAttributeView.class));
    assertTrue(store.supportsFileAttributeView(DosFileAttributeView.class));
    assertTrue(store.supportsFileAttributeView(CustomBasicView.class));

    assertFalse(store.supportsFileAttributeView(FileAttributeView.class));
    assertFalse(store.supportsFileAttributeView(FileOwnerAttributeView.class));
    assertFalse(store.supportsFileAttributeView(AclFileAttributeView.class));
    assertFalse(store.supportsFileAttributeView(UnrelatedView.class));
  }
}
