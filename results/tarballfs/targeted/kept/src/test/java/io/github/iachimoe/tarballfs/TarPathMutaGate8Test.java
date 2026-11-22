package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarPathMutaGate8Test {

  private static void put(byte[] header, int offset, String value) {
    byte[] bytes = value.getBytes(StandardCharsets.US_ASCII);
    System.arraycopy(bytes, 0, header, offset, bytes.length);
  }

  private static byte[] buildTar() throws Exception {
    byte[] h = new byte[512];
    put(h, 0, "a.txt");
    put(h, 100, "0000644\0");
    put(h, 108, "0000000\0");
    put(h, 116, "0000000\0");
    put(h, 124, "00000000000\0");
    put(h, 136, "00000000000\0");
    for (int i = 148; i < 156; i++) {
      h[i] = ' ';
    }
    h[156] = '0';
    put(h, 257, "ustar\0");
    put(h, 263, "00");
    int sum = 0;
    for (byte b : h) {
      sum += b & 0xff;
    }
    put(h, 148, String.format("%06o", sum) + "\0 ");

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    out.write(h);
    out.write(new byte[1024]);
    return out.toByteArray();
  }

  @Test
  void endsWithComparesLastSegment(@TempDir Path dir) throws Exception {
    Path tar = dir.resolve("one.tar");
    Files.write(tar, buildTar());

    TarFileSystemProvider provider = new TarFileSystemProvider();
    try (TarFileSystem fs = (TarFileSystem) provider.newFileSystem(tar, Map.of())) {
      TarPath path = new TarPath(fs, false, "a/b");

      assertTrue(path.endsWith(new TarPath(fs, false, "b")));
      assertTrue(path.endsWith(new TarPath(fs, false, "a/b")));
      assertFalse(path.endsWith(new TarPath(fs, false, "c")));
      assertFalse(path.endsWith(new TarPath(fs, false, "a")));
    }
  }
}
