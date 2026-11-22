package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import java.util.Set;
import org.junit.jupiter.api.Test;

class SingleEntryArchivePathMutaGate24Test {

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
      return null;
    }

    @Override
    public Iterable<FileStore> getFileStores() {
      return null;
    }

    @Override
    public Set<String> supportedFileAttributeViews() {
      return Set.of();
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
  void getRootBehavior() {
    FileSystem fs = new StubFileSystem();
    SingleEntryArchiveFormat format = SingleEntryArchiveFormat.GZIP;

    SingleEntryArchivePath relative = new SingleEntryArchivePath(fs, format, false, "a/b");
    assertNull(relative.getRoot());
    assertNull(new SingleEntryArchivePath(fs, format, false, "").getRoot());

    SingleEntryArchivePath absolute = new SingleEntryArchivePath(fs, format, true, "a/b");
    Path root = absolute.getRoot();
    assertNotNull(root);
    assertTrue(root.isAbsolute());
    assertSame(fs, root.getFileSystem());

    SingleEntryArchivePath typedRoot = (SingleEntryArchivePath) root;
    assertSame(format, typedRoot.format());
    assertEquals(new SingleEntryArchivePath(fs, format, true, ""), root);

    // Root is the same regardless of which absolute path it came from
    assertEquals(root, new SingleEntryArchivePath(fs, format, true, "x").getRoot());

    // Root of a root is itself (by equality), and has no parent or file name
    assertEquals(root, root.getRoot());
    assertNull(root.getParent());
    assertNull(root.getFileName());

    // Root of a different format differs
    Path bzRoot =
        new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.BZIP2, true, "a/b").getRoot();
    assertSame(SingleEntryArchiveFormat.BZIP2, ((SingleEntryArchivePath) bzRoot).format());
    assertTrue(!root.equals(bzRoot));
  }
}
