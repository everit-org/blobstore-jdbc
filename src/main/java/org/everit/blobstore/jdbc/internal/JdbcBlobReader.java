package org.everit.blobstore.jdbc.internal;

import java.sql.Blob;
import java.sql.Connection;
import java.sql.SQLException;

import org.everit.blobstore.api.BlobReader;

public class JdbcBlobReader implements BlobReader {

  protected final Blob blob;

  private final long blobId;

  protected final Connection connection;

  private final long version;

  public JdbcBlobReader(final long blobId, final long version, final Connection connection,
      final Blob blob) {
    this.blobId = blobId;
    this.version = version;
    this.blob = blob;
    this.connection = connection;
  }

  @Override
  public void close() {
    try {
      connection.close();
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      throw new RuntimeException(e);
    }
  }

  @Override
  public long getBlobId() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public long position() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int read(final byte[] b, final int off, final int len) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void seek(final long pos) {
    // TODO Auto-generated method stub

  }

  @Override
  public long size() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public long version() {
    // TODO Auto-generated method stub
    return 0;
  }

}
