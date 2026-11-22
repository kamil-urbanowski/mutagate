package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarPathMutaGate14Test {

  @TempDir
  Path tempDir;

  private static void put(byte[] block, int offset, String value) {
    byte[] bytes = value.getBytes(StandardCharsets.US_ASCII);
    System.arraycopy(bytes, 0, block, offset, bytes.length);
  }

  private static byte[] buildTar() {
    byte[] tar = new byte[512 + 1024];
    byte[] header = new byte[512];
    put(header, 0, "file.txt");
    put(header, 100, "0000644\0");
    put(header, 108, "0000000\0");
    put(header, 116, "0000000\0");
    put(header, 124, "00000000000\0");
    put(header, 136, "00000000000\0");
    Arrays.fill(header, 148, 156, (byte) ' ');
    header[156] = '0';
    put(header, 257, "ustar\0");
    put(header, 263, "00");
    int sum = 0;
    for (byte b : header) {
      sum += b & 0xff;
    }
    put(header, 148, String.format("%06o", sum));
    header[154] = 0;
    header[155] = ' ';
    System.arraycopy(header, 0, tar, 0, 512);
    return tar;
  }

  @Test
  void endsWithComparesTrailingSegments() throws Exception {
    Path tar = tempDir.resolve("sample.tar");
    Files.write(tar, buildTar());

    TarFileSystemProvider provider = new TarFileSystemProvider();
    try (TarFileSystem fs = (TarFileSystem) provider.newFileSystem(tar, Map.of())) {
      TarPath path = new TarPath(fs, false, "a/b/c");

      assertTrue(path.endsWith(new TarPath(fs, false, "b/c")));
      assertTrue(path.endsWith(new TarPath(fs, false, "c")));
      assertFalse(path.endsWith(new TarPath(fs, false, "b/x")));
      assertFalse(path.endsWith(new TarPath(fs, false, "x")));
    }
  }
}
