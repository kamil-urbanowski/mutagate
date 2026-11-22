package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.SeekableByteChannel;
import org.junit.jupiter.api.Test;

class ReadOnlySeekableFileChannelMutaGate8Test {

  private static final class TrackingChannel implements SeekableByteChannel {
    private boolean open = true;
    private long pos = 3;
    int writeCalls = 0;
    int readCalls = 0;

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
      pos = newPosition;
      return this;
    }

    @Override
    public long size() {
      return 10;
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
  void gatheringWriteThrowsAndLeavesStateUntouched() throws IOException {
    TrackingChannel delegate = new TrackingChannel();
    ReadOnlySeekableFileChannel channel = new ReadOnlySeekableFileChannel(delegate);

    ByteBuffer first = ByteBuffer.wrap(new byte[] {1, 2, 3});
    ByteBuffer second = ByteBuffer.wrap(new byte[] {4, 5});
    ByteBuffer[] srcs = {first, second};

    assertThrows(NonWritableChannelException.class, () -> channel.write(srcs, 0, 2));
    assertThrows(NonWritableChannelException.class, () -> channel.write(srcs, 1, 1));
    assertThrows(NonWritableChannelException.class, () -> channel.write(new ByteBuffer[0], 0, 0));
    assertThrows(NonWritableChannelException.class, () -> channel.write(null, 0, 0));

    assertEquals(0, delegate.writeCalls);
    assertEquals(0, delegate.readCalls);
    assertEquals(3, first.remaining());
    assertEquals(2, second.remaining());
    assertEquals(0, first.position());
    assertEquals(0, second.position());
    assertEquals(3, channel.position());
    assertEquals(10, channel.size());
  }
}
