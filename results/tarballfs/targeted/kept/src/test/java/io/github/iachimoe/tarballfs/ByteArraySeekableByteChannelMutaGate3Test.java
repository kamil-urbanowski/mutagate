package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.ByteBuffer;
import org.junit.jupiter.api.Test;

class ByteArraySeekableByteChannelMutaGate3Test {

  @Test
  void positionReflectsReadsAndExplicitSeeks() throws Exception {
    ByteArraySeekableByteChannel channel =
        new ByteArraySeekableByteChannel(new byte[] {1, 2, 3, 4, 5, 6});

    assertEquals(0L, channel.position());

    channel.read(ByteBuffer.allocate(4));
    assertEquals(4L, channel.position());

    channel.position(2);
    assertEquals(2L, channel.position());
  }
}
