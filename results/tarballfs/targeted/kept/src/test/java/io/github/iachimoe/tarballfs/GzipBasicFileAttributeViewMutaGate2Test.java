package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.GZIPOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GzipBasicFileAttributeViewMutaGate2Test {

  @TempDir Path tempDir;

  @Test
  void readAttributesReturnsNonNullAttributes() throws Exception {
    Path gz = tempDir.resolve("sample.txt.gz");
    try (OutputStream out = new GZIPOutputStream(Files.newOutputStream(gz))) {
      out.write("hello world".getBytes(StandardCharsets.UTF_8));
    }

    try (FileSystem fs = FileSystems.newFileSystem(gz, (ClassLoader) null)) {
      Path root = fs.getRootDirectories().iterator().next();
      SingleEntryArchivePath archivePath = (SingleEntryArchivePath) root;
      GzipBasicFileAttributeView view =
          new GzipBasicFileAttributeView(
              SingleEntryArchivePath.require(archivePath, archivePath.format()));

      BasicFileAttributes attributes = view.readAttributes();

      assertNotNull(attributes);
    }
  }
}
