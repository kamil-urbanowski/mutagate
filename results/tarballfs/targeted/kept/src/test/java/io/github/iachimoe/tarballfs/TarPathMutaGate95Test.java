package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarPathMutaGate95Test {

  private static void put(byte[] buf, int offset, String value) {
    byte[] b = value.getBytes(StandardCharsets.US_ASCII);
    System.arraycopy(b, 0, buf, offset, b.length);
  }

  private static byte[] buildTar() {
    byte[] data = new byte[512 + 1024];
    put(data, 0, "hello.txt");
    put(data, 100, "0000644");
    put(data, 108, "0000000");
    put(data, 116, "0000000");
    put(data, 124, "00000000000");
    put(data, 136, "00000000000");
    for (int i = 148; i < 156; i++) {
      data[i] = ' ';
    }
    data[156] = '0';
    put(data, 257, "ustar");
    put(data, 263, "00");
    int sum = 0;
    for (int i = 0; i < 512; i++) {
      sum += data[i] & 0xff;
    }
    put(data, 148, String.format("%06o", sum));
    data[154] = 0;
    data[155] = ' ';
    return data;
  }

  @Test
  void iteratorStopsAfterLastSegment(@TempDir Path dir) throws Exception {
    Path tar = dir.resolve("sample.tar");
    Files.write(tar, buildTar());

    TarFileSystemProvider provider = new TarFileSystemProvider();
    TarFileSystem fs = (TarFileSystem) provider.newFileSystem(tar, Collections.emptyMap());
    try {
      TarPath path = new TarPath(fs, false, "a/b/c");
      Iterator<Path> it = path.iterator();

      List<String> names = new ArrayList<>();
      while (it.hasNext()) {
        names.add(it.next().toString());
      }

      assertEquals(List.of("a", "b", "c"), names);
      assertFalse(it.hasNext());

      TarPath root = new TarPath(fs, true, "");
      assertFalse(root.iterator().hasNext());

      TarPath single = new TarPath(fs, true, "x");
      Iterator<Path> single1 = single.iterator();
      assertTrue(single1.hasNext());
      assertEquals("x", single1.next().toString());
      assertFalse(single1.hasNext());
    } finally {
      fs.close();
    }
  }
}
