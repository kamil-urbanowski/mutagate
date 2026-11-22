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
import java.util.Set;
import org.junit.jupiter.api.Test;

class CompressorArchiveProviderSupportMutaGate5Test {

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
      return null;
    }

    @Override
    public Iterable<FileStore> getFileStores() {
      return null;
    }

    @Override
    public Set<String> supportedFileAttributeViews() {
      return Set.of("basic");
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
    public WatchService newWatchService() {
      return null;
    }
  }

  @Test
  void checkReadAccessAllowsOnlyReadModesOnExistingPath() {
    SingleEntryArchiveFormat format = SingleEntryArchiveFormat.GZIP;
    SingleEntryArchivePath root = new SingleEntryArchivePath(new StubFileSystem(), format, true, "");

    assertDoesNotThrow(() -> CompressorArchiveProviderSupport.checkReadAccess(root, null, format));
    assertDoesNotThrow(() -> CompressorArchiveProviderSupport.checkReadAccess(root, new AccessMode[0], format));
    assertDoesNotThrow(
        () -> CompressorArchiveProviderSupport.checkReadAccess(root, new AccessMode[] {AccessMode.READ}, format));
    assertDoesNotThrow(
        () -> CompressorArchiveProviderSupport.checkReadAccess(
            root, new AccessMode[] {AccessMode.READ, AccessMode.READ}, format));

    assertThrows(
        AccessDeniedException.class,
        () -> CompressorArchiveProviderSupport.checkReadAccess(root, new AccessMode[] {AccessMode.WRITE}, format));
    assertThrows(
        AccessDeniedException.class,
        () -> CompressorArchiveProviderSupport.checkReadAccess(root, new AccessMode[] {AccessMode.EXECUTE}, format));
    assertThrows(
        AccessDeniedException.class,
        () -> CompressorArchiveProviderSupport.checkReadAccess(
            root, new AccessMode[] {AccessMode.READ, AccessMode.WRITE}, format));
    assertThrows(
        AccessDeniedException.class,
        () -> CompressorArchiveProviderSupport.checkReadAccess(
            root, new AccessMode[] {AccessMode.WRITE, AccessMode.READ}, format));
  }
}
