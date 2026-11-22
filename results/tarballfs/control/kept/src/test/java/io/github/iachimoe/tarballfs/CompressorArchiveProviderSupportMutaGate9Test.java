package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

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
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CompressorArchiveProviderSupportMutaGate9Test {

  private static final class StubProvider extends FileSystemProvider {
    @Override
    public String getScheme() {
      return "stub";
    }

    @Override
    public FileSystem newFileSystem(URI uri, Map<String, ?> env) {
      throw new UnsupportedOperationException();
    }

    @Override
    public FileSystem getFileSystem(URI uri) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Path getPath(URI uri) {
      throw new UnsupportedOperationException();
    }

    @Override
    public SeekableByteChannel newByteChannel(
        Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) {
      throw new UnsupportedOperationException();
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(
        Path dir, DirectoryStream.Filter<? super Path> filter) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void createDirectory(Path dir, FileAttribute<?>... attrs) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void delete(Path path) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void copy(Path source, Path target, CopyOption... options) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void move(Path source, Path target, CopyOption... options) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSameFile(Path path, Path path2) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean isHidden(Path path) {
      throw new UnsupportedOperationException();
    }

    @Override
    public FileStore getFileStore(Path path) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void checkAccess(Path path, AccessMode... modes) throws IOException {
      throw new UnsupportedOperationException();
    }

    @Override
    public <V extends FileAttributeView> V getFileAttributeView(
        Path path, Class<V> type, LinkOption... options) {
      throw new UnsupportedOperationException();
    }

    @Override
    public <A extends BasicFileAttributes> A readAttributes(
        Path path, Class<A> type, LinkOption... options) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setAttribute(Path path, String attribute, Object value, LinkOption... options) {
      throw new UnsupportedOperationException();
    }
  }

  private static final class StubFileSystem extends AbstractCompressorArchiveFileSystem {
    StubFileSystem(FileSystemProvider provider, URI uri) {
      super(provider, uri, Path.of("archive.gz"), "gzip", "archive", Path.of("archive.tmp"));
    }

    @Override
    protected void deregister(URI uri) {
      // nothing
    }

    @Override
    protected SingleEntryArchiveFormat pathFormat() {
      return SingleEntryArchiveFormat.GZIP;
    }
  }

  @Test
  void returnsTheFileStoreOwnedByThePathsFileSystem() {
    StubFileSystem fs = new StubFileSystem(new StubProvider(), URI.create("stub:/tmp/archive.gz"));
    SingleEntryArchivePath path =
        new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.GZIP, true, "archive");

    FileStore store = CompressorArchiveProviderSupport.compressorFileStore(
        path, SingleEntryArchiveFormat.GZIP);

    assertNotNull(store);
    assertSame(fs.compressorFileStore(), store);
  }
}
