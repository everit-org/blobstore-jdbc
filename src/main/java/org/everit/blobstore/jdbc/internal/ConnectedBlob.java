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

import java.sql.SQLException;
import java.sql.Statement;

/**
 * Holder class of a blob and its version that was queried from the database.
 *
 */
public class ConnectedBlob {

  public final BlobChannel blobChannel;

  public final long blobId;

  protected final Statement statement;

  public final long version;

  public ConnectedBlob(final long blobId, final BlobChannel blobChannel, final long version,
      final Statement statement) {
    this.blobId = blobId;
    this.blobChannel = blobChannel;
    this.version = version;
    this.statement = statement;
  }

  /**
   * Closes the {@link BlobChannel} and the statement that queried this blob.
   */
  public void close() {
    Throwable thrownException = null;
    try {
      blobChannel.close();
    } catch (RuntimeException | Error e) {
      thrownException = e;
    }

    try {
      statement.close();
    } catch (SQLException e) {
      if (thrownException == null) {
        thrownException = e;
      } else {
        thrownException.addSuppressed(e);
      }
    }

    if (thrownException != null) {
      // TODO
      throw new RuntimeException(thrownException);
    }
  }

}
