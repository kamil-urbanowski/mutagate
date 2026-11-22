package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

class SingleEntryArchivePathMutaGate3Test {

  private static final class StubFileSystem extends FileSystem {
    @Override
    public FileSystemProvider provider() {
      throw new UnsupportedOperationException();
    }

    @Override
    public void close() throws IOException {
      // no-op
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
      throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<FileStore> getFileStores() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> supportedFileAttributeViews() {
      throw new UnsupportedOperationException();
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
  void resolveBehaviors() {
    FileSystem fs = new StubFileSystem();
    FileSystem otherFs = new StubFileSystem();
    SingleEntryArchiveFormat gzip = SingleEntryArchiveFormat.GZIP;

    // relative base + relative other
    SingleEntryArchivePath rel = new SingleEntryArchivePath(fs, gzip, false, "a");
    SingleEntryArchivePath relOther = new SingleEntryArchivePath(fs, gzip, false, "b/c");
    Path r1 = rel.resolve(relOther);
    assertEquals("a/b/c", r1.toString());
    assertFalse(r1.isAbsolute());
    assertEquals(3, r1.getNameCount());

    // absolute base keeps absolute flag
    SingleEntryArchivePath abs = new SingleEntryArchivePath(fs, gzip, true, "a");
    Path r2 = abs.resolve(relOther);
    assertEquals("/a/b/c", r2.toString());
    assertTrue(r2.isAbsolute());

    // empty base yields other's name, no leading separator in entry
    SingleEntryArchivePath emptyRel = new SingleEntryArchivePath(fs, gzip, false, "");
    Path r3 = emptyRel.resolve(relOther);
    assertEquals("b/c", r3.toString());
    assertFalse(r3.isAbsolute());

    SingleEntryArchivePath root = new SingleEntryArchivePath(fs, gzip, true, "");
    Path r4 = root.resolve(relOther);
    assertEquals("/b/c", r4.toString());
    assertTrue(r4.isAbsolute());

    // absolute other returned as-is (same instance)
    SingleEntryArchivePath absOther = new SingleEntryArchivePath(fs, gzip, true, "z");
    assertSame(absOther, rel.resolve(absOther));

    // absolute other from a different file system is still returned as-is
    SingleEntryArchivePath absForeign = new SingleEntryArchivePath(otherFs, gzip, true, "z");
    assertSame(absForeign, rel.resolve(absForeign));

    // relative other from a different file system rejected
    SingleEntryArchivePath relForeignFs = new SingleEntryArchivePath(otherFs, gzip, false, "x");
    assertThrows(IllegalArgumentException.class, () -> rel.resolve(relForeignFs));

    // relative other with a different format rejected
    SingleEntryArchivePath relForeignFormat =
        new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.BZIP2, false, "x");
    assertThrows(IllegalArgumentException.class, () -> rel.resolve(relForeignFormat));

    // null rejected
    assertThrows(NullPointerException.class, () -> rel.resolve((Path) null));
  }
}
