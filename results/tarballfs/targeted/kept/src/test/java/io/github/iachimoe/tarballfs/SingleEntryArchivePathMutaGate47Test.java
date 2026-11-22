package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class SingleEntryArchivePathMutaGate47Test {

  @Test
  void normalizeKeepsLeadingParentOnlyForRelativePaths() {
    var fs = FileSystems.getDefault();

    Path relative = new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.GZIP, false, "../a");
    Path absolute = new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.GZIP, true, "../a");

    assertEquals("../a", relative.normalize().toString());
    assertEquals("/a", absolute.normalize().toString());
  }
}
