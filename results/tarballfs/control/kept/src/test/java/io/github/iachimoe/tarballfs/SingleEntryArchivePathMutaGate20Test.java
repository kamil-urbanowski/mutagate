package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

class SingleEntryArchivePathMutaGate20Test {

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
  void endsWithStringMatchesTrailingSegments() {
    FileSystem fs = new StubFileSystem();
    SingleEntryArchivePath relative =
        new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.GZIP, false, "a/b/c");
    SingleEntryArchivePath absolute =
        new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.GZIP, true, "a/b/c");

    assertTrue(relative.endsWith("c"));
    assertTrue(relative.endsWith("b/c"));
    assertTrue(relative.endsWith("a/b/c"));

    assertFalse(relative.endsWith("b"));
    assertFalse(relative.endsWith("a/b"));
    assertFalse(relative.endsWith("x/c"));
    assertFalse(relative.endsWith("z/a/b/c"));

    // Absolute receiver: the relative argument still matches trailing segments.
    assertTrue(absolute.endsWith("c"));
    assertTrue(absolute.endsWith("b/c"));
    assertTrue(absolute.endsWith("a/b/c"));
    assertFalse(absolute.endsWith("a/b"));

    assertThrows(NullPointerException.class, () -> relative.endsWith((String) null));
  }
}
