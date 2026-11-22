package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.ByteBuffer;
import org.junit.jupiter.api.Test;

class ByteArraySeekableByteChannelMutaGate8Test {

  @Test
  void readAtEndReturnsMinusOne() throws Exception {
    ByteArraySeekableByteChannel channel =
        new ByteArraySeekableByteChannel(new byte[] {1, 2, 3});
    ByteBuffer buf = ByteBuffer.allocate(10);

    assertEquals(3, channel.read(buf));
    assertEquals(-1, channel.read(ByteBuffer.allocate(4)));

    channel.position(3);
    assertEquals(-1, channel.read(ByteBuffer.allocate(4)));
  }
}
