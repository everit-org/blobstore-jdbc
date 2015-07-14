package org.everit.blobstore.jdbc.internal;

import java.sql.Blob;
import java.sql.Connection;

import org.everit.blobstore.api.BlobAccessor;

public class JdbcBlobAccessor extends JdbcBlobReader implements BlobAccessor {

  public JdbcBlobAccessor(final long blobId, final long version, final Connection connection,
      final Blob blob) {
    super(blobId, version, connection, blob);
  }

  @Override
  public void close() {
    // TODO update blob record with new version
    super.close();
  }

  @Override
  public long newVersion() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void truncate(final long newLength) {
    // TODO Auto-generated method stub

  }

  @Override
  public void write(final byte[] b, final int off, final int len) {
    // TODO Auto-generated method stub

  }

}
