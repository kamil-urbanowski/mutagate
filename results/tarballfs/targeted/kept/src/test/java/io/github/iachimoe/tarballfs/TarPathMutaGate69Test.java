package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;

class TarPathMutaGate69Test {

  private static void put(byte[] block, int offset, String value) {
    byte[] bytes = value.getBytes(StandardCharsets.US_ASCII);
    System.arraycopy(bytes, 0, block, offset, bytes.length);
  }

  private static byte[] buildTarWithEmptyFile(String name) {
    byte[] header = new byte[512];
    put(header, 0, name);
    put(header, 100, "0000644\0");
    put(header, 108, "0000000\0");
    put(header, 116, "0000000\0");
    put(header, 124, "00000000000\0");
    put(header, 136, "00000000000\0");
    Arrays.fill(header, 148, 156, (byte) ' ');
    header[156] = (byte) '0';
    put(header, 257, "ustar\0");
    put(header, 263, "00");
    int sum = 0;
    for (byte b : header) {
      sum += b & 0xFF;
    }
    put(header, 148, String.format("%06o", sum));
    header[154] = 0;
    header[155] = (byte) ' ';

    byte[] tar = new byte[512 + 1024];
    System.arraycopy(header, 0, tar, 0, 512);
    return tar;
  }

  @Test
  void resolveSiblingReplacesLastSegmentWhenParentExists() throws Exception {
    Path tar = Files.createTempFile("mutagate69", ".tar");
    FileSystem raw = null;
    try {
      Files.write(tar, buildTarWithEmptyFile("hello.txt"));
      raw = new TarFileSystemProvider().newFileSystem(tar, Map.of());
      TarFileSystem fs = (TarFileSystem) raw;

      TarPath path = new TarPath(fs, true, "a/b");
      Path result = path.resolveSibling(new TarPath(fs, false, "c"));

      assertNotNull(result);
      assertEquals(new TarPath(fs, true, "a/c"), result);
      assertEquals("/a/c", result.toString());

      TarPath relative = new TarPath(fs, false, "x/y/z");
      Path relResult = relative.resolveSibling(new TarPath(fs, false, "w"));
      assertNotNull(relResult);
      assertEquals("x/y/w", relResult.toString());
    } finally {
      if (raw != null) {
        raw.close();
      }
      Files.deleteIfExists(tar);
    }
  }
}
