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

class SingleEntryArchivePathMutaGate4Test {

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
  void startsWithBehaviors() {
    FileSystem fs = new StubFileSystem();
    FileSystem otherFs = new StubFileSystem();
    SingleEntryArchiveFormat gz = SingleEntryArchiveFormat.GZIP;
    SingleEntryArchiveFormat bz = SingleEntryArchiveFormat.BZIP2;

    SingleEntryArchivePath rel = new SingleEntryArchivePath(fs, gz, false, "a/b/c");
    SingleEntryArchivePath abs = new SingleEntryArchivePath(fs, gz, true, "a/b/c");

    // proper prefixes and equality
    assertTrue(rel.startsWith(new SingleEntryArchivePath(fs, gz, false, "a")));
    assertTrue(rel.startsWith(new SingleEntryArchivePath(fs, gz, false, "a/b")));
    assertTrue(rel.startsWith(new SingleEntryArchivePath(fs, gz, false, "a/b/c")));
    assertTrue(abs.startsWith(new SingleEntryArchivePath(fs, gz, true, "a/b")));
    assertTrue(abs.startsWith(new SingleEntryArchivePath(fs, gz, true, "a/b/c")));

    // empty prefixes
    assertTrue(rel.startsWith(new SingleEntryArchivePath(fs, gz, false, "")));
    assertTrue(abs.startsWith(new SingleEntryArchivePath(fs, gz, true, "")));

    // longer other
    assertFalse(rel.startsWith(new SingleEntryArchivePath(fs, gz, false, "a/b/c/d")));
    assertFalse(new SingleEntryArchivePath(fs, gz, false, "").startsWith(rel));

    // mismatching segments (not merely string prefix)
    assertFalse(rel.startsWith(new SingleEntryArchivePath(fs, gz, false, "b")));
    assertFalse(rel.startsWith(new SingleEntryArchivePath(fs, gz, false, "a/x")));
    assertFalse(rel.startsWith(new SingleEntryArchivePath(fs, gz, false, "a/b/x")));
    assertFalse(
        new SingleEntryArchivePath(fs, gz, false, "ab/c")
            .startsWith(new SingleEntryArchivePath(fs, gz, false, "a")));

    // absolute vs relative mismatch
    assertFalse(rel.startsWith(new SingleEntryArchivePath(fs, gz, true, "a/b")));
    assertFalse(abs.startsWith(new SingleEntryArchivePath(fs, gz, false, "a/b")));
    assertFalse(rel.startsWith(new SingleEntryArchivePath(fs, gz, true, "")));

    // different format
    assertFalse(rel.startsWith(new SingleEntryArchivePath(fs, bz, false, "a")));

    // different file system
    assertFalse(rel.startsWith(new SingleEntryArchivePath(otherFs, gz, false, "a")));

    // foreign path type
    assertFalse(rel.startsWith(Path.of("a")));
  }
}
