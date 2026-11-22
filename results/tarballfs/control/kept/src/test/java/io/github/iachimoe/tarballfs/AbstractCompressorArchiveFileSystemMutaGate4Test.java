package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessMode;
import java.nio.file.ClosedFileSystemException;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AbstractCompressorArchiveFileSystemMutaGate4Test {

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
    public void checkAccess(Path path, AccessMode... modes) {
      throw new UnsupportedOperationException();
    }

    @Override
    public <V extends java.nio.file.attribute.FileAttributeView> V getFileAttributeView(
        Path path, Class<V> type, LinkOption... options) {
      throw new UnsupportedOperationException();
    }

    @Override
    public <A extends BasicFileAttributes> A readAttributes(
        Path path, Class<A> type, LinkOption... options) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Object> readAttributes(
        Path path, String attributes, LinkOption... options) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setAttribute(Path path, String attribute, Object value, LinkOption... options) {
      throw new UnsupportedOperationException();
    }
  }

  private static final class TestFileSystem extends AbstractCompressorArchiveFileSystem {
    TestFileSystem(URI uri, Path archive, Path temp) {
      super(new StubProvider(), uri, archive, "gzip", "entry.txt", temp);
    }

    @Override
    protected void deregister(URI uri) {
      // no-op
    }

    @Override
    protected SingleEntryArchiveFormat pathFormat() {
      return SingleEntryArchiveFormat.GZIP;
    }
  }

  @Test
  void getPathMatcherDelegatesToDefaultFileSystemAndValidatesState(@TempDir Path dir)
      throws IOException {
    Path archive = Files.createFile(dir.resolve("a.gz"));
    Path temp = dir.resolve("temp.bin");
    TestFileSystem fs = new TestFileSystem(URI.create("stub:/a.gz"), archive, temp);

    PathMatcher glob = fs.getPathMatcher("glob:*.txt");
    assertTrue(glob.matches(Path.of("a.txt")));
    assertFalse(glob.matches(Path.of("b.java")));

    PathMatcher regex = fs.getPathMatcher("regex:.*\\.java");
    assertTrue(regex.matches(Path.of("b.java")));
    assertFalse(regex.matches(Path.of("a.txt")));

    assertThrows(NullPointerException.class, () -> fs.getPathMatcher(null));
    assertThrows(IllegalArgumentException.class, () -> fs.getPathMatcher("nocolon"));
    assertThrows(UnsupportedOperationException.class, () -> fs.getPathMatcher("bogus:abc"));

    fs.close();
    assertThrows(ClosedFileSystemException.class, () -> fs.getPathMatcher("glob:*.txt"));
    assertThrows(ClosedFileSystemException.class, () -> fs.getPathMatcher(null));
  }
}
