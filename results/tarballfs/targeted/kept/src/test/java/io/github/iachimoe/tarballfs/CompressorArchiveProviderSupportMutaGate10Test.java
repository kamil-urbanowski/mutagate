package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class CompressorArchiveProviderSupportMutaGate10Test {

  static final class StubProvider extends FileSystemProvider {
    @Override
    public String getScheme() {
      return "stubgz";
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
    public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options) {
      throw new UnsupportedOperationException();
    }

    @Override
    public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options) {
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

  static final class TestFs extends AbstractCompressorArchiveFileSystem {
    TestFs(FileSystemProvider provider) {
      super(
          provider,
          URI.create("stubgz:/tmp/host.gz"),
          Path.of("host.gz"),
          "gzip",
          "payload",
          Path.of("payload.tmp"));
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

  private static List<Path> drain(DirectoryStream<Path> stream) throws IOException {
    List<Path> result = new ArrayList<>();
    try (DirectoryStream<Path> s = stream) {
      for (Path p : s) {
        result.add(p);
      }
    }
    return result;
  }

  @Test
  void rootDirectoryStreamAppliesFilterWhenPresentAndHandlesNullFilter() throws IOException {
    SingleEntryArchiveFormat format = SingleEntryArchiveFormat.GZIP;
    TestFs fs = new TestFs(new StubProvider());
    Path root = new SingleEntryArchivePath(fs, format, true, "");

    // Null filter: the single virtual entry is listed.
    List<Path> unfiltered = drain(CompressorArchiveProviderSupport.newRootDirectoryStream(root, null, format));
    assertEquals(1, unfiltered.size());

    // Rejecting filter: nothing is listed, and the filter was consulted.
    AtomicInteger rejectCalls = new AtomicInteger();
    List<Path> none =
        drain(
            CompressorArchiveProviderSupport.newRootDirectoryStream(
                root,
                p -> {
                  rejectCalls.incrementAndGet();
                  return false;
                },
                format));
    assertEquals(0, none.size());
    assertEquals(1, rejectCalls.get());

    // Accepting filter: the entry is listed.
    AtomicInteger acceptCalls = new AtomicInteger();
    List<Path> all =
        drain(
            CompressorArchiveProviderSupport.newRootDirectoryStream(
                root,
                p -> {
                  acceptCalls.incrementAndGet();
                  return true;
                },
                format));
    assertEquals(1, all.size());
    assertEquals(1, acceptCalls.get());
  }
}
