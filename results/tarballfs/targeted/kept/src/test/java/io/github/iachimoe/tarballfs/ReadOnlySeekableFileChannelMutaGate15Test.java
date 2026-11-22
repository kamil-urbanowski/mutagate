package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import org.junit.jupiter.api.Test;

class ReadOnlySeekableFileChannelMutaGate15Test {

  private static final class StallingChannel implements SeekableByteChannel {
    private int calls = 0;

    @Override
    public int read(ByteBuffer dst) {
      calls++;
      if (calls == 1) {
        dst.put((byte) 1);
        dst.put((byte) 2);
        dst.put((byte) 3);
        return 3;
      }
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
      return 100;
    }

    @Override
    public SeekableByteChannel truncate(long size) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean isOpen() {
      return true;
    }

    @Override
    public void close() {
    }
  }

  @Test
  void scatteringReadReturnsBytesReadWhenDelegateStalls() throws IOException {
    ReadOnlySeekableFileChannel channel = new ReadOnlySeekableFileChannel(new StallingChannel());
    ByteBuffer[] dsts = new ByteBuffer[] {ByteBuffer.allocate(5), ByteBuffer.allocate(5)};

    long result = channel.read(dsts, 0, 2);

    assertEquals(3L, result);
    assertEquals(3, dsts[0].position());
    assertEquals(0, dsts[1].position());
  }
}
