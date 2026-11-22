package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.ByteBuffer;
import org.junit.jupiter.api.Test;

class ByteArraySeekableByteChannelMutaGate4Test {

  @Test
  void positionTracksReadsSeeksAndEndOfData() throws Exception {
    ByteArraySeekableByteChannel channel =
        new ByteArraySeekableByteChannel(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10});

    assertEquals(0L, channel.position());

    ByteBuffer buf = ByteBuffer.allocate(4);
    assertEquals(4, channel.read(buf));
    assertEquals(4L, channel.position());

    channel.position(7);
    assertEquals(7L, channel.position());

    ByteBuffer big = ByteBuffer.allocate(20);
    assertEquals(3, channel.read(big));
    assertEquals(10L, channel.position());

    assertEquals(-1, channel.read(ByteBuffer.allocate(2)));
    assertEquals(10L, channel.position());

    channel.position(25);
    assertEquals(25L, channel.position());
    assertEquals(-1, channel.read(ByteBuffer.allocate(2)));
    assertEquals(25L, channel.position());

    channel.close();
    assertEquals(25L, channel.position());
  }
}
