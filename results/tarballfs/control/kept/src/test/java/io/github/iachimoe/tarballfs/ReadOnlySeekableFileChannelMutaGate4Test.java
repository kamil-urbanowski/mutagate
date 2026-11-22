package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import org.junit.jupiter.api.Test;

class ReadOnlySeekableFileChannelMutaGate4Test {

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
      this.pos = newPosition;
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
  void positionMovesDelegateAndReturnsSameChannel() throws IOException {
    MemoryChannel delegate = new MemoryChannel(new byte[] {10, 11, 12, 13, 14, 15});
    ReadOnlySeekableFileChannel channel = new ReadOnlySeekableFileChannel(delegate);

    FileChannel result = channel.position(3);

    assertSame(channel, result);
    assertEquals(3L, delegate.position());
    assertEquals(3L, channel.position());

    ByteBuffer buf = ByteBuffer.allocate(1);
    assertEquals(1, channel.read(buf));
    assertEquals(13, buf.get(0));
    assertEquals(4L, channel.position());

    FileChannel chained = channel.position(1).position(5);
    assertSame(channel, chained);
    assertEquals(5L, delegate.position());

    ByteBuffer buf2 = ByteBuffer.allocate(1);
    assertEquals(1, channel.read(buf2));
    assertEquals(15, buf2.get(0));
  }
}
