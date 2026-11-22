package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertNull;

import java.lang.reflect.Method;
import org.apache.commons.compress.compressors.gzip.GzipParameters;
import org.junit.jupiter.api.Test;

final class GzipFileSystemProviderMutaGate10Test {
  @Test
  void gzipHeaderPreferredName_nullMetadata_returnsNull() throws Exception {
    Method method =
        GzipFileSystemProvider.class.getDeclaredMethod("gzipHeaderPreferredName", GzipParameters.class);
    method.setAccessible(true);

    Object result = method.invoke(null, new Object[] {null});

    assertNull(result);
  }
}
