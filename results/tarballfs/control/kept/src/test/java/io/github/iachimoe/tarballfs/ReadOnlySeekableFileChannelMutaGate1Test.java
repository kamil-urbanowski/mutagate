package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class ReadOnlySeekableFileChannelMutaGate1Test {

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

  private static final class StuckTarget implements WritableByteChannel {
    @Override
    public int write(ByteBuffer src) {
      return 0;
    }

    @Override
    public boolean isOpen() {
      return true;
    }

    @Override
    public void close() {}
  }

  @Test
  void transferToCopiesRangesAcrossChunksWithoutMovingPosition() throws IOException {
    byte[] data = new byte[20000];
    for (int i = 0; i < data.length; i++) {
      data[i] = (byte) ((i * 31) % 251);
    }
    ReadOnlySeekableFileChannel channel = new ReadOnlySeekableFileChannel(new MemoryChannel(data));
    channel.position(5);

    ByteArrayOutputStream out1 = new ByteArrayOutputStream();
    long t1 = channel.transferTo(100, 10000, Channels.newChannel(out1));
    assertEquals(10000, t1);
    assertArrayEquals(Arrays.copyOfRange(data, 100, 10100), out1.toByteArray());
    assertEquals(5, channel.position());

    ByteArrayOutputStream out2 = new ByteArrayOutputStream();
    long t2 = channel.transferTo(19000, 5000, Channels.newChannel(out2));
    assertEquals(1000, t2);
    assertArrayEquals(Arrays.copyOfRange(data, 19000, 20000), out2.toByteArray());
    assertEquals(5, channel.position());

    ByteArrayOutputStream out3 = new ByteArrayOutputStream();
    assertEquals(0, channel.transferTo(20000, 10, Channels.newChannel(out3)));
    assertEquals(0, out3.size());

    ByteArrayOutputStream out4 = new ByteArrayOutputStream();
    assertEquals(0, channel.transferTo(0, 0, Channels.newChannel(out4)));
    assertEquals(0, out4.size());

    assertThrows(IOException.class, () -> channel.transferTo(0, 10, new StuckTarget()));
  }
}
