package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarPathMutaGate49Test {

  @TempDir
  Path tempDir;

  private static void put(byte[] block, int offset, String value) {
    byte[] bytes = value.getBytes(StandardCharsets.US_ASCII);
    System.arraycopy(bytes, 0, block, offset, bytes.length);
  }

  private static byte[] buildTar() {
    byte[] header = new byte[512];
    put(header, 0, "a/");
    put(header, 100, "0000755\0");
    put(header, 108, "0000000\0");
    put(header, 116, "0000000\0");
    put(header, 124, "00000000000\0");
    put(header, 136, "00000000000\0");
    for (int i = 148; i < 156; i++) {
      header[i] = ' ';
    }
    header[156] = '5';
    put(header, 257, "ustar\0");
    put(header, 263, "00");

    int sum = 0;
    for (byte b : header) {
      sum += b & 0xFF;
    }
    put(header, 148, String.format("%06o", sum));
    header[154] = 0;
    header[155] = ' ';

    byte[] tar = new byte[512 + 1024];
    System.arraycopy(header, 0, tar, 0, 512);
    return tar;
  }

  @Test
  void relativizeAddsOneParentSegmentPerDivergingSegment() throws Exception {
    Path tar = tempDir.resolve("archive.tar");
    Files.write(tar, buildTar());

    TarFileSystemProvider provider = new TarFileSystemProvider();
    try (FileSystem fs = provider.newFileSystem(tar, Map.of())) {
      Path base = fs.getPath("a/b");
      Path other = fs.getPath("a/c");

      Path rel = base.relativize(other);

      assertEquals("../c", rel.toString());
      assertEquals(2, rel.getNameCount());
    }
  }
}
