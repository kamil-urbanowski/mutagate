package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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

class SingleEntryArchivePathMutaGate72Test {

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
  void resolveSiblingOnPathWithoutParentResolvesOtherAgainstItself() {
    FileSystem fs = new StubFileSystem();
    SingleEntryArchivePath path =
        new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.GZIP, false, "foo");
    SingleEntryArchivePath other =
        new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.GZIP, false, "bar");

    Path result = path.resolveSibling(other);

    assertNotNull(result);
    assertEquals("foo/bar", result.toString());
    assertEquals(
        new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.GZIP, false, "foo/bar"), result);
  }
}
