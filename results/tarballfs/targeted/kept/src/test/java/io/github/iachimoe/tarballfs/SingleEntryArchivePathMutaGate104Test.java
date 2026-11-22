package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SingleEntryArchivePathMutaGate104Test {

  @TempDir
  Path tempDir;

  @Test
  void toUriForNamedEntryIsNonNullAndEndsWithEntry() throws Exception {
    Path archive = tempDir.resolve("sample.txt.gz");
    try (OutputStream out = new GZIPOutputStream(Files.newOutputStream(archive))) {
      out.write("hello".getBytes(StandardCharsets.UTF_8));
    }

    try (FileSystem fs = FileSystems.newFileSystem(archive)) {
      Path entry = fs.getPath("/sample.txt");
      URI uri = entry.toUri();
      assertNotNull(uri);
      assertTrue(uri.toString().endsWith("!sample.txt"), uri.toString());

      URI rootUri = fs.getPath("/").toUri();
      assertNotNull(rootUri);
      assertTrue(rootUri.toString().endsWith("!"), rootUri.toString());
      assertEquals(rootUri.toString() + "sample.txt", uri.toString());
    }
  }
}
