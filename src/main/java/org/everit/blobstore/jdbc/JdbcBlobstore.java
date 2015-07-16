package org.everit.blobstore.jdbc;

import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.everit.blobstore.api.BlobAccessor;
import org.everit.blobstore.api.BlobReader;
import org.everit.blobstore.api.Blobstore;
import org.everit.blobstore.api.NoSuchBlobException;
import org.everit.blobstore.jdbc.internal.ConnectedBlob;
import org.everit.blobstore.jdbc.internal.EmptyReadOnlyBlob;
import org.everit.blobstore.jdbc.internal.JdbcBlobAccessor;
import org.everit.blobstore.jdbc.internal.JdbcBlobReader;
import org.everit.blobstore.jdbc.schema.qdsl.QBlobstoreBlob;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;
import com.querydsl.core.Tuple;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.SQLBindings;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.dml.SQLDeleteClause;
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
   * @param closeable
   *          The closeable instance that should be closed.
   * @param e
   *          The {@link Error} or {@link RuntimeException} that occured.
   */
  protected void closeCloseableDueToThrowable(final AutoCloseable closeable, final Throwable e) {
    try {
      closeable.close();
    } catch (Throwable th) {
      e.addSuppressed(th);
    }
    if (e instanceof Error) {
      throw (Error) e;
    } else if (e instanceof RuntimeException) {
      // TODO
      throw (RuntimeException) e;
    }
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
  protected ConnectedBlob connectBlob(final long blobId, final boolean forUpdate,
      final Connection connection) {
    QBlobstoreBlob qBlob = QBlobstoreBlob.blobstoreBlob;
    try {
      SQLQuery<Tuple> query = new SQLQuery<>(connection, querydslConfiguration)
          .select(qBlob.blobId, qBlob.version_.as("version_"), qBlob.blob_.as("blob_"))
          .from(qBlob)
          .where(qBlob.blobId.eq(blobId));
      if (forUpdate) {
        query.forUpdate();
      }
      SQLBindings sqlBindings = query.getSQL();

      PreparedStatement preparedStatement = connection.prepareStatement(sqlBindings.getSQL());
      ImmutableList<Object> bindings = sqlBindings.getBindings();
      int i = 0;
      UnmodifiableIterator<Object> iterator = bindings.iterator();
      while (iterator.hasNext()) {
        Object binding = iterator.next();
        i++;
        preparedStatement.setObject(i, binding);
      }
      ResultSet resultSet = preparedStatement.executeQuery();
      if (!resultSet.next()) {
        throw new NoSuchBlobException(blobId);
      }

      long version = resultSet.getLong("version_");
      Blob blob = resultSet.getBlob("blob_");

      return new ConnectedBlob(blobId, blob, version, preparedStatement);

    } catch (SQLException | RuntimeException | Error e) {
      closeCloseableDueToThrowable(connection, e);
      // TODO throw unchecked sql exception
      throw new RuntimeException(e);
    }
  }

  @Override
  public BlobAccessor createBlob() {
    Connection connection = getNewDatabaseConnection();

    QBlobstoreBlob qBlob = QBlobstoreBlob.blobstoreBlob;
    Long blobId;
    try {
      blobId =
          new SQLInsertClause(connection, querydslConfiguration, qBlob)
              .set(qBlob.version_, 0L).set(qBlob.blob_, new EmptyReadOnlyBlob())
              .executeWithKey(qBlob.blobId);
    } catch (RuntimeException | Error e) {
      closeCloseableDueToThrowable(connection, e);
      throw new RuntimeException(e);
    }

    return updateBlob(blobId, connection);
  }

  @Override
  public void deleteBlob(final long blobId) {
    try (Connection connection = dataSource.getConnection()) {
      QBlobstoreBlob qBlob = QBlobstoreBlob.blobstoreBlob;
      long rowNum = new SQLDeleteClause(connection, querydslConfiguration, qBlob)
          .where(qBlob.blobId.eq(blobId))
          .execute();
      if (rowNum == 0) {
        throw new NoSuchBlobException(blobId);
      }
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      throw new RuntimeException(e);
    }
  }

  private Connection getNewDatabaseConnection() {
    Connection connection;
    try {
      connection = dataSource.getConnection();
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      throw new RuntimeException(e);
    }
    return connection;
  }

  @Override
  public BlobReader readBlob(final long blobId) {
    Connection connection = getNewDatabaseConnection();
    ConnectedBlob connectedBlob = connectBlob(blobId, false, connection);
    return new JdbcBlobReader(connectedBlob, connection);
  }

  @Override
  public BlobReader readBlobForUpdate(final long blobId) {
    Connection connection = getNewDatabaseConnection();
    ConnectedBlob connectedBlob = connectBlob(blobId, true, connection);
    return new JdbcBlobReader(connectedBlob, connection);
  }

  @Override
  public BlobAccessor updateBlob(final long blobId) {
    Connection connection = getNewDatabaseConnection();
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
    ConnectedBlob connectedBlob = connectBlob(blobId, true, connection);
    return new JdbcBlobAccessor(connectedBlob, connection, querydslConfiguration);
  }

}
