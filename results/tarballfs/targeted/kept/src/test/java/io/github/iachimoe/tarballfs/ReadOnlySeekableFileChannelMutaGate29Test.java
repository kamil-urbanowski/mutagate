package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileLock;
import java.nio.channels.SeekableByteChannel;
import org.junit.jupiter.api.Test;

class ReadOnlySeekableFileChannelMutaGate29Test {

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
    public void close() throws IOException {
      open = false;
    }
  }

  @Test
  void tryLockReturnsValidLockDescribingRequest() throws IOException {
    ReadOnlySeekableFileChannel channel = new ReadOnlySeekableFileChannel(new StubChannel());
    FileLock lock = channel.tryLock(5L, 10L, true);

    assertNotNull(lock);
    assertSame(channel, lock.channel());
    assertEquals(5L, lock.position());
    assertEquals(10L, lock.size());
    assertTrue(lock.isShared());
    assertTrue(lock.isValid());

    lock.release();
    assertFalse(lock.isValid());
  }
}
