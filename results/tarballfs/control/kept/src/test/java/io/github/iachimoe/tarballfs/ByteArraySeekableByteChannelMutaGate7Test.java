package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import org.junit.jupiter.api.Test;

class ByteArraySeekableByteChannelMutaGate7Test {

  @Test
  void isOpenReflectsLifecycle() throws IOException {
    ByteArraySeekableByteChannel channel = new ByteArraySeekableByteChannel(new byte[] {1, 2, 3});
    assertTrue(channel.isOpen());

    // Operations on an open channel do not change the open state
    channel.read(ByteBuffer.allocate(2));
    channel.position(1);
    assertTrue(channel.isOpen());

    channel.close();
    assertFalse(channel.isOpen());

    // Closing again is idempotent and the channel stays closed
    channel.close();
    assertFalse(channel.isOpen());

    // Failed operations on a closed channel don't reopen it
    assertThrows(ClosedChannelException.class, () -> channel.read(ByteBuffer.allocate(1)));
    assertFalse(channel.isOpen());

    // A null-backed channel is also open until closed
    ByteArraySeekableByteChannel empty = new ByteArraySeekableByteChannel(null);
    assertTrue(empty.isOpen());
    empty.close();
    assertFalse(empty.isOpen());
  }
}
