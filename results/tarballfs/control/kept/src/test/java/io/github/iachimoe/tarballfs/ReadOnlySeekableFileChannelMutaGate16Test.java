package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileLock;
import java.nio.channels.SeekableByteChannel;
import org.junit.jupiter.api.Test;

class ReadOnlySeekableFileChannelMutaGate16Test {

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
  void lockReturnsValidLockReflectingArgumentsAndInvalidatedOnReleaseOrClose() throws IOException {
    ReadOnlySeekableFileChannel channel = new ReadOnlySeekableFileChannel(new StubChannel());

    FileLock lock = channel.lock(5L, 77L, true);
    assertSame(channel, lock.channel());
    assertSame(channel, lock.acquiredBy());
    assertEquals(5L, lock.position());
    assertEquals(77L, lock.size());
    assertTrue(lock.isShared());
    assertTrue(lock.isValid());

    lock.release();
    assertFalse(lock.isValid());

    FileLock exclusive = channel.lock(0L, 10L, false);
    assertFalse(exclusive.isShared());
    assertEquals(0L, exclusive.position());
    assertEquals(10L, exclusive.size());
    assertTrue(exclusive.isValid());

    channel.close();
    assertFalse(exclusive.isValid());
  }
}
