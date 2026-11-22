package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.junit.jupiter.api.Test;

class SingleEntryArchivePathMutaGate106Test {

  @Test
  void iteratorYieldsEachSegmentThenStops() {
    SingleEntryArchivePath path = new SingleEntryArchivePath(
        FileSystems.getDefault(), SingleEntryArchiveFormat.GZIP, false, "a/b/c");

    Iterator<Path> it = path.iterator();
    assertTrue(it.hasNext());

    List<String> names = new ArrayList<>();
    while (it.hasNext()) {
      names.add(it.next().toString());
    }

    assertEquals(List.of("a", "b", "c"), names);
    assertFalse(it.hasNext());

    SingleEntryArchivePath empty = new SingleEntryArchivePath(
        FileSystems.getDefault(), SingleEntryArchiveFormat.GZIP, false, "");
    assertFalse(empty.iterator().hasNext());
  }
}
