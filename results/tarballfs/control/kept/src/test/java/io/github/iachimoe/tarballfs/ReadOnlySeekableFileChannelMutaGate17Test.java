package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileLock;
import java.nio.channels.SeekableByteChannel;
import org.junit.jupiter.api.Test;

class ReadOnlySeekableFileChannelMutaGate17Test {

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
  void tryLockReturnsVirtualLockWithGivenParametersAndTracksValidity() throws IOException {
    ReadOnlySeekableFileChannel channel = new ReadOnlySeekableFileChannel(new StubChannel());

    FileLock shared = channel.tryLock(5L, 100L, true);
    assertNotNull(shared);
    assertSame(channel, shared.channel());
    assertSame(channel, shared.acquiredBy());
    assertEquals(5L, shared.position());
    assertEquals(100L, shared.size());
    assertTrue(shared.isShared());
    assertTrue(shared.isValid());

    FileLock exclusive = channel.tryLock(0L, 10L, false);
    assertNotNull(exclusive);
    assertNotSame(shared, exclusive);
    assertFalse(exclusive.isShared());
    assertEquals(0L, exclusive.position());
    assertEquals(10L, exclusive.size());
    assertTrue(exclusive.isValid());

    exclusive.release();
    assertFalse(exclusive.isValid());
    assertTrue(shared.isValid());

    channel.close();
    assertFalse(shared.isValid());
  }
}
