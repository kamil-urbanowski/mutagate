package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SeekableByteChannel;
import org.junit.jupiter.api.Test;

class ByteArraySeekableByteChannelMutaGate2Test {

  @Test
  void positionBehavior() throws Exception {
    byte[] data = {10, 20, 30, 40, 50};
    ByteArraySeekableByteChannel ch = new ByteArraySeekableByteChannel(data);

    // Returns this and sets position
    SeekableByteChannel returned = ch.position(2);
    assertSame(ch, returned);
    assertEquals(2L, ch.position());

    // Reading starts at the new position
    ByteBuffer buf = ByteBuffer.allocate(10);
    assertEquals(3, ch.read(buf));
    assertEquals(30, buf.get(0));
    assertEquals(40, buf.get(1));
    assertEquals(50, buf.get(2));
    assertEquals(5L, ch.position());

    // Position zero is valid (boundary)
    ch.position(0);
    assertEquals(0L, ch.position());

    // Position beyond size is allowed; read returns -1
    ch.position(100);
    assertEquals(100L, ch.position());
    assertEquals(-1, ch.read(ByteBuffer.allocate(4)));

    // Max int is accepted
    ch.position(Integer.MAX_VALUE);
    assertEquals((long) Integer.MAX_VALUE, ch.position());

    // Invalid positions throw and leave position unchanged
    ch.position(3);
    assertThrows(IllegalArgumentException.class, () -> ch.position(-1));
    assertEquals(3L, ch.position());
    assertThrows(IllegalArgumentException.class, () -> ch.position((long) Integer.MAX_VALUE + 1));
    assertEquals(3L, ch.position());

    // Closed channel throws ClosedChannelException, even for invalid position
    ch.close();
    assertThrows(ClosedChannelException.class, () -> ch.position(1));
    assertThrows(ClosedChannelException.class, () -> ch.position(-1));
  }
}
