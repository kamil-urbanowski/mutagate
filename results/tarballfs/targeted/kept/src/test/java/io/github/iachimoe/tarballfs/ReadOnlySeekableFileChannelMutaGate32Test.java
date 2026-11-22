package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileLock;
import java.nio.channels.SeekableByteChannel;
import org.junit.jupiter.api.Test;

class ReadOnlySeekableFileChannelMutaGate32Test {

  private static final class StubChannel implements SeekableByteChannel {
    private boolean open = true;

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
    public void close() {
      open = false;
    }
  }

  @Test
  void lockValidityReflectsReleaseAndChannelClose() throws IOException {
    ReadOnlySeekableFileChannel channel = new ReadOnlySeekableFileChannel(new StubChannel());

    FileLock released = channel.tryLock(0, 10, true);
    assertTrue(released.isValid());
    released.release();
    assertFalse(released.isValid());

    FileLock closedWithChannel = channel.lock(0, 10, false);
    assertTrue(closedWithChannel.isValid());
    channel.close();
    assertFalse(closedWithChannel.isValid());
  }
}
