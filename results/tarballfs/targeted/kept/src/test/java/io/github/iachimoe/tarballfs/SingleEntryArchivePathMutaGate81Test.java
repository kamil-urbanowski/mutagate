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

class SingleEntryArchivePathMutaGate81Test {

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
  void startsWithRequiresSameFormatFileSystemAndAbsoluteness() {
    FileSystem fs = new StubFileSystem();
    FileSystem otherFs = new StubFileSystem();

    SingleEntryArchivePath path =
        new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.GZIP, false, "a/b");
    SingleEntryArchivePath sameKind =
        new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.GZIP, false, "a");
    SingleEntryArchivePath otherFormat =
        new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.BZIP2, false, "a");
    SingleEntryArchivePath otherFileSystem =
        new SingleEntryArchivePath(otherFs, SingleEntryArchiveFormat.GZIP, false, "a");
    SingleEntryArchivePath otherAbsolute =
        new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.GZIP, true, "a");

    assertTrue(path.startsWith(sameKind));
    assertFalse(path.startsWith(otherFormat));
    assertFalse(path.startsWith(otherFileSystem));
    assertFalse(path.startsWith(otherAbsolute));
  }
}
