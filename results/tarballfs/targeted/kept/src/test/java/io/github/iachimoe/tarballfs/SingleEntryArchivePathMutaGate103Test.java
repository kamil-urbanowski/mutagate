package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.OutputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SingleEntryArchivePathMutaGate103Test {

  @TempDir
  Path tempDir;

  @Test
  void toUriOfRootPathIsNotNullAndEndsWithBang() throws Exception {
    Path archive = tempDir.resolve("data.txt.gz");
    try (OutputStream out = new GZIPOutputStream(Files.newOutputStream(archive))) {
      out.write("hello".getBytes("UTF-8"));
    }

    try (FileSystem fs = FileSystems.newFileSystem(archive, getClass().getClassLoader())) {
      SingleEntryArchivePath root = (SingleEntryArchivePath) fs.getPath("/");
      assertEquals("", root.entryName());

      URI uri = root.toUri();
      assertNotNull(uri);

      SingleEntryArchiveFormat format = root.format();
      String expected = format.uriSchemePrefix()
          + format.hostArchive(fs).toAbsolutePath().toString()
          + "!";
      assertEquals(URI.create(expected), uri);
      assertEquals(expected, uri.toString());
    }
  }
}
