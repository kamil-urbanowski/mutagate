package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import org.junit.jupiter.api.Test;

class ByteArraySeekableByteChannelMutaGate8Test {

  @Test
  void closeMarksChannelClosedAndAffectsSubsequentOperations() throws IOException {
    ByteArraySeekableByteChannel channel =
        new ByteArraySeekableByteChannel(new byte[] {1, 2, 3, 4});
    assertTrue(channel.isOpen());

    channel.read(ByteBuffer.allocate(2));
    assertEquals(2L, channel.position());

    channel.close();
    assertFalse(channel.isOpen());

    assertThrows(ClosedChannelException.class, () -> channel.read(ByteBuffer.allocate(1)));
    assertThrows(ClosedChannelException.class, () -> channel.position(0));

    // position() and size() do not check open state, and state is preserved
    assertEquals(2L, channel.position());
    assertEquals(4L, channel.size());

    // closing again is idempotent
    channel.close();
    assertFalse(channel.isOpen());
  }
}
