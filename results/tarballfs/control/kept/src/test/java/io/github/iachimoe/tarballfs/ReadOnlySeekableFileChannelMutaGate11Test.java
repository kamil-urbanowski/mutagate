package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import org.junit.jupiter.api.Test;

class ReadOnlySeekableFileChannelMutaGate11Test {

  private static final class StubChannel implements SeekableByteChannel {
    long size;
    long position;
    int sizeCalls;
    boolean open = true;

    StubChannel(long size, long position) {
      this.size = size;
      this.position = position;
    }

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
      return position;
    }

    @Override
    public SeekableByteChannel position(long newPosition) {
      position = newPosition;
      return this;
    }

    @Override
    public long size() {
      sizeCalls++;
      return size;
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
  void sizeReflectsDelegateSizeLiveWithoutAffectingPosition() throws IOException {
    StubChannel stub = new StubChannel(123456789012L, 7);
    ReadOnlySeekableFileChannel channel = new ReadOnlySeekableFileChannel(stub);

    assertEquals(123456789012L, channel.size());
    assertEquals(1, stub.sizeCalls);
    assertEquals(7, channel.position());

    stub.size = 42;
    assertEquals(42, channel.size());
    assertEquals(2, stub.sizeCalls);

    stub.size = 0;
    assertEquals(0, channel.size());
    assertEquals(7, stub.position);
  }
}
