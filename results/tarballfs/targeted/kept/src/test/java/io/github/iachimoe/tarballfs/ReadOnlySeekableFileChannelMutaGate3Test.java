package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import org.junit.jupiter.api.Test;

class ReadOnlySeekableFileChannelMutaGate3Test {

  private static final class FakeChannel implements SeekableByteChannel {
    private long pos;
    private boolean open = true;

    @Override
    public int read(ByteBuffer dst) {
      return -1;
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
      return 100;
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
  void positionReturnsDelegatePosition() throws IOException {
    FakeChannel fake = new FakeChannel();
    ReadOnlySeekableFileChannel channel = new ReadOnlySeekableFileChannel(fake);
    assertEquals(0L, channel.position());
    channel.position(42L);
    assertEquals(42L, channel.position());
    fake.position(7L);
    assertEquals(7L, channel.position());
  }
}
