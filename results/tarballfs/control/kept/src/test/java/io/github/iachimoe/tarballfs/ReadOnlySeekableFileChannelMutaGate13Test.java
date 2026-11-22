package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import org.junit.jupiter.api.Test;

class ReadOnlySeekableFileChannelMutaGate13Test {

  private static final class CountingChannel implements SeekableByteChannel {
    private final byte[] data;
    private long pos;
    private boolean open = true;
    int interactions;

    CountingChannel(byte[] data) {
      this.data = data;
    }

    @Override
    public int read(ByteBuffer dst) {
      interactions++;
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
      interactions++;
      throw new UnsupportedOperationException();
    }

    @Override
    public long position() {
      interactions++;
      return pos;
    }

    @Override
    public SeekableByteChannel position(long newPosition) {
      interactions++;
      pos = newPosition;
      return this;
    }

    @Override
    public long size() {
      interactions++;
      return data.length;
    }

    @Override
    public SeekableByteChannel truncate(long size) {
      interactions++;
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
  void forceIsNoOpForBothArgumentsAndLeavesChannelUntouched() throws IOException {
    byte[] bytes = {1, 2, 3, 4, 5, 6};
    CountingChannel backing = new CountingChannel(bytes);
    ReadOnlySeekableFileChannel channel = new ReadOnlySeekableFileChannel(backing);

    ByteBuffer first = ByteBuffer.allocate(2);
    channel.read(first);
    assertEquals(2, channel.position());
    int interactionsBefore = backing.interactions;

    assertDoesNotThrow(() -> channel.force(true));
    assertDoesNotThrow(() -> channel.force(false));

    assertEquals(interactionsBefore, backing.interactions);
    assertTrue(channel.isOpen());
    assertTrue(backing.isOpen());
    assertEquals(2, channel.position());
    assertEquals(bytes.length, channel.size());

    ByteBuffer rest = ByteBuffer.allocate(4);
    assertEquals(4, channel.read(rest));
    assertArrayEquals(new byte[] {3, 4, 5, 6}, rest.array());

    channel.close();
  }
}
