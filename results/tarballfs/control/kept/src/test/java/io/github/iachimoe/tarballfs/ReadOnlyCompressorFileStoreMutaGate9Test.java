package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

class ReadOnlyCompressorFileStoreMutaGate9Test {

  @Test
  void supportsFileAttributeViewByNameOnlyAcceptsExactBasic() {
    Path archive = Paths.get("archive.tar.gz");
    ReadOnlyCompressorFileStore store = new ReadOnlyCompressorFileStore(archive, "gz");

    assertTrue(store.supportsFileAttributeView("basic"));

    assertFalse(store.supportsFileAttributeView((String) null));
    assertFalse(store.supportsFileAttributeView(""));
    assertFalse(store.supportsFileAttributeView("Basic"));
    assertFalse(store.supportsFileAttributeView("BASIC"));
    assertFalse(store.supportsFileAttributeView(" basic"));
    assertFalse(store.supportsFileAttributeView("basic "));
    assertFalse(store.supportsFileAttributeView("posix"));
    assertFalse(store.supportsFileAttributeView("owner"));
    assertFalse(store.supportsFileAttributeView("dos"));
    assertFalse(store.supportsFileAttributeView("user"));
  }
}
