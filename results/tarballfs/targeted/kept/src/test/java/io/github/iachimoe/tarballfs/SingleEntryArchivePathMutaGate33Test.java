package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

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

class SingleEntryArchivePathMutaGate33Test {

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
  void getParentHandlesSingleSegmentAndMultiSegmentPaths() {
    FileSystem fs = new StubFileSystem();
    SingleEntryArchiveFormat format = SingleEntryArchiveFormat.GZIP;

    SingleEntryArchivePath relativeSingle = new SingleEntryArchivePath(fs, format, false, "a");
    assertNull(relativeSingle.getParent());

    SingleEntryArchivePath absoluteSingle = new SingleEntryArchivePath(fs, format, true, "a");
    Path rootParent = absoluteSingle.getParent();
    assertNotNull(rootParent);
    assertEquals(new SingleEntryArchivePath(fs, format, true, ""), rootParent);
    assertEquals("/", rootParent.toString());

    SingleEntryArchivePath relativeDouble = new SingleEntryArchivePath(fs, format, false, "a/b");
    Path parent = relativeDouble.getParent();
    assertNotNull(parent);
    assertEquals("a", parent.toString());
    assertEquals(1, parent.getNameCount());
  }
}
