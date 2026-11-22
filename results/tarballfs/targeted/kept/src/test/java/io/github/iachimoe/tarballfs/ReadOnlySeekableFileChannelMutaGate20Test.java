package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class ReadOnlySeekableFileChannelMutaGate20Test {

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
  void transferToStopsAtRequestedCountAcrossMultipleChunks() throws IOException {
    byte[] data = new byte[20000];
    for (int i = 0; i < data.length; i++) {
      data[i] = (byte) (i * 31 + 7);
    }
    ReadOnlySeekableFileChannel channel = new ReadOnlySeekableFileChannel(new MemoryChannel(data));

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    WritableByteChannel target = Channels.newChannel(out);

    long transferred = channel.transferTo(0, 10000, target);

    assertEquals(10000L, transferred);
    assertEquals(10000, out.size());
    assertArrayEquals(Arrays.copyOfRange(data, 0, 10000), out.toByteArray());
  }
}
