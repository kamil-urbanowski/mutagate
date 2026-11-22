package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import org.junit.jupiter.api.Test;

class ByteArraySeekableByteChannelMutaGate5Test {

  @Test
  void positionReturnsSameChannelAllowingChaining() throws Exception {
    ByteArraySeekableByteChannel channel =
        new ByteArraySeekableByteChannel(new byte[] {1, 2, 3, 4, 5});

    SeekableByteChannel returned = channel.position(2);

    assertSame(channel, returned);
    assertEquals(2L, returned.position());

    ByteBuffer buf = ByteBuffer.allocate(2);
    int n = channel.position(1).read(buf);
    assertEquals(2, n);
    assertEquals(2, buf.get(0));
    assertEquals(3, buf.get(1));
  }
}
