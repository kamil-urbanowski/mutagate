package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.ByteBuffer;
import org.junit.jupiter.api.Test;

class ByteArraySeekableByteChannelMutaGate5Test {

  @Test
  void sizeReflectsDataLengthAndIsIndependentOfPositionReadAndClose() throws Exception {
    ByteArraySeekableByteChannel empty = new ByteArraySeekableByteChannel(new byte[0]);
    assertEquals(0L, empty.size());

    ByteArraySeekableByteChannel nullData = new ByteArraySeekableByteChannel(null);
    assertEquals(0L, nullData.size());

    ByteArraySeekableByteChannel channel =
        new ByteArraySeekableByteChannel(new byte[] {1, 2, 3, 4, 5});
    assertEquals(5L, channel.size());

    channel.position(3);
    assertEquals(5L, channel.size());

    channel.position(100);
    assertEquals(5L, channel.size());

    channel.position(0);
    ByteBuffer buf = ByteBuffer.allocate(10);
    assertEquals(5, channel.read(buf));
    assertEquals(5L, channel.size());
    assertEquals(-1, channel.read(ByteBuffer.allocate(1)));
    assertEquals(5L, channel.size());

    channel.close();
    assertEquals(5L, channel.size());
  }
}
