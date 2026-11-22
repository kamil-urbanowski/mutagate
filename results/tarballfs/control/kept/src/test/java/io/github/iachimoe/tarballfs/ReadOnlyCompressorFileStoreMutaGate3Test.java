package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

class ReadOnlyCompressorFileStoreMutaGate3Test {

  @Test
  void typeReturnsExactlyTheValuePassedToConstructor() {
    Path archive = Paths.get("some", "archive.tar.gz");
    String gzip = new String("gzip");
    String bzip2 = "bzip2";

    ReadOnlyCompressorFileStore first = new ReadOnlyCompressorFileStore(archive, gzip);
    ReadOnlyCompressorFileStore second = new ReadOnlyCompressorFileStore(archive, bzip2);
    ReadOnlyCompressorFileStore empty = new ReadOnlyCompressorFileStore(archive, "");
    ReadOnlyCompressorFileStore nullType = new ReadOnlyCompressorFileStore(archive, null);

    assertSame(gzip, first.type());
    assertEquals("gzip", first.type());
    assertEquals("bzip2", second.type());
    assertEquals("", empty.type());
    assertNull(nullType.type());
    assertEquals("gzip", first.type());
  }
}
