package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Iterator;
import org.junit.jupiter.api.Test;

class SingleEntryArchivePathMutaGate107Test {

  @Test
  void iteratorStopsAfterLastSegment() {
    SingleEntryArchivePath path =
        new SingleEntryArchivePath(FileSystems.getDefault(), SingleEntryArchiveFormat.GZIP, false, "a/b");

    Iterator<Path> it = path.iterator();
    assertTrue(it.hasNext());
    assertEquals("a", it.next().toString());
    assertTrue(it.hasNext());
    assertEquals("b", it.next().toString());
    assertFalse(it.hasNext());

    SingleEntryArchivePath empty =
        new SingleEntryArchivePath(FileSystems.getDefault(), SingleEntryArchiveFormat.GZIP, false, "");
    assertFalse(empty.iterator().hasNext());
  }
}
