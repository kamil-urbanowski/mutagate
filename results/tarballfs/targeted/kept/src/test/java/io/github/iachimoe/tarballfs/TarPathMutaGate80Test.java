package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarPathMutaGate80Test {

  @TempDir
  Path tempDir;

  private static void put(byte[] block, int offset, String value) {
    byte[] bytes = value.getBytes(StandardCharsets.US_ASCII);
    System.arraycopy(bytes, 0, block, offset, bytes.length);
  }

  private static byte[] buildTar() {
    byte[] data = new byte[10240];
    put(data, 0, "a/");
    put(data, 100, "0000755\0");
    put(data, 108, "0000000\0");
    put(data, 116, "0000000\0");
    put(data, 124, "00000000000\0");
    put(data, 136, "00000000000\0");
    for (int i = 148; i < 156; i++) {
      data[i] = ' ';
    }
    data[156] = '5';
    put(data, 257, "ustar\0");
    put(data, 263, "00");
    int sum = 0;
    for (int i = 0; i < 512; i++) {
      sum += data[i] & 0xff;
    }
    put(data, 148, String.format("%06o", sum) + "\0 ");
    return data;
  }

  @Test
  void startsWithReturnsFalseForNonTarPath() throws Exception {
    Path tar = tempDir.resolve("archive.tar");
    Files.write(tar, buildTar());

    try (FileSystem fs = new TarFileSystemProvider().newFileSystem(tar, Map.of())) {
      Path tarPath = fs.getPath("/a/b");
      Path foreign = Path.of("/a");

      assertFalse(tarPath.startsWith(foreign));
      assertTrue(tarPath.startsWith(fs.getPath("/a")));
    }
  }
}
