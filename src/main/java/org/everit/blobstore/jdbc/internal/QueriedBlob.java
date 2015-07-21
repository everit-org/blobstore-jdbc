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

import java.sql.Blob;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Holder class of a blob and its version that was queried from the database.
 *
 */
public class QueriedBlob {

  public final Blob blob;

  public final long blobId;

  protected final Statement statement;

  public final long version;

  public QueriedBlob(final long blobId, final Blob blob, final long version,
      final Statement statement) {
    this.blobId = blobId;
    this.blob = blob;
    this.version = version;
    this.statement = statement;
  }

  /**
   * Closes the statement that queried this blob.
   */
  public void closeStatement() {
    try {
      statement.close();
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      throw new RuntimeException(e);
    }
  }

}
