package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.ClosedFileSystemException;
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

class SingleEntryArchivePathMutaGate98Test {

  private static final class StubFileSystem extends FileSystem {
    private boolean open;

    StubFileSystem(boolean open) {
      this.open = open;
    }

    @Override
    public FileSystemProvider provider() {
      throw new UnsupportedOperationException();
    }

    @Override
    public void close() throws IOException {
      open = false;
    }

    @Override
    public boolean isOpen() {
      return open;
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
      return Set.of("basic");
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
  void toFileChecksFileSystemOpenState() {
    StubFileSystem openFs = new StubFileSystem(true);
    SingleEntryArchivePath openPath =
        new SingleEntryArchivePath(openFs, SingleEntryArchiveFormat.GZIP, false, "entry.txt");
    // Open file system: proceeds past the check and rejects non-root entries.
    assertThrows(UnsupportedOperationException.class, openPath::toFile);

    StubFileSystem closedFs = new StubFileSystem(false);
    SingleEntryArchivePath closedPath =
        new SingleEntryArchivePath(closedFs, SingleEntryArchiveFormat.GZIP, false, "entry.txt");
    // Closed file system: must fail with ClosedFileSystemException.
    assertThrows(ClosedFileSystemException.class, closedPath::toFile);
  }
}
