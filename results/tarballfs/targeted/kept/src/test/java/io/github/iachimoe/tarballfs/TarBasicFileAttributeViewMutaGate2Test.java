package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarBasicFileAttributeViewMutaGate2Test {

  private static void putOctal(byte[] block, int offset, int length, long value) {
    String s = Long.toOctalString(value);
    StringBuilder sb = new StringBuilder();
    for (int i = s.length(); i < length - 1; i++) {
      sb.append('0');
    }
    sb.append(s);
    byte[] b = sb.toString().getBytes(StandardCharsets.US_ASCII);
    System.arraycopy(b, 0, block, offset, b.length);
  }

  private static byte[] buildTar(String name, byte[] content) {
    byte[] header = new byte[512];
    byte[] nameBytes = name.getBytes(StandardCharsets.US_ASCII);
    System.arraycopy(nameBytes, 0, header, 0, nameBytes.length);
    putOctal(header, 100, 8, 0644);
    putOctal(header, 108, 8, 0);
    putOctal(header, 116, 8, 0);
    putOctal(header, 124, 12, content.length);
    putOctal(header, 136, 12, 1700000000L);
    for (int i = 148; i < 156; i++) {
      header[i] = ' ';
    }
    header[156] = '0';
    byte[] magic = "ustar".getBytes(StandardCharsets.US_ASCII);
    System.arraycopy(magic, 0, header, 257, magic.length);
    header[263] = '0';
    header[264] = '0';
    long sum = 0;
    for (byte b : header) {
      sum += b & 0xff;
    }
    String chk = Long.toOctalString(sum);
    StringBuilder sb = new StringBuilder();
    for (int i = chk.length(); i < 6; i++) {
      sb.append('0');
    }
    sb.append(chk);
    byte[] chkBytes = sb.toString().getBytes(StandardCharsets.US_ASCII);
    System.arraycopy(chkBytes, 0, header, 148, chkBytes.length);
    header[154] = 0;
    header[155] = ' ';

    int padded = ((content.length + 511) / 512) * 512;
    byte[] out = new byte[512 + padded + 1024];
    System.arraycopy(header, 0, out, 0, 512);
    System.arraycopy(content, 0, out, 512, content.length);
    return out;
  }

  @Test
  void readAttributesReturnsNonNullAttributesForExistingEntry(@TempDir Path dir) throws Exception {
    byte[] content = "hello".getBytes(StandardCharsets.US_ASCII);
    Path tarFile = dir.resolve("test.tar");
    Files.write(tarFile, buildTar("hello.txt", content));

    TarFileSystemProvider provider = new TarFileSystemProvider();
    try (FileSystem fs = provider.newFileSystem(tarFile, Collections.<String, Object>emptyMap())) {
      Path p = fs.getPath("hello.txt");
      TarBasicFileAttributeView view = new TarBasicFileAttributeView((TarPath) p);

      assertEquals("basic", view.name());
      BasicFileAttributes attrs = view.readAttributes();
      assertNotNull(attrs);
      assertTrue(attrs.isRegularFile());
      assertFalse(attrs.isDirectory());
      assertEquals(5L, attrs.size());
    }
  }
}
