package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.FileSystems;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CompressorArchiveProviderSupportMutaGate9Test {

  @Test
  void writeOptionIsRejectedBeforeAnyFileSystemAccess() {
    SingleEntryArchiveFormat format = SingleEntryArchiveFormat.GZIP;
    SingleEntryArchivePath path =
        new SingleEntryArchivePath(FileSystems.getDefault(), format, true, "entry");
    Set<OpenOption> options = Set.of(StandardOpenOption.READ, StandardOpenOption.WRITE);

    UnsupportedOperationException ex =
        assertThrows(
            UnsupportedOperationException.class,
            () -> CompressorArchiveProviderSupport.newReadOnlyPayloadByteChannel(path, options, format));

    assertTrue(ex.getMessage().contains("read-only"));
  }
}
