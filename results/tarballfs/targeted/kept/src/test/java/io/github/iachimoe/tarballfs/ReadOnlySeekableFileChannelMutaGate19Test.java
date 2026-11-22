package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class ReadOnlySeekableFileChannelMutaGate19Test {

  private static final class ZeroFirstChannel implements SeekableByteChannel {
    private final byte[] data;
    private long pos;
    private int readCalls;
    private boolean open = true;

    ZeroFirstChannel(byte[] data) {
      this.data = data;
    }

    @Override
    public int read(ByteBuffer dst) {
      if (readCalls++ == 0) {
        return 0;
      }
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
  void transferToContinuesAfterZeroByteRead() throws IOException {
    byte[] data = "hello".getBytes(StandardCharsets.US_ASCII);
    ReadOnlySeekableFileChannel channel =
        new ReadOnlySeekableFileChannel(new ZeroFirstChannel(data));
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    WritableByteChannel target = Channels.newChannel(out);

    long transferred = channel.transferTo(0, 5, target);

    assertEquals(5L, transferred);
    assertEquals("hello", new String(out.toByteArray(), StandardCharsets.US_ASCII));
  }
}
