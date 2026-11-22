package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarPathMutaGate86Test {

  @TempDir
  Path tempDir;

  private static void put(byte[] buf, int offset, String value) {
    byte[] b = value.getBytes(StandardCharsets.ISO_8859_1);
    System.arraycopy(b, 0, buf, offset, b.length);
  }

  private static byte[] buildTar() {
    byte[] tar = new byte[512 + 1024];
    put(tar, 0, "a.txt");
    put(tar, 100, "0000644\0");
    put(tar, 108, "0000000\0");
    put(tar, 116, "0000000\0");
    put(tar, 124, "00000000000\0");
    put(tar, 136, "00000000000\0");
    Arrays.fill(tar, 148, 156, (byte) ' ');
    tar[156] = '0';
    put(tar, 257, "ustar\0");
    put(tar, 263, "00");
    int sum = 0;
    for (int i = 0; i < 512; i++) {
      sum += tar[i] & 0xff;
    }
    put(tar, 148, String.format("%06o", sum) + "\0 ");
    return tar;
  }

  @Test
  void subpathBoundaries() throws Exception {
    Path tar = tempDir.resolve("one.tar");
    Files.write(tar, buildTar());

    TarFileSystemProvider provider = new TarFileSystemProvider();
    try (TarFileSystem fs = (TarFileSystem) provider.newFileSystem(tar, Map.of())) {
      TarPath path = new TarPath(fs, false, "a/b/c");

      // beginIndex == 0 is valid (kills "< 0" -> "<= 0")
      assertEquals("a", path.subpath(0, 1).toString());
      // endIndex == segments.length is valid (kills "> length" -> ">= length")
      assertEquals("a/b/c", path.subpath(0, 3).toString());
      assertEquals("b/c", path.subpath(1, 3).toString());
      assertEquals("b", path.subpath(1, 2).toString());

      // beginIndex == endIndex is invalid (kills ">=" -> ">")
      assertThrows(IllegalArgumentException.class, () -> path.subpath(1, 1));
      assertThrows(IllegalArgumentException.class, () -> path.subpath(0, 0));
      assertThrows(IllegalArgumentException.class, () -> path.subpath(2, 1));

      // out of range values are invalid
      assertThrows(IllegalArgumentException.class, () -> path.subpath(-1, 2));
      assertThrows(IllegalArgumentException.class, () -> path.subpath(0, 4));
    }
  }
}
