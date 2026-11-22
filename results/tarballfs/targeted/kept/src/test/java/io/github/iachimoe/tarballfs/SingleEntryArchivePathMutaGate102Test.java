package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.OutputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SingleEntryArchivePathMutaGate102Test {

  @TempDir
  Path tempDir;

  @Test
  void toUriDistinguishesEmptyAndNonEmptyEntry() throws Exception {
    Path archive = tempDir.resolve("data.txt.gz");
    try (OutputStream out = new GZIPOutputStream(Files.newOutputStream(archive))) {
      out.write("hello".getBytes("UTF-8"));
    }

    try (FileSystem fs = FileSystems.newFileSystem(archive, (ClassLoader) null)) {
      SingleEntryArchivePath named = (SingleEntryArchivePath) fs.getPath("data.txt");
      SingleEntryArchiveFormat format = named.format();
      SingleEntryArchivePath empty = new SingleEntryArchivePath(fs, format, true, "");

      String base = format.uriSchemePrefix() + format.hostArchive(fs).toAbsolutePath();

      assertEquals(URI.create(base + "!"), empty.toUri());
      assertEquals(URI.create(base + "!data.txt"), named.toUri());
      assertNotEquals(empty.toUri(), named.toUri());
    }
  }
}
