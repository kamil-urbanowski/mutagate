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
import java.util.Set;
import org.junit.jupiter.api.Test;

class SingleEntryArchivePathMutaGate7Test {

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
  void equalsComparesFormatAbsoluteFileSystemAndSegments() {
    FileSystem fs1 = new StubFileSystem();
    FileSystem fs2 = new StubFileSystem();

    SingleEntryArchivePath base =
        new SingleEntryArchivePath(fs1, SingleEntryArchiveFormat.GZIP, false, "a/b");
    SingleEntryArchivePath same =
        new SingleEntryArchivePath(fs1, SingleEntryArchiveFormat.GZIP, false, "a/b");

    assertTrue(base.equals(base));
    assertTrue(base.equals(same));
    assertTrue(same.equals(base));

    SingleEntryArchivePath otherFormat =
        new SingleEntryArchivePath(fs1, SingleEntryArchiveFormat.BZIP2, false, "a/b");
    assertFalse(base.equals(otherFormat));
    assertFalse(otherFormat.equals(base));

    SingleEntryArchivePath otherAbsolute =
        new SingleEntryArchivePath(fs1, SingleEntryArchiveFormat.GZIP, true, "a/b");
    assertFalse(base.equals(otherAbsolute));
    assertFalse(otherAbsolute.equals(base));

    SingleEntryArchivePath otherFs =
        new SingleEntryArchivePath(fs2, SingleEntryArchiveFormat.GZIP, false, "a/b");
    assertFalse(base.equals(otherFs));
    assertFalse(otherFs.equals(base));

    SingleEntryArchivePath shorter =
        new SingleEntryArchivePath(fs1, SingleEntryArchiveFormat.GZIP, false, "a");
    assertFalse(base.equals(shorter));
    assertFalse(shorter.equals(base));

    SingleEntryArchivePath reordered =
        new SingleEntryArchivePath(fs1, SingleEntryArchiveFormat.GZIP, false, "b/a");
    assertFalse(base.equals(reordered));

    SingleEntryArchivePath differentName =
        new SingleEntryArchivePath(fs1, SingleEntryArchiveFormat.GZIP, false, "a/c");
    assertFalse(base.equals(differentName));

    SingleEntryArchivePath absRoot =
        new SingleEntryArchivePath(fs1, SingleEntryArchiveFormat.GZIP, true, "");
    SingleEntryArchivePath relEmpty =
        new SingleEntryArchivePath(fs1, SingleEntryArchiveFormat.GZIP, false, "");
    assertFalse(absRoot.equals(relEmpty));
    assertTrue(relEmpty.equals(
        new SingleEntryArchivePath(fs1, SingleEntryArchiveFormat.GZIP, false, "")));

    assertFalse(base.equals(null));
    assertFalse(base.equals("a/b"));
    assertFalse(base.equals(new Object()));
  }
}
