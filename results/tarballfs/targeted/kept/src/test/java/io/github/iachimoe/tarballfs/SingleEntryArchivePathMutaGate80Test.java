package io.github.iachimoe.tarballfs;

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

class SingleEntryArchivePathMutaGate80Test {

  private static final class StubFileSystem extends FileSystem {
    @Override
    public FileSystemProvider provider() {
      return null;
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
  void startsWithRequiresSameFormatFileSystemAndAbsoluteness() {
    FileSystem fs1 = new StubFileSystem();
    FileSystem fs2 = new StubFileSystem();

    SingleEntryArchivePath base =
        new SingleEntryArchivePath(fs1, SingleEntryArchiveFormat.GZIP, false, "a/b");

    assertTrue(base.startsWith(
        new SingleEntryArchivePath(fs1, SingleEntryArchiveFormat.GZIP, false, "a")));
    assertFalse(base.startsWith(
        new SingleEntryArchivePath(fs2, SingleEntryArchiveFormat.GZIP, false, "a")));
    assertFalse(base.startsWith(
        new SingleEntryArchivePath(fs1, SingleEntryArchiveFormat.BZIP2, false, "a")));
    assertFalse(base.startsWith(
        new SingleEntryArchivePath(fs1, SingleEntryArchiveFormat.GZIP, true, "a")));
  }
}
