package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertSame;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

class SingleEntryArchiveFormatMutaGate8Test {

  @Test
  void bzip2HostArchiveReturnsTheBzip2FileOfTheFileSystem() {
    Path bzip2Path = Paths.get("archive.bz2").toAbsolutePath();
    Path tempFile = Paths.get("archive.tmp").toAbsolutePath();
    URI uri = URI.create("bzip2:" + bzip2Path.toUri());

    Bzip2FileSystem fs =
        new Bzip2FileSystem(new Bzip2FileSystemProvider(), uri, bzip2Path, "archive", tempFile);

    Path result = SingleEntryArchiveFormat.BZIP2.hostArchive(fs);

    assertSame(fs.bzip2File(), result);
  }
}
