package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import org.junit.jupiter.api.Test;

class ReadOnlySeekableFileChannelMutaGate4Test {

  private static final class FakeChannel implements SeekableByteChannel {
    private final byte[] data;
    private long pos;
    private boolean open = true;

    FakeChannel(byte[] data) {
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
  void positionReturnsSameChannelAndSeeks() throws IOException {
    byte[] data = {10, 20, 30, 40, 50};
    try (ReadOnlySeekableFileChannel channel =
        new ReadOnlySeekableFileChannel(new FakeChannel(data))) {
      FileChannel result = channel.position(2);
      assertSame(channel, result);
      assertEquals(2L, result.position());

      ByteBuffer buf = ByteBuffer.allocate(1);
      result.read(buf);
      assertEquals(30, buf.get(0));
    }
  }
}
