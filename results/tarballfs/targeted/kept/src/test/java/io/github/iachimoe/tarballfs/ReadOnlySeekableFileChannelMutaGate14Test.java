package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import org.junit.jupiter.api.Test;

class ReadOnlySeekableFileChannelMutaGate14Test {

  private static final class BytesChannel implements SeekableByteChannel {
    private final byte[] data;
    private long pos;
    private boolean open = true;

    BytesChannel(byte[] data) {
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
  void scatteringReadAtEndOfDataReturnsMinusOne() throws IOException {
    try (ReadOnlySeekableFileChannel channel =
        new ReadOnlySeekableFileChannel(new BytesChannel(new byte[0]))) {
      ByteBuffer[] dsts = {ByteBuffer.allocate(4), ByteBuffer.allocate(4)};
      long result = channel.read(dsts, 0, 2);
      assertEquals(-1L, result);
    }
  }
}
