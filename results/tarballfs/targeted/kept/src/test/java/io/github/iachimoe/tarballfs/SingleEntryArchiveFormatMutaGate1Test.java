package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class SingleEntryArchiveFormatMutaGate1Test {

  @Test
  void gzipHostArchiveReturnsFileSystemGzipFile() throws Exception {
    Path gzip = Files.createTempFile("mutagate", ".gz");
    Path temp = Files.createTempFile("mutagate", ".tmp");
    try {
      GzipFileSystem fs =
          new GzipFileSystem(
              new GzipFileSystemProvider(),
              URI.create("gzip:" + gzip.toUri()),
              gzip,
              "entry",
              temp);

      Path result = SingleEntryArchiveFormat.GZIP.hostArchive(fs);

      assertNotNull(result);
      assertEquals(fs.gzipFile(), result);
    } finally {
      Files.deleteIfExists(gzip);
      Files.deleteIfExists(temp);
    }
  }
}
