package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.ClosedFileSystemException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarPathMutaGate10Test {

  @TempDir
  Path tempDir;

  private static byte[] buildTar(String name, String content) throws IOException {
    byte[] data = content.getBytes(StandardCharsets.US_ASCII);
    byte[] header = new byte[512];
    put(header, 0, name);
    put(header, 100, "0000644\0");
    put(header, 108, "0000000\0");
    put(header, 116, "0000000\0");
    put(header, 124, String.format("%011o", data.length) + "\0");
    put(header, 136, String.format("%011o", 0) + "\0");
    for (int i = 148; i < 156; i++) {
      header[i] = ' ';
    }
    header[156] = '0';
    put(header, 257, "ustar\0");
    put(header, 263, "00");
    long sum = 0;
    for (byte b : header) {
      sum += (b & 0xff);
    }
    put(header, 148, String.format("%06o", sum) + "\0 ");

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    out.write(header);
    out.write(data);
    int pad = (512 - (data.length % 512)) % 512;
    out.write(new byte[pad]);
    out.write(new byte[1024]);
    return out.toByteArray();
  }

  private static void put(byte[] buf, int offset, String s) {
    byte[] b = s.getBytes(StandardCharsets.US_ASCII);
    System.arraycopy(b, 0, buf, offset, b.length);
  }

  @Test
  void toRealPathNormalizesChecksExistenceAndFailsWhenClosed() throws IOException {
    Path tar = tempDir.resolve("test.tar");
    Files.write(tar, buildTar("hello.txt", "hello"));

    FileSystem fs = new TarFileSystemProvider().newFileSystem(tar, Map.of());
    Path existing;
    try {
      existing = fs.getPath("dir/../hello.txt");
      Path real = existing.toRealPath();
      assertTrue(real.isAbsolute());
      assertEquals("/hello.txt", real.toString());

      Path missing = fs.getPath("a/./b/../missing.txt");
      NoSuchFileException ex = assertThrows(NoSuchFileException.class, missing::toRealPath);
      assertEquals("/a/missing.txt", ex.getFile());
    } finally {
      fs.close();
    }

    assertThrows(ClosedFileSystemException.class, existing::toRealPath);
  }
}
