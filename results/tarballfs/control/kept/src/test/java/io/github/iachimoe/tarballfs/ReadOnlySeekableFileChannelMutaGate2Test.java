package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import org.junit.jupiter.api.Test;

class ReadOnlySeekableFileChannelMutaGate2Test {

  private static final class InMemoryChannel implements SeekableByteChannel {
    private final byte[] data;
    private int pos;
    private boolean open = true;

    InMemoryChannel(byte[] data) {
      this.data = data;
    }

    @Override
    public int read(ByteBuffer dst) {
      if (pos >= data.length) {
        return -1;
      }
      int n = Math.min(dst.remaining(), data.length - pos);
      dst.put(data, pos, n);
      pos += n;
      return n;
    }

    @Override
    public int write(ByteBuffer src) {
      throw new UnsupportedOperationException();
    }

    @Override
    public long position() {
      return pos;
    }

    @Override
    public SeekableByteChannel position(long newPosition) {
      pos = (int) newPosition;
      return this;
    }

    @Override
    public long size() {
      return data.length;
    }

    @Override
    public SeekableByteChannel truncate(long size) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean isOpen() {
      return open;
    }

    @Override
    public void close() throws IOException {
      open = false;
    }
  }

  @Test
  void scatterReadHonorsOffsetLengthAndReportsEof() throws IOException {
    byte[] data = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
    ReadOnlySeekableFileChannel channel =
        new ReadOnlySeekableFileChannel(new InMemoryChannel(data));

    ByteBuffer b0 = ByteBuffer.allocate(3);
    ByteBuffer b1 = ByteBuffer.allocate(4);
    ByteBuffer b2 = ByteBuffer.allocate(5);
    ByteBuffer b3 = ByteBuffer.allocate(2);
    ByteBuffer[] bufs = {b0, b1, b2, b3};

    long first = channel.read(bufs, 1, 2);

    assertEquals(9, first);
    assertEquals(0, b0.position());
    assertEquals(0, b3.position());
    assertEquals(4, b1.position());
    assertEquals(5, b2.position());
    assertArrayEquals(new byte[] {0, 1, 2, 3}, b1.array());
    assertArrayEquals(new byte[] {4, 5, 6, 7, 8}, b2.array());
    assertEquals(9, channel.position());

    b1.clear();
    b2.clear();

    // Only one byte left: partial total is returned rather than -1.
    long second = channel.read(bufs, 1, 2);
    assertEquals(1, second);
    assertEquals(1, b1.position());
    assertEquals(0, b2.position());
    assertEquals(9, b1.get(0));

    // Now at EOF with nothing read: -1.
    b1.clear();
    b2.clear();
    assertEquals(-1, channel.read(bufs, 1, 2));
  }
}
