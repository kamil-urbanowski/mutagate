package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.SeekableByteChannel;
import org.junit.jupiter.api.Test;

class ReadOnlySeekableFileChannelMutaGate12Test {

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
      throw new NonWritableChannelException();
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
      throw new NonWritableChannelException();
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
  void scatteringReadReportsPartialTotalThenEndOfStream() throws IOException {
    ReadOnlySeekableFileChannel channel =
        new ReadOnlySeekableFileChannel(new BytesChannel(new byte[] {1, 2, 3, 4, 5}));

    ByteBuffer[] dsts = {ByteBuffer.allocate(3), ByteBuffer.allocate(4)};
    long first = channel.read(dsts, 0, 2);
    assertEquals(5L, first);

    ByteBuffer[] more = {ByteBuffer.allocate(2)};
    long second = channel.read(more, 0, 1);
    assertEquals(-1L, second);
  }
}
