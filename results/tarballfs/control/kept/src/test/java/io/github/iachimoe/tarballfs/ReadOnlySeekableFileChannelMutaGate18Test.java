package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import org.junit.jupiter.api.Test;

class ReadOnlySeekableFileChannelMutaGate18Test {

  private static final class StubChannel implements SeekableByteChannel {
    long pos = 7;
    long sizeValue = 42;
    boolean open = true;
    boolean closed = false;

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
      return pos;
    }

    @Override
    public SeekableByteChannel position(long newPosition) {
      pos = newPosition;
      return this;
    }

    @Override
    public long size() {
      return sizeValue;
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
      closed = true;
    }
  }

  @Test
  void constructorStoresDelegateUsedForPositionSizeAndClose() throws IOException {
    StubChannel stub = new StubChannel();
    ReadOnlySeekableFileChannel channel = new ReadOnlySeekableFileChannel(stub);

    assertEquals(7L, channel.position());
    assertEquals(42L, channel.size());
    assertSame(channel, channel.position(13L));
    assertEquals(13L, stub.pos);
    assertTrue(channel.isOpen());

    channel.close();
    assertTrue(stub.closed);
    assertEquals(false, channel.isOpen());
  }
}
