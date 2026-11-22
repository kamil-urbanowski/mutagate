package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.PosixFileAttributeView;
import org.junit.jupiter.api.Test;

class ReadOnlyCompressorFileStoreMutaGate8Test {

  interface CustomBasicView extends BasicFileAttributeView {}

  @Test
  void supportsFileAttributeViewByClass() {
    Path path = Paths.get("archive.gz");
    ReadOnlyCompressorFileStore store = new ReadOnlyCompressorFileStore(path, "gzip");

    assertTrue(store.supportsFileAttributeView(BasicFileAttributeView.class));
    assertTrue(store.supportsFileAttributeView(CustomBasicView.class));
    assertTrue(store.supportsFileAttributeView(DosFileAttributeView.class));
    assertTrue(store.supportsFileAttributeView(PosixFileAttributeView.class));

    assertFalse(store.supportsFileAttributeView(FileOwnerAttributeView.class));
    assertFalse(store.supportsFileAttributeView(FileAttributeView.class));
    assertFalse(store.supportsFileAttributeView((Class<? extends FileAttributeView>) null));
  }
}
