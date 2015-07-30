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

import org.everit.blobstore.api.BlobReader;

/**
 * JDBC based {@link BlobReader} implementation that uses {@link java.sql.Blob#getBytes(long, int)}
 * function to get the content.
 *
 */
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
        executeAfterBlobStatementClosedButBeforeConnectionClose();
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
  protected void executeAfterBlobStatementClosedButBeforeConnectionClose() throws SQLException {
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

    // Pre check is necessary as MySQL JDBC driver cannot handle if length is greater than the
    // remaining size of the blob.
    long size = size();
    int validLen = len;
    if (size < position + len) {
      validLen = (int) (size - position);
    }
    if (validLen == 0) {
      // Necessary because bug in Mysql jdbc driver
      return -1;
    } else if (validLen < 0) {
      throw new IndexOutOfBoundsException();
    }

    int readByteNum = connectedBlob.blobChannel.read(position, b, off, len);
    position += readByteNum;
    return readByteNum;

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
      return connectedBlob.blobChannel.getBlob().length();
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
