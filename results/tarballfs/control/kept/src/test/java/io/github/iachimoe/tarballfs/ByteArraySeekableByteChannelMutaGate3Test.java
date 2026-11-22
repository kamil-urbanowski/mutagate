package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.ByteBuffer;
import java.nio.channels.NonWritableChannelException;
import org.junit.jupiter.api.Test;

class ByteArraySeekableByteChannelMutaGate3Test {

  @Test
  void writeThrowsNonWritableAndLeavesStateUnchanged() throws Exception {
    byte[] data = {1, 2, 3, 4};
    ByteArraySeekableByteChannel channel = new ByteArraySeekableByteChannel(data);
    ByteBuffer src = ByteBuffer.wrap(new byte[] {9, 9});

    assertThrows(NonWritableChannelException.class, () -> channel.write(src));

    assertEquals(2, src.remaining());
    assertEquals(0, src.position());
    assertEquals(0L, channel.position());
    assertEquals(4L, channel.size());
    assertEquals(true, channel.isOpen());

    ByteBuffer dst = ByteBuffer.allocate(4);
    assertEquals(4, channel.read(dst));
    assertEquals(1, dst.get(0));
    assertEquals(2, dst.get(1));
    assertEquals(3, dst.get(2));
    assertEquals(4, dst.get(3));

    channel.close();
    assertThrows(NonWritableChannelException.class, () -> channel.write(ByteBuffer.allocate(1)));
  }
}
