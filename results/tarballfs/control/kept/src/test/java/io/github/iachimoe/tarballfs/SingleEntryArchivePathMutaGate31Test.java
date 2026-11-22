package io.github.iachimoe.tarballfs;

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

class SingleEntryArchivePathMutaGate31Test {

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
  void formatReturnsConstructorFormatAndIsPropagatedToDerivedPaths() {
    FileSystem fs = new StubFileSystem();

    for (SingleEntryArchiveFormat expected : SingleEntryArchiveFormat.values()) {
      SingleEntryArchivePath path = new SingleEntryArchivePath(fs, expected, false, "a/b/c");
      assertSame(expected, path.format());

      SingleEntryArchivePath absolute = new SingleEntryArchivePath(fs, expected, true, "x/y");
      assertSame(expected, absolute.format());

      assertSame(expected, ((SingleEntryArchivePath) path.getParent()).format());
      assertSame(expected, ((SingleEntryArchivePath) path.getFileName()).format());
      assertSame(expected, ((SingleEntryArchivePath) path.getName(1)).format());
      assertSame(expected, ((SingleEntryArchivePath) path.normalize()).format());
      assertSame(expected, ((SingleEntryArchivePath) path.toAbsolutePath()).format());
      assertSame(expected, ((SingleEntryArchivePath) absolute.getRoot()).format());
      assertSame(expected, ((SingleEntryArchivePath) path.resolve("d")).format());
    }

    SingleEntryArchivePath gz =
        new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.GZIP, false, "e");
    SingleEntryArchivePath bz =
        new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.BZIP2, false, "e");
    assertSame(SingleEntryArchiveFormat.GZIP, gz.format());
    assertSame(SingleEntryArchiveFormat.BZIP2, bz.format());
  }
}
