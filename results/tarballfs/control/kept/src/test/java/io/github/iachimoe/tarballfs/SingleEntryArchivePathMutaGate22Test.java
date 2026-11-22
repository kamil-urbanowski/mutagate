package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

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

class SingleEntryArchivePathMutaGate22Test {

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
  void getFileSystemReturnsOwningFileSystemForPathsAndDerivedPaths() {
    FileSystem fsA = new StubFileSystem();
    FileSystem fsB = new StubFileSystem();

    SingleEntryArchivePath relative =
        new SingleEntryArchivePath(fsA, SingleEntryArchiveFormat.GZIP, false, "dir/sub/file.txt");
    SingleEntryArchivePath absolute =
        new SingleEntryArchivePath(fsA, SingleEntryArchiveFormat.BZIP2, true, "dir/file.txt");
    SingleEntryArchivePath other =
        new SingleEntryArchivePath(fsB, SingleEntryArchiveFormat.GZIP, false, "dir/sub/file.txt");

    assertSame(fsA, relative.getFileSystem());
    assertSame(fsA, absolute.getFileSystem());
    assertSame(fsB, other.getFileSystem());
    assertNotSame(relative.getFileSystem(), other.getFileSystem());

    assertSame(fsA, relative.getParent().getFileSystem());
    assertSame(fsA, relative.getFileName().getFileSystem());
    assertSame(fsA, relative.getName(1).getFileSystem());
    assertSame(fsA, relative.subpath(0, 2).getFileSystem());
    assertSame(fsA, relative.normalize().getFileSystem());
    assertSame(fsA, relative.toAbsolutePath().getFileSystem());
    assertSame(fsA, relative.resolve("more").getFileSystem());
    assertSame(fsA, absolute.getRoot().getFileSystem());
  }
}
