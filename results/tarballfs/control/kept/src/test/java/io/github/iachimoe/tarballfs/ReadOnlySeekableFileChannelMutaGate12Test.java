package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.SeekableByteChannel;
import org.junit.jupiter.api.Test;

class ReadOnlySeekableFileChannelMutaGate12Test {

  private static final class StubChannel implements SeekableByteChannel {
    private boolean open = true;
    private long position = 5;
    private final long size = 100;
    int calls = 0;

    @Override
    public int read(ByteBuffer dst) {
      calls++;
      return -1;
    }

    @Override
    public int write(ByteBuffer src) {
      calls++;
      return 0;
    }

    @Override
    public long position() {
      return position;
    }

    @Override
    public SeekableByteChannel position(long newPosition) {
      calls++;
      position = newPosition;
      return this;
    }

    @Override
    public long size() {
      return size;
    }

    @Override
    public SeekableByteChannel truncate(long size) {
      calls++;
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
  void truncateThrowsAndLeavesDelegateUntouched() throws IOException {
    StubChannel stub = new StubChannel();
    ReadOnlySeekableFileChannel channel = new ReadOnlySeekableFileChannel(stub);

    assertThrows(NonWritableChannelException.class, () -> channel.truncate(0));
    assertThrows(NonWritableChannelException.class, () -> channel.truncate(50));
    assertThrows(NonWritableChannelException.class, () -> channel.truncate(1000));
    assertThrows(NonWritableChannelException.class, () -> channel.truncate(-1));

    assertEquals(0, stub.calls);
    assertEquals(100, channel.size());
    assertEquals(5, channel.position());
    assertTrue(channel.isOpen());
  }
}
