package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.ByteBuffer;
import org.junit.jupiter.api.Test;

class ByteArraySeekableByteChannelMutaGate9Test {

  @Test
  void sizeReturnsDataLengthAndIsIndependentOfPosition() throws Exception {
    ByteArraySeekableByteChannel channel =
        new ByteArraySeekableByteChannel(new byte[] {1, 2, 3, 4, 5});
    assertEquals(5L, channel.size());

    channel.read(ByteBuffer.allocate(2));
    assertEquals(5L, channel.size());

    ByteArraySeekableByteChannel empty = new ByteArraySeekableByteChannel(null);
    assertEquals(0L, empty.size());
  }
}
