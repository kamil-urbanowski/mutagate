package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarPathMutaGate36Test {

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
    Arrays.fill(header, 148, 156, (byte) ' ');
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
  void hashCodeCombinesFileSystemAbsoluteFlagAndSegments() throws Exception {
    Path tar = tempDir.resolve("archive.tar");
    Files.write(tar, buildTar());

    TarFileSystemProvider provider = new TarFileSystemProvider();
    try (FileSystem fs = provider.newFileSystem(tar, Map.of())) {
      TarFileSystem tarFs = (TarFileSystem) fs;
      TarPath path = new TarPath(tarFs, true, "a/b");

      int base = Objects.hash(System.identityHashCode(tarFs), true);
      int expected = 31 * base + Arrays.hashCode(new String[] {"a", "b"});

      assertEquals(expected, path.hashCode());
    }
  }
}
