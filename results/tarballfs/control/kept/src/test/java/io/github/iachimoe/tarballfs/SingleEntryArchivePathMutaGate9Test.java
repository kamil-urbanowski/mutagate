package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SingleEntryArchivePathMutaGate9Test {

  @TempDir
  Path tempDir;

  @Test
  void toUriCombinesPrefixAbsoluteArchivePathBangAndEntry() throws Exception {
    Path archive = tempDir.resolve("data.txt.gz");
    try (OutputStream out = new GZIPOutputStream(Files.newOutputStream(archive))) {
      out.write("hello".getBytes());
    }

    try (FileSystem fs = FileSystems.newFileSystem(archive, (ClassLoader) null)) {
      SingleEntryArchivePath entry = (SingleEntryArchivePath) fs.getPath("/data.txt");
      SingleEntryArchiveFormat format = entry.format();
      String base = format.uriSchemePrefix() + format.hostArchive(fs).toAbsolutePath() + "!";

      assertEquals(base + "data.txt", entry.toUri().toString());

      SingleEntryArchivePath relative = (SingleEntryArchivePath) fs.getPath("data.txt");
      assertEquals(entry.toUri(), relative.toUri());

      SingleEntryArchivePath nested = (SingleEntryArchivePath) fs.getPath("a/b");
      assertEquals(base + "a/b", nested.toUri().toString());

      SingleEntryArchivePath root = (SingleEntryArchivePath) fs.getPath("/");
      assertEquals(base, root.toUri().toString());
      assertTrue(root.toUri().toString().endsWith("!"));
    }
  }
}
