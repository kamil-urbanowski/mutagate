package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class ReadOnlySeekableFileChannelMutaGate10Test {

  private static final class BytesChannel implements SeekableByteChannel {
    private final byte[] data;
    private int pos;
    private boolean open = true;

    BytesChannel(byte[] data) {
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
  void scatteringReadFillsAllBuffers() throws IOException {
    byte[] data = "abcdefgh".getBytes(StandardCharsets.US_ASCII);
    try (ReadOnlySeekableFileChannel channel =
        new ReadOnlySeekableFileChannel(new BytesChannel(data))) {
      ByteBuffer first = ByteBuffer.allocate(3);
      ByteBuffer second = ByteBuffer.allocate(5);

      long read = channel.read(new ByteBuffer[] {first, second}, 0, 2);

      assertEquals(8L, read);
      assertEquals("abc", new String(first.array(), StandardCharsets.US_ASCII));
      assertEquals("defgh", new String(second.array(), StandardCharsets.US_ASCII));
      assertEquals(8L, channel.position());
    }
  }
}
