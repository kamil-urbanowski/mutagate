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

class ReadOnlySeekableFileChannelMutaGate27Test {

  private static final class MemoryChannel implements SeekableByteChannel {
    private final byte[] data;
    private long position;
    private boolean open = true;

    MemoryChannel(byte[] data) {
      this.data = data;
    }

    @Override
    public int read(ByteBuffer dst) {
      if (position >= data.length) {
        return -1;
      }
      int n = (int) Math.min(dst.remaining(), data.length - position);
      dst.put(data, (int) position, n);
      position += n;
      return n;
    }

    @Override
    public int write(ByteBuffer src) {
      throw new UnsupportedOperationException();
    }

    @Override
    public long position() {
      return position;
    }

    @Override
    public SeekableByteChannel position(long newPosition) {
      position = newPosition;
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
  void transferToWritesBytesToTargetWhenTargetAcceptsData() throws IOException {
    byte[] data = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
    ReadOnlySeekableFileChannel channel = new ReadOnlySeekableFileChannel(new MemoryChannel(data));
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    WritableByteChannel target = Channels.newChannel(out);

    long transferred = channel.transferTo(2, 5, target);

    assertEquals(5L, transferred);
    assertArrayEquals(new byte[] {2, 3, 4, 5, 6}, out.toByteArray());
  }
}
