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

class SingleEntryArchivePathMutaGate45Test {

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

  private static SingleEntryArchivePath path(FileSystem fs, boolean absolute, String name) {
    return new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.GZIP, absolute, name);
  }

  @Test
  void normalizeHandlesParentSegments() {
    FileSystem fs = new StubFileSystem();

    assertEquals("b", path(fs, false, "a/../b").normalize().toString());
    assertEquals("", path(fs, false, "a/..").normalize().toString());
    assertEquals("../..", path(fs, false, "../..").normalize().toString());
    assertEquals("../../a", path(fs, false, "../../a").normalize().toString());
    assertEquals("..", path(fs, false, "a/b/../../..").normalize().toString());
    assertEquals("/", path(fs, true, "a/../..").normalize().toString());
    assertEquals("/b", path(fs, true, "a/../b").normalize().toString());
    assertEquals(path(fs, false, "b"), path(fs, false, "a/../b").normalize());
  }
}
