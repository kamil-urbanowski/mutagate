package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SingleEntryArchivePathMutaGate100Test {

  @TempDir
  Path tempDir;

  @Test
  void rootPathToFileReturnsHostArchiveFile() throws Exception {
    Path archive = tempDir.resolve("sample.txt.gz");
    try (OutputStream out = new GZIPOutputStream(Files.newOutputStream(archive))) {
      out.write("hello".getBytes(StandardCharsets.UTF_8));
    }

    try (FileSystem fs = FileSystems.newFileSystem(archive, (ClassLoader) null)) {
      Path root = fs.getPath("/");
      File file = root.toFile();
      assertNotNull(file);
      assertEquals(archive.getFileName().toString(), file.getName());
    }
  }
}
