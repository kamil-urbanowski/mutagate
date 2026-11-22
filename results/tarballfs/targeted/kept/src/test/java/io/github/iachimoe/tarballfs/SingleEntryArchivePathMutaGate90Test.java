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
import java.util.Set;
import org.junit.jupiter.api.Test;

class SingleEntryArchivePathMutaGate90Test {

  private static final class StubFileSystem extends FileSystem {
    @Override
    public FileSystemProvider provider() {
      throw new UnsupportedOperationException();
    }

    @Override
    public void close() throws IOException {
      throw new UnsupportedOperationException();
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
      throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<FileStore> getFileStores() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> supportedFileAttributeViews() {
      throw new UnsupportedOperationException();
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
  void subpathBoundaries() {
    FileSystem fs = new StubFileSystem();
    SingleEntryArchivePath path =
        new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.GZIP, false, "a/b/c");

    // Valid extremes: begin == 0 and end == segment count
    assertEquals("a/b/c", path.subpath(0, 3).toString());
    assertEquals("a", path.subpath(0, 1).toString());
    assertEquals("c", path.subpath(2, 3).toString());
    assertEquals("b/c", path.subpath(1, 3).toString());

    // Invalid: begin == end
    assertThrows(IllegalArgumentException.class, () -> path.subpath(1, 1));
    assertThrows(IllegalArgumentException.class, () -> path.subpath(0, 0));
    // Invalid: begin > end
    assertThrows(IllegalArgumentException.class, () -> path.subpath(2, 1));
    // Invalid: negative begin
    assertThrows(IllegalArgumentException.class, () -> path.subpath(-1, 2));
    // Invalid: end beyond count
    assertThrows(IllegalArgumentException.class, () -> path.subpath(0, 4));
  }
}
