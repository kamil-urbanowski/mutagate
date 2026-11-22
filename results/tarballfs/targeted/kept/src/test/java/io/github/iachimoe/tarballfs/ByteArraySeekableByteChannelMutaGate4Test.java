package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import org.junit.jupiter.api.Test;

class ByteArraySeekableByteChannelMutaGate4Test {

  @Test
  void positionAcceptsBoundaryValuesAndRejectsOutOfRange() throws Exception {
    ByteArraySeekableByteChannel channel =
        new ByteArraySeekableByteChannel(new byte[] {1, 2, 3});

    // Lower boundary: 0 is valid
    SeekableByteChannel result = channel.position(0L);
    assertSame(channel, result);
    assertEquals(0L, channel.position());

    // Upper boundary: Integer.MAX_VALUE is valid
    channel.position((long) Integer.MAX_VALUE);
    assertEquals((long) Integer.MAX_VALUE, channel.position());
    assertEquals(-1, channel.read(ByteBuffer.allocate(1)));

    // Just outside the boundaries is invalid
    assertThrows(IllegalArgumentException.class, () -> channel.position(-1L));
    assertThrows(
        IllegalArgumentException.class, () -> channel.position((long) Integer.MAX_VALUE + 1L));

    // Failed calls must not change the position
    assertEquals((long) Integer.MAX_VALUE, channel.position());
  }
}
