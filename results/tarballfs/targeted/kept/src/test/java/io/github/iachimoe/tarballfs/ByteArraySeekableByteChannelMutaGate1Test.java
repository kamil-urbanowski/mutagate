package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ByteArraySeekableByteChannelMutaGate1Test {

  @Test
  void isOpenIsTrueUntilClosed() {
    ByteArraySeekableByteChannel channel = new ByteArraySeekableByteChannel(new byte[] {1, 2, 3});
    assertTrue(channel.isOpen());
    channel.close();
    assertFalse(channel.isOpen());
  }
}
