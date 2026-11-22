package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import org.junit.jupiter.api.Test;

class ReadOnlySeekableFileChannelMutaGate5Test {

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
  void scatteringReadOnlyFillsBuffersInRequestedRange() throws IOException {
    byte[] data = new byte[100];
    for (int i = 0; i < data.length; i++) {
      data[i] = (byte) (i + 1);
    }
    ReadOnlySeekableFileChannel channel = new ReadOnlySeekableFileChannel(new MemoryChannel(data));

    ByteBuffer[] dsts = {
      ByteBuffer.allocate(4), ByteBuffer.allocate(4), ByteBuffer.allocate(4)
    };

    long read = channel.read(dsts, 1, 1);

    assertEquals(4L, read);
    assertEquals(0, dsts[0].position());
    assertEquals(4, dsts[1].position());
    assertEquals(0, dsts[2].position());
    assertEquals(4L, channel.position());
    assertEquals(1, dsts[1].get(0));
    assertEquals(4, dsts[1].get(3));
  }
}
