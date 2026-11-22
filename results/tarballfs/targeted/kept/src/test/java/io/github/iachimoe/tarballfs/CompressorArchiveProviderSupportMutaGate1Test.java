package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CompressorArchiveProviderSupportMutaGate1Test {

  @Test
  void assertReadOnlyByteChannelOptionsHandlesReadDetection() {
    SingleEntryArchiveFormat format = SingleEntryArchiveFormat.GZIP;
    String label = format.providerDisplayName();

    // null options: allowed
    assertDoesNotThrow(() -> CompressorArchiveProviderSupport.assertReadOnlyByteChannelOptions(null, format));

    // empty options: allowed
    Set<OpenOption> empty = new HashSet<>();
    assertDoesNotThrow(() -> CompressorArchiveProviderSupport.assertReadOnlyByteChannelOptions(empty, format));

    // READ only: allowed
    Set<OpenOption> readOnly = new HashSet<>();
    readOnly.add(StandardOpenOption.READ);
    assertDoesNotThrow(() -> CompressorArchiveProviderSupport.assertReadOnlyByteChannelOptions(readOnly, format));

    // WRITE without READ: rejected by the read check, with its specific message
    Set<OpenOption> writeOnly = new HashSet<>();
    writeOnly.add(StandardOpenOption.WRITE);
    UnsupportedOperationException ex = assertThrows(
        UnsupportedOperationException.class,
        () -> CompressorArchiveProviderSupport.assertReadOnlyByteChannelOptions(writeOnly, format));
    assertEquals(label + " supports read-only operations", ex.getMessage());

    // READ plus WRITE: passes the read check, rejected by the loop
    Set<OpenOption> readWrite = new HashSet<>();
    readWrite.add(StandardOpenOption.READ);
    readWrite.add(StandardOpenOption.WRITE);
    UnsupportedOperationException ex2 = assertThrows(
        UnsupportedOperationException.class,
        () -> CompressorArchiveProviderSupport.assertReadOnlyByteChannelOptions(readWrite, format));
    assertEquals(label + " is read-only", ex2.getMessage());
  }
}
