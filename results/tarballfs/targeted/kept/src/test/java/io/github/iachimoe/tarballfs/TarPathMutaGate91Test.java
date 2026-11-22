package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarPathMutaGate91Test {

  @TempDir
  Path tempDir;

  private static void put(byte[] header, int offset, String value) {
    byte[] b = value.getBytes(StandardCharsets.US_ASCII);
    System.arraycopy(b, 0, header, offset, b.length);
  }

  private static byte[] buildTar(String name, byte[] content) throws Exception {
    byte[] h = new byte[512];
    put(h, 0, name);
    put(h, 100, "0000644");
    put(h, 108, "0000000");
    put(h, 116, "0000000");
    put(h, 124, String.format("%011o", content.length));
    put(h, 136, "00000000000");
    for (int i = 148; i < 156; i++) {
      h[i] = ' ';
    }
    h[156] = '0';
    put(h, 257, "ustar");
    put(h, 263, "00");
    int sum = 0;
    for (byte b : h) {
      sum += b & 0xff;
    }
    put(h, 148, String.format("%06o", sum));
    h[154] = 0;
    h[155] = ' ';

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    out.write(h);
    out.write(content);
    int pad = (512 - (content.length % 512)) % 512;
    out.write(new byte[pad]);
    out.write(new byte[1024]);
    return out.toByteArray();
  }

  @Test
  void toAbsolutePathHandlesRelativeAndAbsolutePaths() throws Exception {
    Path tar = tempDir.resolve("sample.tar");
    Files.write(tar, buildTar("hello.txt", "hello".getBytes(StandardCharsets.US_ASCII)));

    TarFileSystemProvider provider = new TarFileSystemProvider();
    try (FileSystem fs = provider.newFileSystem(tar, Map.of())) {
      TarFileSystem tfs = (TarFileSystem) fs;

      TarPath relative = new TarPath(tfs, false, "a/b");
      Path madeAbsolute = relative.toAbsolutePath();
      assertTrue(madeAbsolute.isAbsolute());
      assertEquals("/a/b", madeAbsolute.toString());
      assertNotSame(relative, madeAbsolute);
      assertFalse(relative.isAbsolute());

      TarPath absolute = new TarPath(tfs, true, "a/b");
      Path again = absolute.toAbsolutePath();
      assertSame(absolute, again);
      assertTrue(again.isAbsolute());
      assertEquals("/a/b", again.toString());
    }
  }
}
