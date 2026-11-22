package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import org.junit.jupiter.api.Test;

class ReadOnlySeekableFileChannelMutaGate16Test {

  private static final class MemoryChannel implements SeekableByteChannel {
    private final byte[] data;
    private long pos;
    private boolean open = true;

    MemoryChannel(byte[] data) {
      this.data = data;
    }

    @Override
    public int read(ByteBuffer dst) {
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
  void scatteringReadReturnsTotalBytesRead() throws IOException {
    byte[] data = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
    try (ReadOnlySeekableFileChannel channel =
        new ReadOnlySeekableFileChannel(new MemoryChannel(data))) {
      ByteBuffer a = ByteBuffer.allocate(3);
      ByteBuffer b = ByteBuffer.allocate(4);

      long result = channel.read(new ByteBuffer[] {a, b}, 0, 2);

      assertEquals(7L, result);
      assertEquals(7L, channel.position());
      assertEquals(1, a.get(0));
      assertEquals(3, a.get(2));
      assertEquals(4, b.get(0));
      assertEquals(7, b.get(3));
    }
  }
}
