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
import java.util.Collections;
import java.util.Set;
import org.junit.jupiter.api.Test;

class SingleEntryArchivePathMutaGate2Test {

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

  private static SingleEntryArchivePath path(FileSystem fs, boolean absolute, String name) {
    return new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.GZIP, absolute, name);
  }

  @Test
  void normalizeHandlesDotAndDotDotSegments() {
    FileSystem fs = new StubFileSystem();

    Path mixed = path(fs, false, "a/./b/../c").normalize();
    assertNotNull(mixed);
    assertEquals("a/c", mixed.toString());
    assertEquals(path(fs, false, "a/c"), mixed);
    assertEquals(false, mixed.isAbsolute());

    assertEquals("../../a", path(fs, false, "../../a").normalize().toString());
    assertEquals("../b", path(fs, false, "a/../../b").normalize().toString());
    assertEquals("..", path(fs, false, "../a/..").normalize().toString());
    assertEquals("..", path(fs, false, "a/../..").normalize().toString());
    assertEquals("", path(fs, false, "a/..").normalize().toString());
    assertEquals("", path(fs, false, "./.").normalize().toString());

    Path absParent = path(fs, true, "../a").normalize();
    assertEquals("/a", absParent.toString());
    assertEquals(true, absParent.isAbsolute());
    assertEquals(path(fs, true, "a"), absParent);

    assertEquals("/", path(fs, true, "a/b/../..").normalize().toString());
    assertEquals("/", path(fs, true, "../..").normalize().toString());
    assertEquals("/b", path(fs, true, "a/../../b").normalize().toString());

    SingleEntryArchivePath original = path(fs, false, "x/../y");
    original.normalize();
    assertEquals("x/../y", original.toString());
  }
}
