package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CompressorArchiveProviderSupportMutaGate2Test {

  private static final class StubProvider extends FileSystemProvider {
    @Override
    public String getScheme() {
      return "gzip";
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
    public DirectoryStream<Path> newDirectoryStream(Path dir, DirectoryStream.Filter<? super Path> filter) {
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

  private static final class TestFs extends AbstractCompressorArchiveFileSystem {
    TestFs() {
      super(
          new StubProvider(),
          URI.create("gzip:/a.gz"),
          Path.of("a.gz"),
          "gzip",
          "payload",
          Path.of("payload.tmp"));
    }

    @Override
    protected void deregister(URI uri) {}

    @Override
    protected SingleEntryArchiveFormat pathFormat() {
      return SingleEntryArchiveFormat.GZIP;
    }
  }

  @Test
  void rootDirectoryStreamListsVirtualEntryAndAppliesFilter() throws IOException {
    SingleEntryArchiveFormat format = SingleEntryArchiveFormat.GZIP;
    TestFs fs = new TestFs();
    Path root = new SingleEntryArchivePath(fs, format, true, "");
    Path expected = fs.getPath("/", "payload");

    // No filter: the single virtual entry is listed.
    List<Path> all = new ArrayList<>();
    try (DirectoryStream<Path> ds = CompressorArchiveProviderSupport.newRootDirectoryStream(root, null, format)) {
      for (Path p : ds) {
        all.add(p);
      }
    }
    assertEquals(1, all.size());
    assertEquals(expected, all.get(0));

    // Accepting filter receives the child and keeps it.
    List<Path> seen = new ArrayList<>();
    List<Path> accepted = new ArrayList<>();
    try (DirectoryStream<Path> ds =
        CompressorArchiveProviderSupport.newRootDirectoryStream(
            root,
            p -> {
              seen.add(p);
              return true;
            },
            format)) {
      for (Path p : ds) {
        accepted.add(p);
      }
    }
    assertEquals(1, seen.size());
    assertEquals(expected, seen.get(0));
    assertEquals(1, accepted.size());

    // Rejecting filter yields an empty stream.
    try (DirectoryStream<Path> ds =
        CompressorArchiveProviderSupport.newRootDirectoryStream(root, p -> false, format)) {
      assertFalse(ds.iterator().hasNext());
    }

    // Non-root directory is rejected.
    Path nonRoot = new SingleEntryArchivePath(fs, format, true, "payload");
    assertThrows(
        NoSuchFileException.class,
        () -> CompressorArchiveProviderSupport.newRootDirectoryStream(nonRoot, null, format));

    assertTrue(all.get(0) instanceof SingleEntryArchivePath);
  }
}
