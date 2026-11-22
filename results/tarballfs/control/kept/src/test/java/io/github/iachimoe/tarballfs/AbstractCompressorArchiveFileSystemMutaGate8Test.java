package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AbstractCompressorArchiveFileSystemMutaGate8Test {

  static final class TestFileSystem extends AbstractCompressorArchiveFileSystem {
    final List<URI> deregistered = new ArrayList<>();

    TestFileSystem(URI uri, Path archive, Path temp) {
      super(null == null ? new java.nio.file.spi.FileSystemProvider() {
        @Override
        public String getScheme() {
          return "test";
        }

        @Override
        public java.nio.file.FileSystem newFileSystem(URI u, java.util.Map<String, ?> env) {
          throw new UnsupportedOperationException();
        }

        @Override
        public java.nio.file.FileSystem getFileSystem(URI u) {
          throw new UnsupportedOperationException();
        }

        @Override
        public Path getPath(URI u) {
          throw new UnsupportedOperationException();
        }

        @Override
        public java.nio.channels.SeekableByteChannel newByteChannel(
            Path p,
            java.util.Set<? extends java.nio.file.OpenOption> o,
            java.nio.file.attribute.FileAttribute<?>... a) {
          throw new UnsupportedOperationException();
        }

        @Override
        public java.nio.file.DirectoryStream<Path> newDirectoryStream(
            Path d, java.nio.file.DirectoryStream.Filter<? super Path> f) {
          throw new UnsupportedOperationException();
        }

        @Override
        public void createDirectory(Path d, java.nio.file.attribute.FileAttribute<?>... a) {
          throw new UnsupportedOperationException();
        }

        @Override
        public void delete(Path p) {
          throw new UnsupportedOperationException();
        }

        @Override
        public void copy(Path s, Path t, java.nio.file.CopyOption... o) {
          throw new UnsupportedOperationException();
        }

        @Override
        public void move(Path s, Path t, java.nio.file.CopyOption... o) {
          throw new UnsupportedOperationException();
        }

        @Override
        public boolean isSameFile(Path a, Path b) {
          return false;
        }

        @Override
        public boolean isHidden(Path p) {
          return false;
        }

        @Override
        public java.nio.file.FileStore getFileStore(Path p) {
          throw new UnsupportedOperationException();
        }

        @Override
        public void checkAccess(Path p, java.nio.file.AccessMode... m) {
          throw new UnsupportedOperationException();
        }

        @Override
        public <V extends java.nio.file.attribute.FileAttributeView> V getFileAttributeView(
            Path p, Class<V> t, java.nio.file.LinkOption... o) {
          return null;
        }

        @Override
        public <A extends java.nio.file.attribute.BasicFileAttributes> A readAttributes(
            Path p, Class<A> t, java.nio.file.LinkOption... o) {
          throw new UnsupportedOperationException();
        }

        @Override
        public java.util.Map<String, Object> readAttributes(
            Path p, String a, java.nio.file.LinkOption... o) {
          throw new UnsupportedOperationException();
        }

        @Override
        public void setAttribute(Path p, String a, Object v, java.nio.file.LinkOption... o) {
          throw new UnsupportedOperationException();
        }
      } : null, uri, archive, "gzip", "entry", temp);
    }

    @Override
    protected void deregister(URI uri) {
      deregistered.add(uri);
    }

    @Override
    protected SingleEntryArchiveFormat pathFormat() {
      return SingleEntryArchiveFormat.GZIP;
    }
  }

  @TempDir Path dir;

  @Test
  void isOpenTransitionsFromTrueToFalseOnCloseAndStaysFalse() throws IOException {
    Path archive = dir.resolve("a.gz");
    Path temp = dir.resolve("a.tmp");
    Files.write(archive, new byte[] {1});
    Files.write(temp, new byte[] {2});
    URI uri = URI.create("test:/a");

    TestFileSystem fs = new TestFileSystem(uri, archive, temp);

    assertTrue(fs.isOpen());
    assertTrue(fs.isOpen());

    fs.close();

    assertFalse(fs.isOpen());
    assertFalse(Files.exists(temp));
    assertEquals(1, fs.deregistered.size());

    fs.close();

    assertFalse(fs.isOpen());
    assertEquals(1, fs.deregistered.size());
  }
}
