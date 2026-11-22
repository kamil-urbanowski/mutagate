package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;
import org.junit.jupiter.api.Test;

class ReadOnlySeekableFileChannelMutaGate24Test {

  private static final class InMemoryChannel implements SeekableByteChannel {
    private final byte[] data;
    private long pos;
    private boolean open = true;

    InMemoryChannel(byte[] data) {
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
    public void close() throws IOException {
      open = false;
    }
  }

  @Test
  void transferToCopiesRequestedBytes() throws IOException {
    byte[] data = new byte[10];
    for (int i = 0; i < data.length; i++) {
      data[i] = (byte) i;
    }
    try (ReadOnlySeekableFileChannel channel =
        new ReadOnlySeekableFileChannel(new InMemoryChannel(data))) {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      WritableByteChannel target = Channels.newChannel(out);

      long transferred = channel.transferTo(2, 5, target);

      assertEquals(5, transferred);
      assertArrayEquals(new byte[] {2, 3, 4, 5, 6}, out.toByteArray());
    }
  }
}
