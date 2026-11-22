package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import org.junit.jupiter.api.Test;

public class ReadOnlySeekableFileChannelMutaGate15Test {

  private static final class StubChannel implements SeekableByteChannel {
    private boolean open = true;
    private long pos = 0;

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
  void mapThrowsUnsupportedForAllModesWithMessageAndKeepsChannelUsable() throws IOException {
    StubChannel stub = new StubChannel();
    try (ReadOnlySeekableFileChannel channel = new ReadOnlySeekableFileChannel(stub)) {
      stub.position(7);

      UnsupportedOperationException ex1 =
          assertThrows(
              UnsupportedOperationException.class,
              () -> channel.map(FileChannel.MapMode.READ_ONLY, 0, 10));
      assertEquals("Memory mapping is not supported for in-memory archive channels", ex1.getMessage());

      UnsupportedOperationException ex2 =
          assertThrows(
              UnsupportedOperationException.class,
              () -> channel.map(FileChannel.MapMode.READ_WRITE, 0, 10));
      assertEquals("Memory mapping is not supported for in-memory archive channels", ex2.getMessage());

      assertThrows(
          UnsupportedOperationException.class,
          () -> channel.map(FileChannel.MapMode.PRIVATE, 5, 0));

      // Channel state is unaffected by failed map calls.
      assertTrue(channel.isOpen());
      assertEquals(7, channel.position());
      assertEquals(100, channel.size());
    }
    assertTrue(!stub.isOpen());
  }
}
