package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import org.junit.jupiter.api.Test;

class ReadOnlySeekableFileChannelMutaGate9Test {

  private static final class MemoryChannel implements SeekableByteChannel {
    private final byte[] data;
    private int pos;
    private boolean open = true;

    MemoryChannel(byte[] data) {
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
    public void close() {
      open = false;
    }
  }

  @Test
  void scatteringReadHonoursOffsetAndLength() throws IOException {
    byte[] data = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
    try (ReadOnlySeekableFileChannel channel =
        new ReadOnlySeekableFileChannel(new MemoryChannel(data))) {
      ByteBuffer[] dsts = {
        ByteBuffer.allocate(2), ByteBuffer.allocate(3), ByteBuffer.allocate(4)
      };

      long read = channel.read(dsts, 1, 2);

      assertEquals(7L, read);
      assertEquals(0, dsts[0].position());
      assertEquals(3, dsts[1].position());
      assertEquals(4, dsts[2].position());
      assertArrayEquals(new byte[] {0, 1, 2}, dsts[1].array());
      assertArrayEquals(new byte[] {3, 4, 5, 6}, dsts[2].array());
      assertEquals(7L, channel.position());
    }
  }
}
