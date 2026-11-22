package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class ReadOnlySeekableFileChannelMutaGate21Test {

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
      throw new NonWritableChannelException();
    }

    @Override
    public long position() {
      return pos;
    }

    @Override
    public SeekableByteChannel position(long newPosition) {
      if (newPosition < 0) {
        throw new IllegalArgumentException("negative position");
      }
      pos = newPosition;
      return this;
    }

    @Override
    public long size() {
      return data.length;
    }

    @Override
    public SeekableByteChannel truncate(long size) {
      throw new NonWritableChannelException();
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
  void transferToAcrossMultipleChunksCopiesCorrectBytes() throws IOException {
    byte[] data = new byte[30000];
    for (int i = 0; i < data.length; i++) {
      data[i] = (byte) (i % 251);
    }

    ReadOnlySeekableFileChannel channel = new ReadOnlySeekableFileChannel(new InMemoryChannel(data));
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    WritableByteChannel target = Channels.newChannel(out);

    long transferred = channel.transferTo(5, 20000, target);

    assertEquals(20000L, transferred);
    assertArrayEquals(Arrays.copyOfRange(data, 5, 20005), out.toByteArray());
  }
}
