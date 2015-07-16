package org.everit.blobstore.jdbc.internal;

import java.io.Closeable;
import java.sql.Blob;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Holder class to get version and blob together from the database.
 *
 */
public class ConnectedBlob implements Closeable {

  public final Blob blob;

  public final long blobId;

  protected final Statement statement;

  public final long version;

  public ConnectedBlob(final long blobId, final Blob blob, final long version,
      final Statement statement) {
    this.blobId = blobId;
    this.blob = blob;
    this.version = version;
    this.statement = statement;
  }

  @Override
  public void close() {
    try {
      statement.close();
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      throw new RuntimeException(e);
    }
  }

}
