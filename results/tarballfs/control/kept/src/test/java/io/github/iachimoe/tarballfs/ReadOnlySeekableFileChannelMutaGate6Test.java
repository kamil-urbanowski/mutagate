package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.ClosedChannelException;
import org.junit.jupiter.api.Test;

class ReadOnlySeekableFileChannelMutaGate6Test {

  private static final class ArrayChannel implements SeekableByteChannel {
    private final byte[] data;
    private long pos;
    private boolean open = true;
    int readCalls;

    ArrayChannel(byte[] data) {
      this.data = data;
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
      readCalls++;
      if (!open) {
        throw new ClosedChannelException();
      }
      if (pos >= data.length) {
        return -1;
      }
      int n = (int) Math.min(dst.remaining(), data.length - pos);
      dst.put(data, (int) pos, n);
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
      pos = newPosition;
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
    public void close() {
      open = false;
    }
  }

  @Test
  void readDelegatesAndAdvancesPositionUntilEof() throws IOException {
    byte[] data = {1, 2, 3, 4, 5};
    ArrayChannel delegate = new ArrayChannel(data);
    ReadOnlySeekableFileChannel channel = new ReadOnlySeekableFileChannel(delegate);

    ByteBuffer first = ByteBuffer.allocate(3);
    assertEquals(3, channel.read(first));
    assertArrayEquals(new byte[] {1, 2, 3}, first.array());
    assertEquals(3, channel.position());

    ByteBuffer second = ByteBuffer.allocate(10);
    assertEquals(2, channel.read(second));
    assertEquals(2, second.position());
    assertEquals(5, channel.position());
    assertEquals(4, second.array()[0]);
    assertEquals(5, second.array()[1]);

    ByteBuffer third = ByteBuffer.allocate(4);
    assertEquals(-1, channel.read(third));
    assertEquals(0, third.position());
    assertEquals(3, delegate.readCalls);

    channel.close();
    try {
      channel.read(ByteBuffer.allocate(1));
      throw new AssertionError("expected exception");
    } catch (ClosedChannelException expected) {
      // expected
    }
  }
}
