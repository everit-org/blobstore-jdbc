package org.everit.blobstore.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.everit.blobstore.api.BlobAccessor;
import org.everit.blobstore.api.BlobReader;
import org.everit.blobstore.api.Blobstore;
import org.everit.blobstore.api.NoSuchBlobException;
import org.everit.blobstore.jdbc.internal.BlobAndVersion;
import org.everit.blobstore.jdbc.internal.InitialBlobBean;
import org.everit.blobstore.jdbc.internal.JdbcBlobAccessor;
import org.everit.blobstore.jdbc.schema.qdsl.QBlobstoreBlob;

import com.querydsl.core.Tuple;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.dml.SQLInsertClause;

/**
 * JDBC based implementation of Blobstore.
 *
 */
public class JdbcBlobstore implements Blobstore {

  protected final DataSource dataSource;

  protected final Configuration querydslConfiguration;

  public JdbcBlobstore(final DataSource dataSource, final Configuration configuration) {
    this.dataSource = dataSource;
    this.querydslConfiguration = configuration;
  }

  /**
   * This method is called when an {@link Error} or {@link RuntimeException} occured and the
   * connection should be closed.
   *
   * @param connection
   *          The database connection that should be closed.
   * @param e
   *          The {@link Error} or {@link RuntimeException} that occured.
   */
  protected void closeConnectionDueToThrowable(final Connection connection, final Throwable e) {
    try {
      connection.close();
    } catch (Throwable th) {
      e.addSuppressed(th);
    }
    if (e instanceof Error) {
      throw (Error) e;
    } else if (e instanceof RuntimeException) {
      throw (RuntimeException) e;
    }
  }

  @Override
  public BlobAccessor createBlob() {
    Connection connection;
    try {
      connection = dataSource.getConnection();
    } catch (SQLException e) {
      // TODO
      throw new RuntimeException(e);
    }

    QBlobstoreBlob qBlob = QBlobstoreBlob.blobstoreBlob;
    Long blobId;
    try {
      blobId = new SQLInsertClause(connection, querydslConfiguration, qBlob)
          .populate(new InitialBlobBean()).executeWithKey(qBlob.blobId);
    } catch (RuntimeException | Error e) {
      closeConnectionDueToThrowable(connection, e);
      throw new RuntimeException(e);
    }

    return updateBlob(blobId, connection);
  }

  @Override
  public void deleteBlob(final long blobId) {
    // TODO Auto-generated method stub

  }

  /**
   * Gets a blob with its current version from the database. If an exception or error occures, this
   * method closes the database connection.
   *
   * @param blobId
   *          The id of the blob.
   * @param forUpdate
   *          Whether a pessimistic lock should be applied on the blob or not.
   * @param connection
   *          The database connection.
   * @return The blob and its current version.
   */
  protected BlobAndVersion getBlobAndVersion(final long blobId, final boolean forUpdate,
      final Connection connection) {
    QBlobstoreBlob qBlob = QBlobstoreBlob.blobstoreBlob;
    try {
      SQLQuery<Tuple> query = new SQLQuery<>(connection, querydslConfiguration)
          .select(qBlob.version_, qBlob.data_)
          .from(qBlob)
          .where(qBlob.blobId.eq(blobId));
      if (forUpdate) {
        query.forUpdate();
      }
      Tuple tuple = query.fetchFirst();

      if (tuple == null) {
        throw new NoSuchBlobException(blobId);
      }

      return new BlobAndVersion(tuple.get(qBlob.data_), tuple.get(qBlob.version_));

    } catch (RuntimeException | Error e) {
      closeConnectionDueToThrowable(connection, e);
      throw new RuntimeException(e);
    }
  }

  @Override
  public BlobReader readBlob(final long blobId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public BlobReader readBlobForUpdate(final long blobId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public BlobAccessor updateBlob(final long blobId) {
    Connection connection;
    try {
      connection = dataSource.getConnection();
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      throw new RuntimeException(e);
    }

    return updateBlob(blobId, connection);
  }

  /**
   * Creates a new {@link BlobAccessor} or closes the connection if an {@link Exception} occurs.
   *
   * @param blobId
   *          The id of the Blob that the {@link BlobAccessor} will belong to.
   * @param connection
   *          The connection of the database.
   * @return An accessor to modify the blob content.
   */
  protected BlobAccessor updateBlob(final long blobId, final Connection connection) {
    BlobAndVersion blobAndVersion = getBlobAndVersion(blobId, true, connection);
    return new JdbcBlobAccessor(blobId, blobAndVersion.version, connection, blobAndVersion.blob);
  }

}
