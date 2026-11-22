package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.ByteBuffer;
import java.nio.channels.NonWritableChannelException;
import org.junit.jupiter.api.Test;

class ByteArraySeekableByteChannelMutaGate6Test {

  @Test
  void truncateAlwaysThrowsAndLeavesChannelUnchanged() throws Exception {
    byte[] data = {1, 2, 3, 4, 5};
    ByteArraySeekableByteChannel channel = new ByteArraySeekableByteChannel(data);
    channel.position(2);

    assertThrows(NonWritableChannelException.class, () -> channel.truncate(0));
    assertThrows(NonWritableChannelException.class, () -> channel.truncate(3));
    assertThrows(NonWritableChannelException.class, () -> channel.truncate(100));
    assertThrows(NonWritableChannelException.class, () -> channel.truncate(-1));

    assertEquals(5L, channel.size());
    assertEquals(2L, channel.position());
    assertTrue(channel.isOpen());

    ByteBuffer buf = ByteBuffer.allocate(10);
    int n = channel.read(buf);
    assertEquals(3, n);
    assertEquals(3, buf.get(0));
    assertEquals(4, buf.get(1));
    assertEquals(5, buf.get(2));

    channel.close();
    assertThrows(NonWritableChannelException.class, () -> channel.truncate(1));
  }
}
