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

class TarPathMutaGate7Test {

  @TempDir
  Path tempDir;

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
    // checksum field treated as spaces while computing
    for (int i = 148; i < 156; i++) {
      header[i] = ' ';
    }
    header[156] = '0';
    put(header, 257, "ustar");
    header[262] = 0;
    put(header, 263, "00");
    int sum = 0;
    for (byte b : header) {
      sum += b & 0xFF;
    }
    String chk = String.format("%06o", sum);
    put(header, 148, chk);
    header[154] = 0;
    header[155] = ' ';

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    out.writeBytes(header);
    out.writeBytes(new byte[1024]);
    return out.toByteArray();
  }

  @Test
  void endsWithReturnsTrueForEqualLengthPathsAndFalseForLongerArgument() throws Exception {
    Path tar = tempDir.resolve("one.tar");
    Files.write(tar, buildTar());

    TarFileSystemProvider provider = new TarFileSystemProvider();
    try (TarFileSystem fs = (TarFileSystem) provider.newFileSystem(tar, Map.of())) {
      TarPath path = new TarPath(fs, false, "a/b");
      TarPath same = new TarPath(fs, false, "a/b");
      TarPath longer = new TarPath(fs, false, "x/a/b");
      TarPath different = new TarPath(fs, false, "a/c");

      assertTrue(path.endsWith(same));
      assertTrue(path.endsWith("a/b"));
      assertFalse(path.endsWith(longer));
      assertFalse(path.endsWith(different));
    }
  }
}
