package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarPathMutaGate25Test {

  @TempDir
  Path tempDir;

  private static void put(byte[] header, int offset, String value) {
    byte[] b = value.getBytes(StandardCharsets.US_ASCII);
    System.arraycopy(b, 0, header, offset, b.length);
  }

  private static byte[] buildTar() throws IOException {
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
  void getNameReturnsRelativeSingleSegmentPathsAndRejectsBadIndexes() throws IOException {
    Path tar = tempDir.resolve("one.tar");
    Files.write(tar, buildTar());

    TarFileSystemProvider provider = new TarFileSystemProvider();
    TarFileSystem fs = (TarFileSystem) provider.newFileSystem(tar, Map.of());
    try {
      TarPath absolute = new TarPath(fs, true, "a/b/c");

      Path first = absolute.getName(0);
      Path middle = absolute.getName(1);
      Path last = absolute.getName(2);

      assertFalse(first.isAbsolute());
      assertFalse(middle.isAbsolute());
      assertFalse(last.isAbsolute());
      assertNull(middle.getRoot());

      assertEquals("a", first.toString());
      assertEquals("b", middle.toString());
      assertEquals("c", last.toString());

      assertEquals(1, middle.getNameCount());
      assertSame(fs, middle.getFileSystem());
      assertEquals(new TarPath(fs, false, "b"), middle);

      TarPath relative = new TarPath(fs, false, "x/y");
      assertEquals(new TarPath(fs, false, "y"), relative.getName(1));

      assertThrows(ArrayIndexOutOfBoundsException.class, () -> absolute.getName(3));
      assertThrows(ArrayIndexOutOfBoundsException.class, () -> absolute.getName(-1));
      assertThrows(ArrayIndexOutOfBoundsException.class, () -> new TarPath(fs, true, "").getName(0));
    } finally {
      fs.close();
    }
  }
}
