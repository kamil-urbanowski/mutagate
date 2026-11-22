package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class TarPathMutaGate9Test {

  @Test
  void toUriBuildsTarUriFromArchiveAndEntryName() throws Exception {
    Path tar = Files.createTempFile("archive", ".tar");
    try {
      Files.write(tar, new byte[1024]);
      TarFileSystemProvider provider = new TarFileSystemProvider();
      TarFileSystem fs = new TarFileSystem(provider, URI.create("tar:" + tar.toAbsolutePath() + "!"), tar);
      try {
        String archive = fs.tarFile().toAbsolutePath().toString();

        // Root: no entry, URI ends with "!"
        assertEquals(URI.create("tar:" + archive + "!"), new TarPath(fs, true, "").toUri());
        assertEquals(URI.create("tar:" + archive + "!"), new TarPath(fs, false, "").toUri());

        // Entry path, absolute and relative produce the same URI
        URI expected = URI.create("tar:" + archive + "!dir/file.txt");
        assertEquals(expected, new TarPath(fs, true, "/dir/file.txt").toUri());
        assertEquals(expected, new TarPath(fs, false, "dir/file.txt").toUri());

        // Trailing slashes and backslashes are normalized in the entry name
        assertEquals(expected, new TarPath(fs, true, "dir\\file.txt/").toUri());

        // Scheme is "tar"
        assertEquals("tar", expected.getScheme());
      } finally {
        fs.close();
      }
    } finally {
      Files.deleteIfExists(tar);
    }
  }
}
