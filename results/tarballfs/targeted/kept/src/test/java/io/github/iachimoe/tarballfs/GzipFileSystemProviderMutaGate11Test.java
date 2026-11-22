package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Method;

import org.apache.commons.compress.compressors.gzip.GzipParameters;
import org.junit.jupiter.api.Test;

final class GzipFileSystemProviderMutaGate11Test {

  private static final class SplitNameParameters extends GzipParameters {
    @Override
    @SuppressWarnings("deprecation")
    public String getFilename() {
      return null;
    }

    @Override
    public String getFileName() {
      return "fallback-name.txt";
    }
  }

  @Test
  void gzipHeaderPreferredName_usesSecondGetter_whenFirstIsNull() throws Exception {
    Method m =
        GzipFileSystemProvider.class.getDeclaredMethod("gzipHeaderPreferredName", GzipParameters.class);
    m.setAccessible(true);

    Object result = m.invoke(null, new SplitNameParameters());

    assertEquals("fallback-name.txt", result);
  }
}
