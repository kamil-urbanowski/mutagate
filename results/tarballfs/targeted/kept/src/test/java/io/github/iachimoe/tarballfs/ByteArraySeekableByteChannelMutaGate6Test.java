package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.ByteBuffer;
import org.junit.jupiter.api.Test;

class ByteArraySeekableByteChannelMutaGate6Test {

  @Test
  void readAtExactEndOfDataReturnsEndOfStream() throws Exception {
    byte[] data = new byte[] {1, 2, 3};
    ByteArraySeekableByteChannel channel = new ByteArraySeekableByteChannel(data);

    ByteBuffer first = ByteBuffer.allocate(3);
    assertEquals(3, channel.read(first));
    assertEquals(3L, channel.position());

    ByteBuffer second = ByteBuffer.allocate(4);
    assertEquals(-1, channel.read(second));
    assertEquals(0, second.position());

    channel.position(data.length);
    assertEquals(-1, channel.read(ByteBuffer.allocate(1)));
  }
}
