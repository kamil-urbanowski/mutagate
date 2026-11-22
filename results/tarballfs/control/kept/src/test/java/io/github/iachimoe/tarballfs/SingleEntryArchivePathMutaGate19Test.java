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
import java.util.Collections;
import java.util.Set;
import org.junit.jupiter.api.Test;

class SingleEntryArchivePathMutaGate19Test {

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
  void startsWithStringComparesWholeSegmentsAndRelativeForm() {
    FileSystem fs = new StubFileSystem();
    SingleEntryArchivePath relative =
        new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.GZIP, false, "a/b/c");
    SingleEntryArchivePath absolute =
        new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.GZIP, true, "a/b/c");

    assertTrue(relative.startsWith("a"));
    assertTrue(relative.startsWith("a/b"));
    assertTrue(relative.startsWith("a/b/c"));

    assertFalse(relative.startsWith("b"));
    assertFalse(relative.startsWith("a/c"));
    assertFalse(relative.startsWith("a/b/c/d"));
    assertFalse(relative.startsWith("a/bc"));

    // The string is parsed as a relative path, so an absolute path never matches.
    assertFalse(absolute.startsWith("a"));
    assertFalse(absolute.startsWith("a/b/c"));

    assertThrows(NullPointerException.class, () -> relative.startsWith((String) null));
  }
}
