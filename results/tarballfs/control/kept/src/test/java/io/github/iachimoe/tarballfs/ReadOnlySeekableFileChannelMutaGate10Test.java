package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import org.junit.jupiter.api.Test;

class ReadOnlySeekableFileChannelMutaGate10Test {

  private static final class StubChannel implements SeekableByteChannel {
    private final byte[] data;
    private long pos;
    private boolean open = true;

    StubChannel(byte[] data) {
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
  void positionReflectsDelegateAcrossReadsSeeksAndPositionalReads() throws IOException {
    ReadOnlySeekableFileChannel channel =
        new ReadOnlySeekableFileChannel(new StubChannel(new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9}));

    assertEquals(0L, channel.position());

    channel.read(ByteBuffer.allocate(4));
    assertEquals(4L, channel.position());

    assertSame(channel, channel.position(7));
    assertEquals(7L, channel.position());

    // positional read must not disturb the channel position
    ByteBuffer buf = ByteBuffer.allocate(2);
    channel.read(buf, 1);
    assertEquals(7L, channel.position());

    // position beyond the size is passed through
    channel.position(25);
    assertEquals(25L, channel.position());
  }
}
