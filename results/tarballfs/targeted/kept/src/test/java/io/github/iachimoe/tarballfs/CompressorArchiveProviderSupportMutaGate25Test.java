package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CompressorArchiveProviderSupportMutaGate25Test {

  private static final BasicFileAttributes ATTRS = SimpleVirtualBasicAttributes.regularFile("x", 5L);

  static final class StubProvider extends FileSystemProvider {
    @Override
    public String getScheme() {
      return "stub";
    }

    @Override
    public java.nio.file.FileSystem newFileSystem(URI uri, Map<String, ?> env) throws IOException {
      throw new UnsupportedOperationException();
    }

    @Override
    public java.nio.file.FileSystem getFileSystem(URI uri) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Path getPath(URI uri) {
      throw new UnsupportedOperationException();
    }

    @Override
    public SeekableByteChannel newByteChannel(
        Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
      throw new UnsupportedOperationException();
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path dir, DirectoryStream.Filter<? super Path> filter)
        throws IOException {
      throw new UnsupportedOperationException();
    }

    @Override
    public void createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException {
      throw new UnsupportedOperationException();
    }

    @Override
    public void delete(Path path) throws IOException {
      throw new UnsupportedOperationException();
    }

    @Override
    public void copy(Path source, Path target, CopyOption... options) throws IOException {
      throw new UnsupportedOperationException();
    }

    @Override
    public void move(Path source, Path target, CopyOption... options) throws IOException {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSameFile(Path path, Path path2) throws IOException {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean isHidden(Path path) throws IOException {
      throw new UnsupportedOperationException();
    }

    @Override
    public FileStore getFileStore(Path path) throws IOException {
      throw new UnsupportedOperationException();
    }

    @Override
    public void checkAccess(Path path, AccessMode... modes) throws IOException {
      throw new UnsupportedOperationException();
    }

    @Override
    public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options) {
      throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options)
        throws IOException {
      return (A) ATTRS;
    }

    @Override
    public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options)
        throws IOException {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setAttribute(Path path, String attribute, Object value, LinkOption... options)
        throws IOException {
      throw new UnsupportedOperationException();
    }
  }

  static final class StubFileSystem extends FileSystem {
    private final FileSystemProvider provider;

    StubFileSystem(FileSystemProvider provider) {
      this.provider = provider;
    }

    @Override
    public FileSystemProvider provider() {
      return provider;
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
    public WatchService newWatchService() {
      throw new UnsupportedOperationException();
    }
  }

  @Test
  void readBasicAttributesMapReturnsMapFromAttributes() throws IOException {
    StubProvider provider = new StubProvider();
    StubFileSystem fs = new StubFileSystem(provider);
    SingleEntryArchiveFormat format = SingleEntryArchiveFormat.GZIP;
    SingleEntryArchivePath path = new SingleEntryArchivePath(fs, format, true, "x");

    Map<String, Object> actual =
        CompressorArchiveProviderSupport.readBasicAttributesMap(path, "basic:*", provider, format);

    Map<String, Object> expected = BasicFileAttributesMaps.basicAttributesToMap(ATTRS, "*");
    assertFalse(actual.isEmpty());
    assertEquals(expected, actual);
  }
}
