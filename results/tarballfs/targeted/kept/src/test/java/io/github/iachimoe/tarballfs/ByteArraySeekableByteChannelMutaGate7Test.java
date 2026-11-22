package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.ByteBuffer;
import org.junit.jupiter.api.Test;

class ByteArraySeekableByteChannelMutaGate7Test {

  @Test
  void readFromNonZeroPositionReturnsOnlyRemainingBytes() throws Exception {
    byte[] data = {1, 2, 3, 4, 5};
    ByteArraySeekableByteChannel channel = new ByteArraySeekableByteChannel(data);
    channel.position(2);

    ByteBuffer dst = ByteBuffer.allocate(10);
    int read = channel.read(dst);

    assertEquals(3, read);
    assertEquals(5L, channel.position());
    assertEquals(3, dst.position());
    byte[] got = new byte[3];
    dst.flip();
    dst.get(got);
    assertArrayEquals(new byte[] {3, 4, 5}, got);
    assertEquals(-1, channel.read(ByteBuffer.allocate(4)));
  }
}
