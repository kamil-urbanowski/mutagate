package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import org.junit.jupiter.api.Test;

class ReadOnlySeekableFileChannelMutaGate17Test {

  private static final class StubChannel implements SeekableByteChannel {
    private final long size;
    private long position;
    private boolean open = true;

    StubChannel(long size) {
      this.size = size;
    }

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
      return position;
    }

    @Override
    public SeekableByteChannel position(long newPosition) {
      this.position = newPosition;
      return this;
    }

    @Override
    public long size() {
      return size;
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
  void sizeReturnsDelegateSize() throws IOException {
    try (ReadOnlySeekableFileChannel channel = new ReadOnlySeekableFileChannel(new StubChannel(12345L))) {
      assertEquals(12345L, channel.size());
    }
  }
}
