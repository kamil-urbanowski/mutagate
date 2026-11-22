package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class ReadOnlySeekableFileChannelMutaGate8Test {

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
  void scatteringReadReturnsTotalBytesRead() throws IOException {
    byte[] data = "abcdefgh".getBytes(StandardCharsets.US_ASCII);
    ReadOnlySeekableFileChannel channel = new ReadOnlySeekableFileChannel(new BytesChannel(data));

    ByteBuffer[] dsts = {ByteBuffer.allocate(3), ByteBuffer.allocate(5)};
    long total = channel.read(dsts, 0, 2);

    assertEquals(8L, total);
    assertEquals("abc", new String(dsts[0].array(), StandardCharsets.US_ASCII));
    assertEquals("defgh", new String(dsts[1].array(), StandardCharsets.US_ASCII));
  }
}
