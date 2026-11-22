package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.FileSystems;
import java.nio.file.attribute.BasicFileAttributeView;
import org.junit.jupiter.api.Test;

class SingleEntryArchiveFormatMutaGate9Test {

  @Test
  void bzip2FormatCreatesBzip2BasicAttributeView() {
    SingleEntryArchivePath path =
        new SingleEntryArchivePath(
            FileSystems.getDefault(), SingleEntryArchiveFormat.BZIP2, true, "entry");

    BasicFileAttributeView view = SingleEntryArchiveFormat.BZIP2.newBasicAttributeView(path);

    assertNotNull(view);
    assertTrue(view instanceof Bzip2BasicFileAttributeView);
    assertEquals(Bzip2BasicFileAttributeView.class, view.getClass());
  }
}
