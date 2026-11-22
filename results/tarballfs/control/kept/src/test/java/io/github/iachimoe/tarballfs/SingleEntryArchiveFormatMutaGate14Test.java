package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.Collections;
import java.util.Set;
import org.junit.jupiter.api.Test;

class SingleEntryArchiveFormatMutaGate14Test {

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
      return Collections.singleton("basic");
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
  void bzip2NewBasicAttributeViewReturnsBzip2View() {
    StubFileSystem fs = new StubFileSystem();
    SingleEntryArchivePath bzPath =
        new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.BZIP2, true, "entry");
    SingleEntryArchivePath gzPath =
        new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.GZIP, true, "entry");

    BasicFileAttributeView bzView = SingleEntryArchiveFormat.BZIP2.newBasicAttributeView(bzPath);
    BasicFileAttributeView gzView = SingleEntryArchiveFormat.GZIP.newBasicAttributeView(gzPath);

    assertNotNull(bzView);
    assertTrue(bzView instanceof Bzip2BasicFileAttributeView);
    assertEquals(Bzip2BasicFileAttributeView.class, bzView.getClass());
    assertTrue(gzView instanceof GzipBasicFileAttributeView);
    assertNotSame(bzView.getClass(), gzView.getClass());
    assertNotSame(bzView, SingleEntryArchiveFormat.BZIP2.newBasicAttributeView(bzPath));
  }
}
