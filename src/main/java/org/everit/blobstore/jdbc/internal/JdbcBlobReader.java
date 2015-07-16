package org.everit.blobstore.jdbc.internal;

import java.sql.Connection;
import java.sql.SQLException;

import org.everit.blobstore.api.BlobReader;

public class JdbcBlobReader implements BlobReader {

  protected final ConnectedBlob connectedBlob;

  protected final Connection connection;

  protected long position = 0;

  public JdbcBlobReader(final ConnectedBlob connectedBlob, final Connection connection) {
    this.connectedBlob = connectedBlob;
    this.connection = connection;
  }

  @Override
  public void close() {
    Throwable thrownException = null;
    try {
      connectedBlob.close();
    } catch (RuntimeException | Error e) {
      thrownException = e;
    }

    if (thrownException == null) {
      try {
        executeAfterBlobFreedAndBeforeConnectionClose();
      } catch (SQLException | RuntimeException | Error e) {
        thrownException = e;
      }
    }

    try {
      connection.close();
    } catch (SQLException | RuntimeException | Error e) {
      if (thrownException != null) {
        thrownException.addSuppressed(e);
      } else {
        thrownException = e;
      }
    }
    if (thrownException != null) {
      // TODO
      throw new RuntimeException(thrownException);
    }
  }

  /**
   * The method is called after the blob is not connected to the database anymore but the database
   * connection is still opened. Further reads and writes can be done in this function.
   *
   * @throws SQLException
   *           if there is an issue during accessing the database.
   */
  protected void executeAfterBlobFreedAndBeforeConnectionClose() throws SQLException {
    // Do nothing here
  }

  @Override
  public long getBlobId() {
    return connectedBlob.blobId;
  }

  @Override
  public long position() {
    return position;
  }

  @Override
  public int read(final byte[] b, final int off, final int len) {
    if (b == null) {
      throw new NullPointerException();
    } else if (off < 0 || len < 0 || len > b.length - off) {
      throw new IndexOutOfBoundsException();
    } else if (len == 0) {
      return 0;
    }

    try {
      byte[] bytes = connectedBlob.blob.getBytes(position + 1, len);
      System.arraycopy(bytes, 0, b, off, bytes.length);
      return bytes.length;
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      throw new RuntimeException(e);
    }
  }

  @Override
  public void seek(final long pos) {
    if (pos < 0) {
      throw new IndexOutOfBoundsException("Position cannot be a negative number");
    }
    if (pos > size()) {
      throw new IndexOutOfBoundsException("Position is higher than the size of the blob");
    }
    this.position = pos;
  }

  @Override
  public long size() {
    try {
      return connectedBlob.blob.length();
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      throw new RuntimeException(e);
    }
  }

  @Override
  public long version() {
    return connectedBlob.version;
  }

}
