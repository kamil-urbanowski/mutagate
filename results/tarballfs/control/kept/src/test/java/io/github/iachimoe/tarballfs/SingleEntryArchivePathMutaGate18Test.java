package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchService;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import org.junit.jupiter.api.Test;

class SingleEntryArchivePathMutaGate18Test {

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

  @Test
  void hashCodeMatchesFormulaAndIsConsistentWithEquals() {
    FileSystem fs = new StubFileSystem();
    SingleEntryArchiveFormat format = SingleEntryArchiveFormat.GZIP;

    SingleEntryArchivePath a = new SingleEntryArchivePath(fs, format, false, "a/b");
    SingleEntryArchivePath b = new SingleEntryArchivePath(fs, format, false, "a/b");
    assertEquals(a, b);
    assertEquals(a.hashCode(), b.hashCode());

    int expectedRelative =
        31 * Objects.hash(format, System.identityHashCode(fs), false)
            + Arrays.hashCode(new String[] {"a", "b"});
    assertEquals(expectedRelative, a.hashCode());

    SingleEntryArchivePath abs = new SingleEntryArchivePath(fs, format, true, "a/b");
    int expectedAbsolute =
        31 * Objects.hash(format, System.identityHashCode(fs), true)
            + Arrays.hashCode(new String[] {"a", "b"});
    assertEquals(expectedAbsolute, abs.hashCode());
    assertNotEquals(a.hashCode(), abs.hashCode());

    SingleEntryArchivePath other = new SingleEntryArchivePath(fs, format, false, "a/c");
    int expectedOther =
        31 * Objects.hash(format, System.identityHashCode(fs), false)
            + Arrays.hashCode(new String[] {"a", "c"});
    assertEquals(expectedOther, other.hashCode());
    assertNotEquals(a.hashCode(), other.hashCode());
  }
}
