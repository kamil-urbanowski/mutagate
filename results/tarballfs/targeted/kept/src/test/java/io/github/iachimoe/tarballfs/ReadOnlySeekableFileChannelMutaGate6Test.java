package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import org.junit.jupiter.api.Test;

class ReadOnlySeekableFileChannelMutaGate6Test {

  private static final class ZeroReadChannel implements SeekableByteChannel {
    private boolean open = true;

    @Override
    public int read(ByteBuffer dst) {
      return 0;
    }

    @Override
    public int write(ByteBuffer src) {
      throw new UnsupportedOperationException();
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
      return 10;
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
    public void close() {
      open = false;
    }
  }

  @Test
  void scatteringReadReturnsZeroWhenDelegateReadsZeroBytesAndNothingTransferred() throws IOException {
    try (ReadOnlySeekableFileChannel channel = new ReadOnlySeekableFileChannel(new ZeroReadChannel())) {
      ByteBuffer[] dsts = new ByteBuffer[] {ByteBuffer.allocate(4), ByteBuffer.allocate(4)};
      long result = channel.read(dsts, 0, 2);
      assertEquals(0L, result);
    }
  }
}
