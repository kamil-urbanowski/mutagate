package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

class SingleEntryArchivePathMutaGate53Test {

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
  void relativizeRequiresSameAbsoluteFormAndWorksForMatchingForm() {
    FileSystem fs = new StubFileSystem();
    SingleEntryArchiveFormat format = SingleEntryArchiveFormat.GZIP;

    SingleEntryArchivePath relBase = new SingleEntryArchivePath(fs, format, false, "a");
    SingleEntryArchivePath relOther = new SingleEntryArchivePath(fs, format, false, "a/b");
    SingleEntryArchivePath absBase = new SingleEntryArchivePath(fs, format, true, "a");
    SingleEntryArchivePath absOther = new SingleEntryArchivePath(fs, format, true, "a/b");

    Path relResult = relBase.relativize(relOther);
    assertEquals("b", relResult.toString());
    assertFalse(relResult.isAbsolute());

    Path absResult = absBase.relativize(absOther);
    assertEquals("b", absResult.toString());
    assertFalse(absResult.isAbsolute());

    assertThrows(IllegalArgumentException.class, () -> relBase.relativize(absOther));
    assertThrows(IllegalArgumentException.class, () -> absBase.relativize(relOther));
  }
}
