package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

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

class SingleEntryArchivePathMutaGate14Test {

  private static final class StubFileSystem extends FileSystem {
    @Override
    public FileSystemProvider provider() {
      throw new UnsupportedOperationException();
    }

    @Override
    public void close() throws IOException {
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
  void resolveSiblingReplacesLastSegmentOrFallsBackToResolve() {
    FileSystem fs = new StubFileSystem();
    SingleEntryArchiveFormat f = SingleEntryArchiveFormat.GZIP;

    SingleEntryArchivePath nested = new SingleEntryArchivePath(fs, f, false, "a/b");
    Path other = new SingleEntryArchivePath(fs, f, false, "c");
    assertEquals("a/c", nested.resolveSibling(other).toString());

    SingleEntryArchivePath deep = new SingleEntryArchivePath(fs, f, false, "a/b/c");
    Path multi = new SingleEntryArchivePath(fs, f, false, "d/e");
    assertEquals("a/b/d/e", deep.resolveSibling(multi).toString());

    SingleEntryArchivePath absNested = new SingleEntryArchivePath(fs, f, true, "a/b");
    assertEquals("/a/c", absNested.resolveSibling(other).toString());

    // single relative segment: no parent, falls back to resolve (appends)
    SingleEntryArchivePath single = new SingleEntryArchivePath(fs, f, false, "a");
    assertEquals("a/c", single.resolveSibling(other).toString());

    // single absolute segment: parent is root
    SingleEntryArchivePath absSingle = new SingleEntryArchivePath(fs, f, true, "a");
    assertEquals("/c", absSingle.resolveSibling(other).toString());

    // absolute root: no parent, falls back to resolve
    SingleEntryArchivePath root = new SingleEntryArchivePath(fs, f, true, "");
    assertEquals("/c", root.resolveSibling(other).toString());

    // absolute other path is returned as is
    SingleEntryArchivePath absOther = new SingleEntryArchivePath(fs, f, true, "x/y");
    assertSame(absOther, nested.resolveSibling(absOther));
    assertSame(absOther, single.resolveSibling(absOther));
  }
}
