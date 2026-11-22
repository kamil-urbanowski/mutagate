package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import org.junit.jupiter.api.Test;

class ReadOnlySeekableFileChannelMutaGate1Test {

  private static final class TrackingChannel implements SeekableByteChannel {
    private boolean open = true;
    private int closeCalls = 0;

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
      closeCalls++;
      open = false;
    }
  }

  @Test
  void closingChannelClosesDelegate() throws IOException {
    TrackingChannel delegate = new TrackingChannel();
    ReadOnlySeekableFileChannel channel = new ReadOnlySeekableFileChannel(delegate);

    assertTrue(delegate.isOpen());
    channel.close();

    assertFalse(channel.isOpen());
    assertFalse(delegate.isOpen());
    assertTrue(delegate.closeCalls == 1);
  }
}
