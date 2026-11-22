package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarPathMutaGate82Test {

  @TempDir
  Path tempDir;

  private static void put(byte[] block, int offset, String value) {
    byte[] bytes = value.getBytes(StandardCharsets.US_ASCII);
    System.arraycopy(bytes, 0, block, offset, bytes.length);
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
    String chk = String.format("%06o", sum);
    put(header, 148, chk);
    header[154] = 0;
    header[155] = ' ';

    byte[] tar = new byte[512 + 1024];
    System.arraycopy(header, 0, tar, 0, 512);
    return tar;
  }

  @Test
  void startsWithReturnsFalseWhenOtherHasMoreSegmentsThanThisPath() throws IOException {
    Path tar = tempDir.resolve("test.tar");
    Files.write(tar, buildTar());

    TarFileSystemProvider provider = new TarFileSystemProvider();
    FileSystem fs = provider.newFileSystem(tar, Collections.emptyMap());
    try {
      TarFileSystem tarFs = (TarFileSystem) fs;
      TarPath shorter = new TarPath(tarFs, false, "a");
      TarPath longer = new TarPath(tarFs, false, "a/b");

      assertFalse(shorter.startsWith(longer));
      assertTrue(longer.startsWith(shorter));
    } finally {
      fs.close();
    }
  }
}
