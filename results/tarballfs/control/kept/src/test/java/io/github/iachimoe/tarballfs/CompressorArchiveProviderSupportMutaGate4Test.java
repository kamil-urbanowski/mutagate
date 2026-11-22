package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CompressorArchiveProviderSupportMutaGate4Test {

  @Test
  void assertReadOnlyByteChannelOptionsAcceptsReadOnlyAndRejectsMutatingOptions() {
    for (SingleEntryArchiveFormat format : SingleEntryArchiveFormat.values()) {
      String label = format.providerDisplayName();

      // Accepted cases
      assertDoesNotThrow(() -> CompressorArchiveProviderSupport.assertReadOnlyByteChannelOptions(null, format));
      assertDoesNotThrow(() -> CompressorArchiveProviderSupport.assertReadOnlyByteChannelOptions(Set.of(), format));
      assertDoesNotThrow(() -> CompressorArchiveProviderSupport.assertReadOnlyByteChannelOptions(
          Set.<OpenOption>of(StandardOpenOption.READ), format));
      assertDoesNotThrow(() -> CompressorArchiveProviderSupport.assertReadOnlyByteChannelOptions(
          Set.<OpenOption>of(StandardOpenOption.READ, StandardOpenOption.SYNC, LinkOption.NOFOLLOW_LINKS),
          format));

      // No READ present: "supports read-only operations"
      UnsupportedOperationException noRead = assertThrows(
          UnsupportedOperationException.class,
          () -> CompressorArchiveProviderSupport.assertReadOnlyByteChannelOptions(
              Set.<OpenOption>of(StandardOpenOption.WRITE), format));
      assertEquals(label + " supports read-only operations", noRead.getMessage());

      UnsupportedOperationException noReadOther = assertThrows(
          UnsupportedOperationException.class,
          () -> CompressorArchiveProviderSupport.assertReadOnlyByteChannelOptions(
              Set.<OpenOption>of(StandardOpenOption.SYNC), format));
      assertEquals(label + " supports read-only operations", noReadOther.getMessage());

      // READ plus a mutating option: "is read-only"
      StandardOpenOption[] mutating = {
        StandardOpenOption.WRITE,
        StandardOpenOption.APPEND,
        StandardOpenOption.TRUNCATE_EXISTING,
        StandardOpenOption.CREATE,
        StandardOpenOption.CREATE_NEW
      };
      for (StandardOpenOption opt : mutating) {
        UnsupportedOperationException ex = assertThrows(
            UnsupportedOperationException.class,
            () -> CompressorArchiveProviderSupport.assertReadOnlyByteChannelOptions(
                Set.<OpenOption>of(StandardOpenOption.READ, opt), format),
            opt.toString());
        assertEquals(label + " is read-only", ex.getMessage(), opt.toString());
      }
    }
  }
}
