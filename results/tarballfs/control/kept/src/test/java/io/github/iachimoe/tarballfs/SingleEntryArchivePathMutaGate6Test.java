package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

class SingleEntryArchivePathMutaGate6Test {

  private static final class StubFileSystem extends FileSystem {
    @Override
    public FileSystemProvider provider() {
      return null;
    }

    @Override
    public void close() throws IOException {
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
      return List.of();
    }

    @Override
    public Iterable<FileStore> getFileStores() {
      return List.of();
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
  void iteratorYieldsRelativeNameElementsInOrder() {
    FileSystem fs = new StubFileSystem();
    SingleEntryArchivePath absolute =
        new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.GZIP, true, "a/b/c");

    Iterator<Path> it = absolute.iterator();
    List<String> names = new ArrayList<>();
    while (it.hasNext()) {
      Path p = it.next();
      assertFalse(p.isAbsolute());
      assertEquals(1, p.getNameCount());
      names.add(p.toString());
    }
    assertEquals(List.of("a", "b", "c"), names);
    assertFalse(it.hasNext());
    assertThrows(ArrayIndexOutOfBoundsException.class, it::next);

    SingleEntryArchivePath empty =
        new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.GZIP, false, "");
    assertFalse(empty.iterator().hasNext());

    SingleEntryArchivePath single =
        new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.GZIP, false, "x");
    Iterator<Path> one = single.iterator();
    assertTrue(one.hasNext());
    assertEquals("x", one.next().toString());
    assertFalse(one.hasNext());

    // each call creates an independent iterator
    Iterator<Path> first = absolute.iterator();
    first.next();
    assertEquals("a", absolute.iterator().next().toString());
    assertEquals("b", first.next().toString());
  }
}
