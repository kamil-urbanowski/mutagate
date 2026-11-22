package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.nio.channels.Channels;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import org.junit.jupiter.api.Test;

class ReadOnlySeekableFileChannelMutaGate26Test {

  @Test
  void transferToWritesBytesToTarget() throws Exception {
    byte[] data = new byte[20000];
    for (int i = 0; i < data.length; i++) {
      data[i] = (byte) (i % 251);
    }
    Path file = Files.createTempFile("mutagate26", ".bin");
    try {
      Files.write(file, data);
      try (SeekableByteChannel delegate = Files.newByteChannel(file, StandardOpenOption.READ);
          ReadOnlySeekableFileChannel channel = new ReadOnlySeekableFileChannel(delegate)) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        WritableByteChannel target = Channels.newChannel(out);

        long transferred = channel.transferTo(100, 15000, target);

        assertEquals(15000, transferred);
        byte[] expected = new byte[15000];
        System.arraycopy(data, 100, expected, 0, 15000);
        assertArrayEquals(expected, out.toByteArray());
      }
    } finally {
      Files.deleteIfExists(file);
    }
  }
}
