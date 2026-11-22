package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CompressorArchiveProviderSupportMutaGate2Test {

  @Test
  void nullAndReadOnlyOptionsAcceptedButWriteOptionsRejected() {
    SingleEntryArchiveFormat format = SingleEntryArchiveFormat.GZIP;

    // null options: the original skips the loop; the mutant would throw NPE.
    assertDoesNotThrow(() -> CompressorArchiveProviderSupport.assertReadOnlyByteChannelOptions(null, format));

    // READ only: accepted.
    assertDoesNotThrow(() -> CompressorArchiveProviderSupport.assertReadOnlyByteChannelOptions(
        Set.of(StandardOpenOption.READ), format));

    // READ plus WRITE: the original throws; the mutant would skip the loop and not throw.
    Set<OpenOption> options = new HashSet<>();
    options.add(StandardOpenOption.READ);
    options.add(StandardOpenOption.WRITE);
    UnsupportedOperationException ex = assertThrows(
        UnsupportedOperationException.class,
        () -> CompressorArchiveProviderSupport.assertReadOnlyByteChannelOptions(options, format));
    assertTrue(ex.getMessage().endsWith("is read-only"));
  }
}
