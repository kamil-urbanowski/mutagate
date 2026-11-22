package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchService;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class SingleEntryArchivePathMutaGate29Test {

  private static final class StubFileSystem extends FileSystem {
    @Override
    public FileSystemProvider provider() {
      return null;
    }

    @Override
    public void close() {}

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
      return List.of();
    }

    @Override
    public Iterable<FileStore> getFileStores() {
      return List.of();
    }

    @Override
    public Set<String> supportedFileAttributeViews() {
      return Set.of();
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
  void toAbsolutePathBehavior() {
    FileSystem fs = new StubFileSystem();
    SingleEntryArchiveFormat format = SingleEntryArchiveFormat.GZIP;

    SingleEntryArchivePath relative = new SingleEntryArchivePath(fs, format, false, "a/b");
    Path abs = relative.toAbsolutePath();

    assertNotSame(relative, abs);
    assertTrue(abs.isAbsolute());
    assertFalse(relative.isAbsolute());
    assertEquals("/a/b", abs.toString());
    assertEquals("a/b", relative.toString());
    assertSame(fs, abs.getFileSystem());
    assertEquals(2, abs.getNameCount());
    assertEquals(new SingleEntryArchivePath(fs, format, true, "a/b"), abs);
    assertEquals("a/b", ((SingleEntryArchivePath) abs).entryName());
    assertSame(format, ((SingleEntryArchivePath) abs).format());

    // Already absolute: returns the same instance
    assertSame(abs, abs.toAbsolutePath());

    // Empty relative path becomes root
    Path emptyAbs = new SingleEntryArchivePath(fs, format, false, "").toAbsolutePath();
    assertTrue(emptyAbs.isAbsolute());
    assertEquals("/", emptyAbs.toString());
    assertEquals(0, emptyAbs.getNameCount());

    // Format is preserved for other formats too
    SingleEntryArchivePath bz = new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.BZIP2, false, "x");
    assertSame(SingleEntryArchiveFormat.BZIP2, ((SingleEntryArchivePath) bz.toAbsolutePath()).format());
  }
}
