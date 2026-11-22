package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.AccessMode;
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

class CompressorArchiveProviderSupportMutaGate6Test {

  private static final class StubFileSystem extends FileSystem {
    @Override
    public FileSystemProvider provider() {
      return null;
    }

    @Override
    public void close() {}

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
      return Collections.singleton("basic");
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
  void checkReadAccessHandlesNullEmptyAndNonReadModes() {
    SingleEntryArchiveFormat format = SingleEntryArchiveFormat.GZIP;
    Path root = new SingleEntryArchivePath(new StubFileSystem(), format, true, "");

    assertDoesNotThrow(() -> CompressorArchiveProviderSupport.checkReadAccess(root, null, format));
    assertDoesNotThrow(
        () -> CompressorArchiveProviderSupport.checkReadAccess(root, new AccessMode[0], format));
    assertDoesNotThrow(
        () ->
            CompressorArchiveProviderSupport.checkReadAccess(
                root, new AccessMode[] {AccessMode.READ}, format));
    assertThrows(
        AccessDeniedException.class,
        () ->
            CompressorArchiveProviderSupport.checkReadAccess(
                root, new AccessMode[] {AccessMode.WRITE}, format));
  }
}
