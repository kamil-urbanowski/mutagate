package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.SeekableByteChannel;
import org.junit.jupiter.api.Test;

class ReadOnlySeekableFileChannelMutaGate9Test {

  private static final class FakeChannel implements SeekableByteChannel {
    long pos = 3;
    boolean open = true;
    int writeCalls = 0;
    int readCalls = 0;
    int positionSets = 0;

    @Override
    public int read(ByteBuffer dst) {
      readCalls++;
      return -1;
    }

    @Override
    public int write(ByteBuffer src) {
      writeCalls++;
      return 0;
    }

    @Override
    public long position() {
      return pos;
    }

    @Override
    public SeekableByteChannel position(long newPosition) {
      positionSets++;
      pos = newPosition;
      return this;
    }

    @Override
    public long size() {
      return 100;
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
  void positionalWriteThrowsAndLeavesStateUntouched() throws IOException {
    FakeChannel fake = new FakeChannel();
    ReadOnlySeekableFileChannel channel = new ReadOnlySeekableFileChannel(fake);
    ByteBuffer src = ByteBuffer.wrap(new byte[] {1, 2, 3, 4});

    assertThrows(NonWritableChannelException.class, () -> channel.write(src, 10L));
    assertThrows(NonWritableChannelException.class, () -> channel.write(src, 0L));
    assertThrows(NonWritableChannelException.class, () -> channel.write(src, -1L));

    assertEquals(4, src.remaining());
    assertEquals(0, src.position());
    assertEquals(0, fake.writeCalls);
    assertEquals(0, fake.readCalls);
    assertEquals(0, fake.positionSets);
    assertEquals(3, channel.position());
    assertEquals(100, channel.size());
    assertEquals(true, channel.isOpen());
  }
}
