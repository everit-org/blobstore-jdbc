package org.everit.blobstore.jdbc.internal;

import java.sql.Connection;
import java.sql.SQLException;

import org.everit.blobstore.api.BlobAccessor;
import org.everit.blobstore.jdbc.schema.qdsl.QBlobstoreBlob;

import com.querydsl.sql.Configuration;
import com.querydsl.sql.dml.SQLUpdateClause;

public class JdbcBlobAccessor extends JdbcBlobReader implements BlobAccessor {

  protected final Configuration querydslConfiguration;

  public JdbcBlobAccessor(final ConnectedBlob connectedBlob, final Connection connection,
      final Configuration querydslConfiguration) {
    super(connectedBlob, connection);
    this.querydslConfiguration = querydslConfiguration;
  }

  @Override
  public void close() {
    super.close();
  }

  @Override
  protected void executeAfterBlobFreedAndBeforeConnectionClose() throws SQLException {
    super.executeAfterBlobFreedAndBeforeConnectionClose();

    QBlobstoreBlob qBlob = QBlobstoreBlob.blobstoreBlob;
    new SQLUpdateClause(connection, querydslConfiguration, qBlob)
        .set(qBlob.version_, newVersion())
        .where(qBlob.blobId.eq(connectedBlob.blobId));
  }

  @Override
  public long newVersion() {
    return connectedBlob.version + 1;
  }

  @Override
  public void truncate(final long newLength) {
    try {
      connectedBlob.blob.truncate(newLength);
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      throw new RuntimeException(e);
    }
  }

  @Override
  public void write(final byte[] b, final int off, final int len) {
    if (b == null) {
      throw new NullPointerException();
    } else if ((off < 0) || (off > b.length) || (len < 0) || ((off + len) > b.length)
        || ((off + len) < 0)) {
      throw new IndexOutOfBoundsException();
    } else if (len == 0) {
      return;
    }
    try {
      connectedBlob.blob.setBytes(position + 1, b, off, len);
      position += len;
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      throw new RuntimeException(e);
    }

  }

}
