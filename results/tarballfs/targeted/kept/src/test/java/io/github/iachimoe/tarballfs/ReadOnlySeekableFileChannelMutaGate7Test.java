package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class ReadOnlySeekableFileChannelMutaGate7Test {

  private static final class InMemoryChannel implements SeekableByteChannel {
    private final byte[] data;
    private int pos;
    private boolean open = true;

    InMemoryChannel(byte[] data) {
      this.data = data;
    }

    @Override
    public int read(ByteBuffer dst) {
      if (pos >= data.length) {
        return -1;
      }
      int n = Math.min(dst.remaining(), data.length - pos);
      dst.put(data, pos, n);
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
      pos = (int) newPosition;
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
  void scatteringReadWithNonZeroOffsetFillsOnlySelectedBuffers() throws IOException {
    byte[] data = "abcdef".getBytes(StandardCharsets.US_ASCII);
    try (ReadOnlySeekableFileChannel channel =
        new ReadOnlySeekableFileChannel(new InMemoryChannel(data))) {
      ByteBuffer first = ByteBuffer.allocate(3);
      ByteBuffer second = ByteBuffer.allocate(3);

      long read = channel.read(new ByteBuffer[] {first, second}, 1, 1);

      assertEquals(3L, read);
      assertEquals(0, first.position());
      assertEquals(3, second.position());
      assertEquals("abc", new String(second.array(), StandardCharsets.US_ASCII));
    }
  }
}
