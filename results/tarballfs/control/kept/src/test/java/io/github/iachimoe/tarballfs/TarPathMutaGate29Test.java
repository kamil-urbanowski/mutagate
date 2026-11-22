package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarPathMutaGate29Test {

  @TempDir
  Path tempDir;

  @Test
  void compareToOrdersByStringRepresentation() throws Exception {
    Path tar = tempDir.resolve("archive.tar");
    Files.write(tar, new byte[1024]);
    TarFileSystem fs = new TarFileSystem(new TarFileSystemProvider(), tar.toUri(), tar);

    TarPath absA = new TarPath(fs, true, "a");
    TarPath relA = new TarPath(fs, false, "a");
    TarPath relAB = new TarPath(fs, false, "a/b");
    TarPath relB = new TarPath(fs, false, "b");
    TarPath root = new TarPath(fs, true, "");
    TarPath empty = new TarPath(fs, false, "");

    // Absolute paths start with '/', which sorts before letters
    assertEquals(-1, Integer.signum(absA.compareTo(relA)));
    assertEquals(1, Integer.signum(relA.compareTo(absA)));

    // Prefix sorts before longer path, with String.compareTo length difference semantics
    assertEquals(-2, relA.compareTo(relAB));
    assertEquals(2, relAB.compareTo(relA));

    // Lexicographic by character
    assertEquals(-1, Integer.signum(relAB.compareTo(relB)));
    assertEquals(1, Integer.signum(relB.compareTo(relAB)));
    assertEquals('a' - 'b', relA.compareTo(relB));

    // Root "/" vs empty relative ""
    assertEquals(1, root.compareTo(empty));
    assertEquals(-1, empty.compareTo(root));

    // Equal string forms compare to zero, including for distinct instances
    TarPath relA2 = new TarPath(fs, false, "a/");
    assertEquals(0, relA.compareTo(relA2));
    assertEquals(0, relA.compareTo(relA));

    // Comparison with a path from a different file system is by string only
    Path tar2 = tempDir.resolve("other.tar");
    Files.write(tar2, new byte[1024]);
    TarFileSystem fs2 = new TarFileSystem(new TarFileSystemProvider(), tar2.toUri(), tar2);
    TarPath otherFsA = new TarPath(fs2, false, "a");
    assertNotEquals(relA, otherFsA);
    assertEquals(0, relA.compareTo(otherFsA));
  }
}
