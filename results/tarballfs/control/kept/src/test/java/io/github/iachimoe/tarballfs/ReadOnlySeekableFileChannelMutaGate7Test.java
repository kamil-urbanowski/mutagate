package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.SeekableByteChannel;
import org.junit.jupiter.api.Test;

class ReadOnlySeekableFileChannelMutaGate7Test {

  private static final class StubChannel implements SeekableByteChannel {
    boolean open = true;
    long pos = 3;
    int writeCalls = 0;

    @Override
    public int read(ByteBuffer dst) {
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
  void writeThrowsAndLeavesBufferAndDelegateUntouched() throws IOException {
    StubChannel stub = new StubChannel();
    ReadOnlySeekableFileChannel channel = new ReadOnlySeekableFileChannel(stub);
    ByteBuffer src = ByteBuffer.wrap(new byte[] {1, 2, 3, 4});

    assertThrows(NonWritableChannelException.class, () -> channel.write(src));

    assertEquals(0, src.position());
    assertEquals(4, src.remaining());
    assertEquals(0, stub.writeCalls);
    assertEquals(3, stub.pos);
    assertEquals(3, channel.position());
    assertEquals(10, channel.size());
    assertEquals(true, channel.isOpen());
  }
}
