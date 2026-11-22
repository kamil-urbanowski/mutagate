package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.ByteBuffer;
import org.junit.jupiter.api.Test;

class ByteArraySeekableByteChannelMutaGate9Test {

  @Test
  void constructorHandlesNullAndSharesArrayWithoutCopy() throws Exception {
    // null data behaves as an empty array
    ByteArraySeekableByteChannel nullChannel = new ByteArraySeekableByteChannel(null);
    assertEquals(0L, nullChannel.size());
    assertEquals(0L, nullChannel.position());
    assertEquals(-1, nullChannel.read(ByteBuffer.allocate(4)));
    assertTrue(nullChannel.isOpen());

    // the array is not copied: later modifications are visible through the channel
    byte[] source = new byte[] {1, 2, 3, 4};
    ByteArraySeekableByteChannel channel = new ByteArraySeekableByteChannel(source);
    assertEquals(4L, channel.size());
    source[2] = 99;
    ByteBuffer buf = ByteBuffer.allocate(4);
    assertEquals(4, channel.read(buf));
    assertEquals(1, buf.get(0));
    assertEquals(2, buf.get(1));
    assertEquals(99, buf.get(2));
    assertEquals(4, buf.get(3));
    assertEquals(4L, channel.position());
    assertSame(channel, channel.position(0));
  }
}
