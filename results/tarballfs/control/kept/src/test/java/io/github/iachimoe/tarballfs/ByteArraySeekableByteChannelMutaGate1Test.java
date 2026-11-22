package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import org.junit.jupiter.api.Test;

class ByteArraySeekableByteChannelMutaGate1Test {

  @Test
  void readBehaviors() throws Exception {
    byte[] data = {1, 2, 3, 4, 5};
    ByteArraySeekableByteChannel ch = new ByteArraySeekableByteChannel(data);

    // Partial read limited by destination capacity
    ByteBuffer small = ByteBuffer.allocate(2);
    assertEquals(2, ch.read(small));
    assertArrayEquals(new byte[] {1, 2}, small.array());
    assertEquals(2, ch.position());

    // Destination larger than remaining data: only remaining bytes are read
    ByteBuffer big = ByteBuffer.allocate(10);
    assertEquals(3, ch.read(big));
    assertEquals(3, big.position());
    assertEquals(7, big.remaining());
    assertArrayEquals(new byte[] {3, 4, 5, 0, 0, 0, 0, 0, 0, 0}, big.array());
    assertEquals(5, ch.position());

    // At end of data returns -1 and leaves position unchanged
    ByteBuffer eof = ByteBuffer.allocate(4);
    assertEquals(-1, ch.read(eof));
    assertEquals(0, eof.position());
    assertEquals(5, ch.position());

    // Zero-remaining destination in the middle returns 0 and doesn't move position
    ch.position(1);
    ByteBuffer empty = ByteBuffer.allocate(0);
    assertEquals(0, ch.read(empty));
    assertEquals(1, ch.position());

    // Respects the destination's current position offset
    ByteBuffer offset = ByteBuffer.allocate(5);
    offset.position(3);
    assertEquals(2, ch.read(offset));
    assertArrayEquals(new byte[] {0, 0, 0, 2, 3}, offset.array());
    assertEquals(3, ch.position());

    // Null data behaves as empty
    ByteArraySeekableByteChannel nullCh = new ByteArraySeekableByteChannel(null);
    assertEquals(-1, nullCh.read(ByteBuffer.allocate(1)));

    // Closed channel throws
    ch.close();
    assertThrows(ClosedChannelException.class, () -> ch.read(ByteBuffer.allocate(1)));
  }
}
