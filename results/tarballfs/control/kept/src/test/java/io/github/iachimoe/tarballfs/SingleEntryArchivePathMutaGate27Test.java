package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchService;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.Collections;
import java.util.Set;
import org.junit.jupiter.api.Test;

class SingleEntryArchivePathMutaGate27Test {

  private static final class StubFileSystem extends FileSystem {
    @Override
    public FileSystemProvider provider() {
      return null;
    }

    @Override
    public void close() throws IOException {}

    @Override
    public boolean isOpen() {
      return true;
    }

    @Override
    public boolean isReadOnly() {
      return true;
    }

    @Override
    public String getSeparator() {
      return "/";
    }

    @Override
    public Iterable<Path> getRootDirectories() {
      return Collections.emptyList();
    }

    @Override
    public Iterable<FileStore> getFileStores() {
      return Collections.emptyList();
    }

    @Override
    public Set<String> supportedFileAttributeViews() {
      return Collections.emptySet();
    }

    @Override
    public Path getPath(String first, String... more) {
      throw new UnsupportedOperationException();
    }

    @Override
    public PathMatcher getPathMatcher(String syntaxAndPattern) {
      throw new UnsupportedOperationException();
    }

    @Override
    public UserPrincipalLookupService getUserPrincipalLookupService() {
      throw new UnsupportedOperationException();
    }

    @Override
    public WatchService newWatchService() throws IOException {
      throw new UnsupportedOperationException();
    }
  }

  @Test
  void resolveStringAppendsSegmentsAndKeepsBaseAbsoluteness() {
    StubFileSystem fs = new StubFileSystem();
    SingleEntryArchiveFormat format = SingleEntryArchiveFormat.GZIP;

    SingleEntryArchivePath relativeBase = new SingleEntryArchivePath(fs, format, false, "a/b");
    Path r1 = relativeBase.resolve("c/d");
    assertEquals("a/b/c/d", r1.toString());
    assertFalse(r1.isAbsolute());
    assertEquals(4, r1.getNameCount());
    assertEquals(new SingleEntryArchivePath(fs, format, false, "a/b/c/d"), r1);

    SingleEntryArchivePath absoluteBase = new SingleEntryArchivePath(fs, format, true, "a");
    Path r2 = absoluteBase.resolve("b");
    assertEquals("/a/b", r2.toString());
    assertTrue(r2.isAbsolute());
    assertEquals(new SingleEntryArchivePath(fs, format, true, "a/b"), r2);

    SingleEntryArchivePath root = new SingleEntryArchivePath(fs, format, true, "");
    Path r3 = root.resolve("x");
    assertEquals("/x", r3.toString());
    assertTrue(r3.isAbsolute());

    SingleEntryArchivePath emptyRelative = new SingleEntryArchivePath(fs, format, false, "");
    Path r4 = emptyRelative.resolve("x");
    assertEquals("x", r4.toString());
    assertFalse(r4.isAbsolute());

    assertEquals("a/b", relativeBase.toString());
  }
}
