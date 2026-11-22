package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import java.util.Set;
import org.junit.jupiter.api.Test;

class SingleEntryArchivePathMutaGate11Test {

  private static final class StubFileSystem extends FileSystem {
    private final boolean open;

    StubFileSystem(boolean open) {
      this.open = open;
    }

    @Override
    public FileSystemProvider provider() {
      throw new UnsupportedOperationException();
    }

    @Override
    public void close() throws IOException {
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
      throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<FileStore> getFileStores() {
      throw new UnsupportedOperationException();
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
    public WatchService newWatchService() {
      throw new UnsupportedOperationException();
    }
  }

  @Test
  void toFileFailsForClosedFileSystemAndForNonEmptyEntries() {
    for (SingleEntryArchiveFormat format : SingleEntryArchiveFormat.values()) {
      FileSystem closed = new StubFileSystem(false);
      assertThrows(ClosedFileSystemException.class,
          () -> new SingleEntryArchivePath(closed, format, true, "").toFile());
      assertThrows(ClosedFileSystemException.class,
          () -> new SingleEntryArchivePath(closed, format, false, "a/b").toFile());

      FileSystem open = new StubFileSystem(true);
      UnsupportedOperationException relative = assertThrows(UnsupportedOperationException.class,
          () -> new SingleEntryArchivePath(open, format, false, "entry.txt").toFile());
      assertEquals(format.toHostFileUnsupportedMessage(), relative.getMessage());

      UnsupportedOperationException absolute = assertThrows(UnsupportedOperationException.class,
          () -> new SingleEntryArchivePath(open, format, true, "dir/entry.txt").toFile());
      assertEquals(format.toHostFileUnsupportedMessage(), absolute.getMessage());
    }
  }
}
