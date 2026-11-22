package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

class SingleEntryArchivePathMutaGate28Test {

  private static final class StubFileSystem extends FileSystem {
    @Override
    public FileSystemProvider provider() {
      throw new UnsupportedOperationException();
    }

    @Override
    public void close() throws IOException {
      // no-op
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
  void resolveSiblingWithStringReplacesLastSegment() {
    FileSystem fs = new StubFileSystem();
    SingleEntryArchiveFormat format = SingleEntryArchiveFormat.GZIP;

    // Relative, multi-segment: sibling is placed under the parent.
    Path relative = new SingleEntryArchivePath(fs, format, false, "a/b/c");
    Path r1 = relative.resolveSibling("d");
    assertEquals("a/b/d", r1.toString());
    assertEquals(new SingleEntryArchivePath(fs, format, false, "a/b/d"), r1);
    assertEquals(false, r1.isAbsolute());

    // Multi-segment sibling argument.
    assertEquals("a/b/d/e", relative.resolveSibling("d/e").toString());

    // Absolute, multi-segment: absoluteness is preserved.
    Path absolute = new SingleEntryArchivePath(fs, format, true, "a/b");
    Path r2 = absolute.resolveSibling("x");
    assertEquals("/a/x", r2.toString());
    assertEquals(new SingleEntryArchivePath(fs, format, true, "a/x"), r2);
    assertEquals(true, r2.isAbsolute());

    // Absolute single segment: parent is the root.
    Path r3 = new SingleEntryArchivePath(fs, format, true, "a").resolveSibling("x");
    assertEquals("/x", r3.toString());
    assertEquals(true, r3.isAbsolute());

    // Relative single segment: no parent, so the argument is resolved against this path.
    Path r4 = new SingleEntryArchivePath(fs, format, false, "a").resolveSibling("x");
    assertEquals("a/x", r4.toString());
    assertEquals(false, r4.isAbsolute());

    // Empty relative path: no parent, result is just the argument.
    Path r5 = new SingleEntryArchivePath(fs, format, false, "").resolveSibling("x");
    assertEquals("x", r5.toString());
  }
}
