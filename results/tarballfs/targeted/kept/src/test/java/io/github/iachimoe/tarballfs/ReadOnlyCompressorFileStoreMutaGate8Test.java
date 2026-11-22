package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

class ReadOnlyCompressorFileStoreMutaGate8Test {

  @Test
  void supportsFileAttributeViewByNameOnlyForBasic() {
    Path archive = Paths.get("archive.tar.gz");
    ReadOnlyCompressorFileStore store = new ReadOnlyCompressorFileStore(archive, "gz");

    assertTrue(store.supportsFileAttributeView("basic"));
    assertFalse(store.supportsFileAttributeView("posix"));
    assertFalse(store.supportsFileAttributeView((String) null));
  }
}
