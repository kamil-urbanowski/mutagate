package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

class SingleEntryArchivePathMutaGate5Test {

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
  void endsWithBehavior() {
    FileSystem fs = new StubFileSystem();
    FileSystem otherFs = new StubFileSystem();
    SingleEntryArchiveFormat gz = SingleEntryArchiveFormat.GZIP;
    SingleEntryArchiveFormat bz = SingleEntryArchiveFormat.BZIP2;

    SingleEntryArchivePath path = new SingleEntryArchivePath(fs, gz, false, "a/b/c");

    // proper suffixes
    assertTrue(path.endsWith(new SingleEntryArchivePath(fs, gz, false, "c")));
    assertTrue(path.endsWith(new SingleEntryArchivePath(fs, gz, false, "b/c")));
    assertTrue(path.endsWith(new SingleEntryArchivePath(fs, gz, false, "a/b/c")));

    // not suffixes
    assertFalse(path.endsWith(new SingleEntryArchivePath(fs, gz, false, "b")));
    assertFalse(path.endsWith(new SingleEntryArchivePath(fs, gz, false, "a/c")));
    assertFalse(path.endsWith(new SingleEntryArchivePath(fs, gz, false, "x/b/c")));
    assertFalse(path.endsWith(new SingleEntryArchivePath(fs, gz, false, "z/a/b/c")));

    // empty other has no segments, so it matches
    assertTrue(path.endsWith(new SingleEntryArchivePath(fs, gz, false, "")));

    // different format or file system
    assertFalse(path.endsWith(new SingleEntryArchivePath(fs, bz, false, "b/c")));
    assertFalse(path.endsWith(new SingleEntryArchivePath(otherFs, gz, false, "b/c")));

    // foreign path type
    assertFalse(path.endsWith(Path.of("c")));

    // absoluteness does not matter for endsWith
    SingleEntryArchivePath abs = new SingleEntryArchivePath(fs, gz, true, "a/b/c");
    assertTrue(abs.endsWith(new SingleEntryArchivePath(fs, gz, false, "b/c")));
    assertTrue(path.endsWith(new SingleEntryArchivePath(fs, gz, true, "b/c")));
    assertTrue(abs.endsWith(new SingleEntryArchivePath(fs, gz, true, "a/b/c")));
  }
}
