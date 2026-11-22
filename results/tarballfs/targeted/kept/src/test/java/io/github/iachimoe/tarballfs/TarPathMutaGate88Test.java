package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;

class TarPathMutaGate88Test {

  private static void put(byte[] block, int offset, String value) {
    byte[] bytes = value.getBytes(StandardCharsets.US_ASCII);
    System.arraycopy(bytes, 0, block, offset, bytes.length);
  }

  private static byte[] buildTar() {
    byte[] header = new byte[512];
    put(header, 0, "a.txt");
    put(header, 100, "0000644");
    put(header, 108, "0000000");
    put(header, 116, "0000000");
    put(header, 124, "00000000000");
    put(header, 136, "00000000000");
    for (int i = 148; i < 156; i++) {
      header[i] = ' ';
    }
    header[156] = '0';
    put(header, 257, "ustar");
    put(header, 263, "00");
    int sum = 0;
    for (byte b : header) {
      sum += b & 0xff;
    }
    put(header, 148, String.format("%06o", sum));
    header[154] = 0;
    header[155] = ' ';

    byte[] tar = new byte[512 + 1024];
    System.arraycopy(header, 0, tar, 0, 512);
    return tar;
  }

  @Test
  void subpathValidatesRangeAndReturnsSegments() throws Exception {
    Path tar = Files.createTempFile("tarpath-subpath", ".tar");
    try {
      Files.write(tar, buildTar());
      try (FileSystem fs = new TarFileSystemProvider().newFileSystem(tar, Map.of())) {
        Path p = fs.getPath("a/b/c");
        assertEquals(3, p.getNameCount());

        // valid ranges
        assertEquals("a/b/c", p.subpath(0, 3).toString());
        assertEquals("a", p.subpath(0, 1).toString());
        assertEquals("b", p.subpath(1, 2).toString());
        assertEquals("b/c", p.subpath(1, 3).toString());

        // invalid: negative begin
        assertThrows(IllegalArgumentException.class, () -> p.subpath(-1, 1));
        // invalid: end beyond length
        assertThrows(IllegalArgumentException.class, () -> p.subpath(0, 4));
        // invalid: begin equals end
        assertThrows(IllegalArgumentException.class, () -> p.subpath(1, 1));
        // invalid: begin greater than end
        assertThrows(IllegalArgumentException.class, () -> p.subpath(2, 1));
      }
    } finally {
      Files.deleteIfExists(tar);
    }
  }
}
