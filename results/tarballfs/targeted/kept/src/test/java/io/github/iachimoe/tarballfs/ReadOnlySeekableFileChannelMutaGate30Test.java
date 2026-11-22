package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.SeekableByteChannel;
import org.junit.jupiter.api.Test;

class ReadOnlySeekableFileChannelMutaGate30Test {

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
    FileChannel channel = new ReadOnlySeekableFileChannel(new StubChannel());

    FileLock lock = channel.tryLock(0, 10, true);
    assertTrue(lock.isValid());
    lock.release();
    assertFalse(lock.isValid());

    FileLock second = channel.lock(0, 10, false);
    assertTrue(second.isValid());
    channel.close();
    assertFalse(second.isValid());
  }
}
