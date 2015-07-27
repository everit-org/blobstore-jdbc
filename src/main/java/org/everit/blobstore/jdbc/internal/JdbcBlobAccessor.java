/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.everit.blobstore.jdbc.internal;

import java.sql.Connection;
import java.sql.SQLException;

import org.everit.blobstore.api.BlobAccessor;
import org.everit.blobstore.jdbc.schema.qdsl.QBlobstoreBlob;

import com.querydsl.sql.Configuration;
import com.querydsl.sql.dml.SQLUpdateClause;

public class JdbcBlobAccessor extends JdbcBlobReader implements BlobAccessor {

  protected final long newVersion;

  protected final Configuration querydslConfiguration;

  protected final boolean updateBlobContentInUpdateSQLNecessary;

  public JdbcBlobAccessor(final QueriedBlob connectedBlob, final Connection connection,
      final Configuration querydslConfiguration,
      final boolean updateBlobContentInUpdateSQLNecessary, final boolean incrementVersion) {
    super(connectedBlob, connection);
    this.querydslConfiguration = querydslConfiguration;
    this.updateBlobContentInUpdateSQLNecessary = updateBlobContentInUpdateSQLNecessary;
    if (incrementVersion) {
      this.newVersion = connectedBlob.version + 1;
    } else {
      this.newVersion = connectedBlob.version;
    }
  }

  @Override
  public void close() {
    super.close();
  }

  @Override
  protected void executeAfterBlobStatementClosedButBeforeConnectionClose() throws SQLException {
    super.executeAfterBlobStatementClosedButBeforeConnectionClose();

    if (!updateBlobContentInUpdateSQLNecessary && version() == newVersion) {
      return;
    }

    QBlobstoreBlob qBlob = QBlobstoreBlob.blobstoreBlob;
    SQLUpdateClause updateClause = new SQLUpdateClause(connection, querydslConfiguration, qBlob)
        .set(qBlob.version_, newVersion())
        .where(qBlob.blobId.eq(connectedBlob.blobId));

    if (updateBlobContentInUpdateSQLNecessary) {
      updateClause.set(qBlob.blob_, connectedBlob.blob);
    }
    updateClause.execute();
  }

  @Override
  public long newVersion() {
    return newVersion;
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
