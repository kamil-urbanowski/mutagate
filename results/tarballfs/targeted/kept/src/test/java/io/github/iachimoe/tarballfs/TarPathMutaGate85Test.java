package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;

class TarPathMutaGate85Test {

  private static void put(byte[] header, int offset, String value) {
    byte[] bytes = value.getBytes(StandardCharsets.US_ASCII);
    System.arraycopy(bytes, 0, header, offset, bytes.length);
  }

  private static byte[] buildTar() throws Exception {
    byte[] header = new byte[512];
    put(header, 0, "a.txt");
    put(header, 100, "0000644");
    put(header, 108, "0000000");
    put(header, 116, "0000000");
    put(header, 124, "00000000000");
    put(header, 136, "00000000000");
    for (int i = 148; i < 156; i++) {
      header[i] = ' ';
    }
    header[156] = '0';
    put(header, 257, "ustar");
    put(header, 263, "00");
    int sum = 0;
    for (byte b : header) {
      sum += b & 0xff;
    }
    put(header, 148, String.format("%06o", sum));
    header[154] = 0;
    header[155] = ' ';

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    out.write(header);
    out.write(new byte[1024]);
    return out.toByteArray();
  }

  @Test
  void subpathBoundaries() throws Exception {
    Path tar = Files.createTempFile("tarpath-test", ".tar");
    FileSystem fs = null;
    try {
      Files.write(tar, buildTar());
      fs = new TarFileSystemProvider().newFileSystem(tar, Map.of());
      TarPath path = new TarPath((TarFileSystem) fs, true, "a/b/c");

      // beginIndex == 0 must be accepted
      assertEquals("a", path.subpath(0, 1).toString());
      // endIndex == segments.length must be accepted
      assertEquals("a/b/c", path.subpath(0, 3).toString());
      assertEquals("c", path.subpath(2, 3).toString());

      // beginIndex == endIndex must be rejected
      assertThrows(IllegalArgumentException.class, () -> path.subpath(1, 1));
      // out of range values must be rejected
      assertThrows(IllegalArgumentException.class, () -> path.subpath(-1, 1));
      assertThrows(IllegalArgumentException.class, () -> path.subpath(0, 4));
      assertThrows(IllegalArgumentException.class, () -> path.subpath(2, 1));
    } finally {
      if (fs != null) {
        fs.close();
      }
      Files.deleteIfExists(tar);
    }
  }
}
