package org.everit.blobstore.jdbc.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

public class EmptyReadOnlyBlob implements Blob {

  @Override
  public void free() throws SQLException {
  }

  @Override
  public InputStream getBinaryStream() throws SQLException {
    return getBinaryStream(1, 0);
  }

  @Override
  public InputStream getBinaryStream(final long pos, final long length) throws SQLException {
    if (pos != 1) {
      throw new SQLException("Empty blob accepts only 1 as start position in getBinaryStream");
    }
    if (length < 0) {
      throw new SQLException("Length must be at least zero");
    }
    return new InputStream() {

      @Override
      public int read() throws IOException {
        return -1;
      }
    };
  }

  @Override
  public byte[] getBytes(final long pos, final int length) throws SQLException {
    if (pos != 1) {
      throw new SQLException("Empty blob accepts only 1 as start position in getBytes");
    }
    if (length < 0) {
      throw new SQLException("Length must be at least zero");
    }
    return new byte[0];
  }

  @Override
  public long length() throws SQLException {
    return 0;
  }

  @Override
  public long position(final Blob pattern, final long start) throws SQLException {
    return -1;
  }

  @Override
  public long position(final byte[] pattern, final long start) throws SQLException {
    return -1;
  }

  @Override
  public OutputStream setBinaryStream(final long pos) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public int setBytes(final long pos, final byte[] bytes) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public int setBytes(final long pos, final byte[] bytes, final int offset, final int len)
      throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void truncate(final long len) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

}
