package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarBasicFileAttributeViewMutaGate2Test {

  @TempDir Path tempDir;

  private static void putOctal(byte[] block, int offset, int length, long value) {
    String s = String.format("%0" + (length - 1) + "o", value);
    byte[] b = s.getBytes(StandardCharsets.US_ASCII);
    System.arraycopy(b, 0, block, offset, b.length);
    block[offset + b.length] = 0;
  }

  private static byte[] buildTar(String name, byte[] content) {
    byte[] header = new byte[512];
    byte[] nameBytes = name.getBytes(StandardCharsets.US_ASCII);
    System.arraycopy(nameBytes, 0, header, 0, nameBytes.length);
    putOctal(header, 100, 8, 0644);
    putOctal(header, 108, 8, 0);
    putOctal(header, 116, 8, 0);
    putOctal(header, 124, 12, content.length);
    putOctal(header, 136, 12, 1600000000L);
    for (int i = 148; i < 156; i++) {
      header[i] = ' ';
    }
    header[156] = '0';
    byte[] magic = "ustar".getBytes(StandardCharsets.US_ASCII);
    System.arraycopy(magic, 0, header, 257, magic.length);
    header[262] = 0;
    header[263] = '0';
    header[264] = '0';
    long sum = 0;
    for (byte b : header) {
      sum += (b & 0xff);
    }
    String cs = String.format("%06o", sum);
    byte[] csBytes = cs.getBytes(StandardCharsets.US_ASCII);
    System.arraycopy(csBytes, 0, header, 148, csBytes.length);
    header[154] = 0;
    header[155] = ' ';

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    out.write(header, 0, header.length);
    out.write(content, 0, content.length);
    int pad = (512 - (content.length % 512)) % 512;
    out.write(new byte[pad], 0, pad);
    out.write(new byte[1024], 0, 1024);
    return out.toByteArray();
  }

  @Test
  void readAttributesReturnsRegularFileAttributesWithCorrectSize() throws Exception {
    byte[] content = "hello".getBytes(StandardCharsets.US_ASCII);
    Path tar = tempDir.resolve("test.tar");
    Files.write(tar, buildTar("hello.txt", content));

    TarFileSystemProvider provider = new TarFileSystemProvider();
    try (FileSystem fs = provider.newFileSystem(tar, Map.of())) {
      TarPath path = (TarPath) fs.getPath("hello.txt");
      TarBasicFileAttributeView view = new TarBasicFileAttributeView(path);

      BasicFileAttributes attrs = view.readAttributes();

      assertTrue(attrs.isRegularFile());
      assertFalse(attrs.isDirectory());
      assertFalse(attrs.isSymbolicLink());
      assertEquals(content.length, attrs.size());
    }
  }
}
