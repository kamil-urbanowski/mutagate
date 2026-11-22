package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

class SingleEntryArchivePathMutaGate91Test {

  private static final class StubFileSystem extends FileSystem {
    @Override
    public FileSystemProvider provider() {
      return null;
    }

    @Override
    public void close() {
    }

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
      return null;
    }

    @Override
    public PathMatcher getPathMatcher(String syntaxAndPattern) {
      return null;
    }

    @Override
    public UserPrincipalLookupService getUserPrincipalLookupService() {
      return null;
    }

    @Override
    public WatchService newWatchService() throws IOException {
      return null;
    }
  }

  @Test
  void subpathBoundaries() {
    SingleEntryArchivePath path =
        new SingleEntryArchivePath(new StubFileSystem(), SingleEntryArchiveFormat.GZIP, false, "a/b/c");
    assertEquals(3, path.getNameCount());

    // Valid at the boundaries: begin == 0 and end == length
    assertEquals("a/b/c", path.subpath(0, 3).toString());
    assertEquals("a", path.subpath(0, 1).toString());
    assertEquals("c", path.subpath(2, 3).toString());

    // begin == end must be rejected
    assertThrows(IllegalArgumentException.class, () -> path.subpath(1, 1));
    assertThrows(IllegalArgumentException.class, () -> path.subpath(3, 3));

    // Out of range values
    assertThrows(IllegalArgumentException.class, () -> path.subpath(-1, 2));
    assertThrows(IllegalArgumentException.class, () -> path.subpath(0, 4));
    assertThrows(IllegalArgumentException.class, () -> path.subpath(2, 1));
  }
}
