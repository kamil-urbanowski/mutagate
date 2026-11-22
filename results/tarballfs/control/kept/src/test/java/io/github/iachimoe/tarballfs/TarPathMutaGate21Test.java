package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarPathMutaGate21Test {

  @TempDir
  Path tempDir;

  @Test
  void getFileSystemReturnsOwningFileSystemForPathAndDerivedPaths() throws Exception {
    Path tar1 = tempDir.resolve("a.tar");
    Path tar2 = tempDir.resolve("b.tar");
    Files.write(tar1, new byte[1024]);
    Files.write(tar2, new byte[1024]);

    TarFileSystemProvider provider = new TarFileSystemProvider();
    TarFileSystem fs1 = new TarFileSystem(provider, URI.create("tar:" + tar1.toAbsolutePath() + "!"), tar1);
    TarFileSystem fs2 = new TarFileSystem(provider, URI.create("tar:" + tar2.toAbsolutePath() + "!"), tar2);

    TarPath p1 = new TarPath(fs1, true, "dir/sub/file.txt");
    TarPath p2 = new TarPath(fs2, false, "other");

    assertSame(fs1, p1.getFileSystem());
    assertSame(fs2, p2.getFileSystem());
    assertNotSame(p1.getFileSystem(), p2.getFileSystem());

    assertSame(fs1, p1.getRoot().getFileSystem());
    assertSame(fs1, p1.getParent().getFileSystem());
    assertSame(fs1, p1.getFileName().getFileSystem());
    assertSame(fs1, p1.getName(1).getFileSystem());
    assertSame(fs1, p1.subpath(0, 2).getFileSystem());
    assertSame(fs1, p1.normalize().getFileSystem());
    assertSame(fs1, p1.toAbsolutePath().getFileSystem());
    assertSame(fs1, p1.resolve("more").getFileSystem());
    assertSame(fs1, p1.resolveSibling("sib").getFileSystem());
    assertSame(fs1, p1.relativize(new TarPath(fs1, true, "dir/x")).getFileSystem());
    assertSame(fs2, p2.toAbsolutePath().getFileSystem());

    TarPath empty = new TarPath(fs1, false, null);
    assertNotNull(empty.getFileSystem());
    assertSame(fs1, empty.getFileSystem());
  }
}
