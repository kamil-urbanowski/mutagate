package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarPathMutaGate30Test {

  @TempDir
  Path tempDir;

  private static void put(byte[] buf, int offset, String value) {
    byte[] bytes = value.getBytes(StandardCharsets.US_ASCII);
    System.arraycopy(bytes, 0, buf, offset, bytes.length);
  }

  private static byte[] buildTar() {
    byte[] header = new byte[512];
    put(header, 0, "a.txt");
    put(header, 100, "0000644\0");
    put(header, 108, "0000000\0");
    put(header, 116, "0000000\0");
    put(header, 124, "00000000000\0");
    put(header, 136, "00000000000\0");
    put(header, 148, "        ");
    header[156] = '0';
    put(header, 257, "ustar\0");
    put(header, 263, "00");
    int sum = 0;
    for (byte b : header) {
      sum += b & 0xFF;
    }
    put(header, 148, String.format("%06o", sum) + "\0 ");

    byte[] tar = new byte[512 + 1024];
    System.arraycopy(header, 0, tar, 0, 512);
    return tar;
  }

  @Test
  void getParentHandlesEmptyAndNonEmptyPaths() throws Exception {
    Path tar = tempDir.resolve("test.tar");
    Files.write(tar, buildTar());

    TarFileSystemProvider provider = new TarFileSystemProvider();
    TarFileSystem fs = (TarFileSystem) provider.newFileSystem(tar, Map.of());
    try {
      TarPath root = new TarPath(fs, true, "");
      assertNull(root.getParent());

      TarPath emptyRelative = new TarPath(fs, false, "");
      assertNull(emptyRelative.getParent());

      TarPath singleRelative = new TarPath(fs, false, "a");
      assertNull(singleRelative.getParent());

      TarPath singleAbsolute = new TarPath(fs, true, "a");
      assertEquals(new TarPath(fs, true, ""), singleAbsolute.getParent());

      TarPath multi = new TarPath(fs, false, "a/b/c");
      Path parent = multi.getParent();
      assertNotNull(parent);
      assertEquals(new TarPath(fs, false, "a/b"), parent);
    } finally {
      fs.close();
    }
  }
}
