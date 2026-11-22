package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class ReadOnlySeekableFileChannelMutaGate3Test {

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
    public void close() throws IOException {
      open = false;
    }
  }

  @Test
  void positionalReadReadsAtGivenPositionAndRestoresPosition() throws IOException {
    byte[] data = "0123456789".getBytes(StandardCharsets.US_ASCII);
    ReadOnlySeekableFileChannel channel = new ReadOnlySeekableFileChannel(new MemoryChannel(data));
    channel.position(2);

    ByteBuffer buf = ByteBuffer.allocate(3);
    int n = channel.read(buf, 5);

    assertEquals(3, n);
    assertEquals("567", new String(buf.array(), StandardCharsets.US_ASCII));
    assertEquals(2, channel.position());

    // Reading at/after end returns -1 and still restores position
    ByteBuffer eof = ByteBuffer.allocate(4);
    assertEquals(-1, channel.read(eof, 10));
    assertEquals(2, channel.position());

    // Subsequent sequential read continues from the restored position
    ByteBuffer next = ByteBuffer.allocate(2);
    assertEquals(2, channel.read(next));
    assertEquals("23", new String(next.array(), StandardCharsets.US_ASCII));
  }
}
