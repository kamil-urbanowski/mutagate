package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import org.junit.jupiter.api.Test;

class ReadOnlySeekableFileChannelMutaGate5Test {

  private static final class CountingChannel implements SeekableByteChannel {
    int closeCount = 0;
    boolean open = true;

    @Override
    public int read(ByteBuffer dst) {
      return -1;
    }

    @Override
    public int write(ByteBuffer src) {
      return 0;
    }

    @Override
    public long position() {
      return 0;
    }

    @Override
    public SeekableByteChannel position(long newPosition) {
      return this;
    }

    @Override
    public long size() {
      return 0;
    }

    @Override
    public SeekableByteChannel truncate(long size) {
      return this;
    }

    @Override
    public boolean isOpen() {
      return open;
    }

    @Override
    public void close() throws IOException {
      closeCount++;
      open = false;
    }
  }

  @Test
  void closeClosesDelegateExactlyOnceEvenWhenClosedTwice() throws IOException {
    CountingChannel delegate = new CountingChannel();
    ReadOnlySeekableFileChannel channel = new ReadOnlySeekableFileChannel(delegate);

    assertTrue(channel.isOpen());
    assertTrue(delegate.isOpen());

    channel.close();

    assertFalse(channel.isOpen());
    assertFalse(delegate.isOpen());
    assertEquals(1, delegate.closeCount);

    channel.close();

    assertEquals(1, delegate.closeCount);
  }
}
