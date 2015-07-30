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
package org.everit.blobstore.jdbc;

import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import javax.sql.DataSource;

import org.everit.blobstore.api.BlobAccessor;
import org.everit.blobstore.api.BlobReader;
import org.everit.blobstore.api.Blobstore;
import org.everit.blobstore.api.NoSuchBlobException;
import org.everit.blobstore.jdbc.internal.BlobChannel;
import org.everit.blobstore.jdbc.internal.BytesBlobChannel;
import org.everit.blobstore.jdbc.internal.ConnectedBlob;
import org.everit.blobstore.jdbc.internal.DatabaseTypeEnum;
import org.everit.blobstore.jdbc.internal.EmptyReadOnlyBlob;
import org.everit.blobstore.jdbc.internal.JdbcBlobAccessor;
import org.everit.blobstore.jdbc.internal.JdbcBlobReader;
import org.everit.blobstore.jdbc.internal.StreamBlobChannel;
import org.everit.blobstore.jdbc.schema.qdsl.QBlobstoreBlob;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;
import com.querydsl.core.QueryFlag;
import com.querydsl.core.QueryFlag.Position;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.SQLBindings;
import com.querydsl.sql.SQLExpressions;
import com.querydsl.sql.SQLOps;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLTemplates;
import com.querydsl.sql.SQLTemplatesRegistry;
import com.querydsl.sql.dml.SQLDeleteClause;
import com.querydsl.sql.dml.SQLInsertClause;

/**
 * JDBC based implementation of Blobstore.
 *
 */
public class JdbcBlobstore implements Blobstore {

  protected final BlobAccessMode blobAccessMode;

  protected final Expression<?> blobSelectionExpression;

  protected final DataSource dataSource;

  protected final Expression<Blob> emptyBlobExpression;

  private DatabaseTypeEnum guessedDatabaseType;

  protected final boolean locatorUpdatesCopy;

  protected final QueryFlag pessimisticLockQueryFlag;

  protected final Configuration querydslConfiguration;

  public JdbcBlobstore(final DataSource dataSource) {
    this(dataSource, null);
  }

  public JdbcBlobstore(final DataSource dataSource,
      final JdbcBlobstoreConfiguration configuration) {
    if (dataSource == null) {
      throw new IllegalArgumentException("DataSource must be defined");
    }
    this.dataSource = dataSource;
    this.querydslConfiguration = resolveQuerydslConfiguration(configuration);
    this.emptyBlobExpression = resolveEmptyBlobExpression(configuration);
    this.pessimisticLockQueryFlag = resolvePessimistickLockQueryFlag(configuration);
    this.locatorUpdatesCopy = resolveLocatorUpdatesCopy(
        configuration);
    this.blobSelectionExpression = resolveBlobSelectionExpression(configuration);
    this.blobAccessMode = resolveBlobAccessMode(configuration);
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
          .select(qBlob.blobId, qBlob.version_.as("version_"),
              Expressions.as(blobSelectionExpression, "blob_")) // CS_DISABLE_LINE_LENGTH
                                                                // qBlob.blob_.as("blob_")
          .from(qBlob)
          .where(qBlob.blobId.eq(blobId));
      if (forUpdate) {
        query.addFlag(pessimisticLockQueryFlag);
      }
      SQLBindings sqlBindings = query.getSQL();

      String sql = sqlBindings.getSQL();
      PreparedStatement preparedStatement = connection.prepareStatement(sql);
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

      BlobChannel blobChannel;
      if (blobAccessMode == BlobAccessMode.BYTES) {
        blobChannel = new BytesBlobChannel(blob);
      } else {
        blobChannel = new StreamBlobChannel(blob);
      }
      return new ConnectedBlob(blobId, blobChannel, version, preparedStatement);

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
      blobId = new SQLInsertClause(connection, querydslConfiguration, qBlob)
          .set(qBlob.version_, 0L).set(qBlob.blob_, emptyBlobExpression)
          .executeWithKey(qBlob.blobId);
    } catch (RuntimeException | Error e) {
      closeCloseableDueToThrowable(connection, e);
      throw new RuntimeException(e);
    }

    return updateBlob(blobId, connection, false);
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

  protected Connection getNewDatabaseConnection() {
    Connection connection;
    try {
      connection = dataSource.getConnection();
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      throw new RuntimeException(e);
    }
    return connection;
  }

  /**
   * Guess the database type based on the metadata information of the {@link Connection} that is
   * provided by the specified {@link DataSource}.
   *
   * @return The guessed type of the database.
   */
  protected DatabaseTypeEnum guessDatabaseType() {
    if (this.guessedDatabaseType != null) {
      return this.guessedDatabaseType;
    }
    String databaseProductName;

    try (Connection connection = dataSource.getConnection()) {
      databaseProductName = connection.getMetaData().getDatabaseProductName();
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      throw new RuntimeException(e);
    }

    DatabaseTypeEnum[] databaseTypes = DatabaseTypeEnum.values();
    for (int i = 0; i < databaseTypes.length && guessedDatabaseType == null; i++) {
      DatabaseTypeEnum databaseType = databaseTypes[i];
      List<String> productNames = databaseType.getDatabaseProductNames();
      Iterator<String> iterator = productNames.iterator();

      while (iterator.hasNext() && guessedDatabaseType == null) {
        String productName = iterator.next();
        if (productName != null && productName.equalsIgnoreCase(databaseProductName)) {
          guessedDatabaseType = databaseType;
        }
      }
    }
    if (guessedDatabaseType == null) {
      guessedDatabaseType = DatabaseTypeEnum.UNKNOWN;
    }

    return guessedDatabaseType;
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

  /**
   * Resolving the access mode how the database should be accessed.
   *
   * @param configuration
   *          The configuration that might contain pre-defined access mode.
   * @return The database access mode.
   */
  protected BlobAccessMode resolveBlobAccessMode(final JdbcBlobstoreConfiguration configuration) {

    if (configuration != null && configuration.blobAccessMode != null) {
      return blobAccessMode;
    }

    BlobAccessMode result;
    DatabaseTypeEnum databaseType = guessDatabaseType();
    switch (databaseType) {
      case DERBY:
        result = BlobAccessMode.STREAM;
        break;
      case HSQLDB:
        result = BlobAccessMode.STREAM;
        break;
      case MYSQL:
        result = BlobAccessMode.STREAM;
        break;
      case ORACLE:
        result = BlobAccessMode.STREAM;
        break;
      case POSTGRESQL:
        result = BlobAccessMode.STREAM;
        break;
      case SQLSERVER:
        result = BlobAccessMode.STREAM;
        break;
      default:
        result = BlobAccessMode.BYTES;
        break;
    }

    return result;
  }

  protected Expression<?> resolveBlobSelectionExpression(
      final JdbcBlobstoreConfiguration configuration) {

    if (configuration != null && configuration.blobSelectionExpression != null) {
      return configuration.blobSelectionExpression;
    }

    DatabaseTypeEnum databaseType = guessDatabaseType();
    if (databaseType != DatabaseTypeEnum.MYSQL) {
      return QBlobstoreBlob.blobstoreBlob.blob_;
    }

    try (Connection connection = dataSource.getConnection()) {
      if (!connection.getMetaData().locatorsUpdateCopy()) {
        return Expressions.constant(QBlobstoreBlob.blobstoreBlob.blob_.getMetadata().getName());
      } else {
        return QBlobstoreBlob.blobstoreBlob.blob_;
      }
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      throw new RuntimeException(e);
    }
  }

  protected Expression<Blob> resolveEmptyBlobExpression(
      final JdbcBlobstoreConfiguration configuration) {

    if (configuration != null && configuration.emptyBlobExpression != null) {
      return configuration.emptyBlobExpression;
    }

    DatabaseTypeEnum databaseType = guessDatabaseType();

    if (databaseType == DatabaseTypeEnum.ORACLE) {
      return SQLExpressions.relationalFunctionCall(Blob.class, "empty_blob");
    } else {
      return Expressions.constant(new EmptyReadOnlyBlob());
    }
  }

  protected boolean resolveLocatorUpdatesCopy(
      final JdbcBlobstoreConfiguration configuration) {

    try (Connection connection = dataSource.getConnection()) {
      return connection.getMetaData().locatorsUpdateCopy();
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      throw new RuntimeException(e);
    }
  }

  protected QueryFlag resolvePessimistickLockQueryFlag(
      final JdbcBlobstoreConfiguration configuration) {

    if (configuration != null && configuration.pessimisticLockQueryFlag != null) {
      return configuration.pessimisticLockQueryFlag;
    }

    DatabaseTypeEnum databaseType = guessDatabaseType();
    if (databaseType == DatabaseTypeEnum.SQLSERVER) {
      return new QueryFlag(Position.BEFORE_FILTERS, "\nwith (updlock)");
    } else {
      return SQLOps.FOR_UPDATE_FLAG;
    }
  }

  protected Configuration resolveQuerydslConfiguration(
      final JdbcBlobstoreConfiguration blobstoreConfiguration) {
    if (blobstoreConfiguration != null && blobstoreConfiguration.querydslConfiguration != null) {
      return querydslConfiguration;
    }

    try (Connection connection = dataSource.getConnection()) {
      SQLTemplates templates = new SQLTemplatesRegistry().getTemplates(connection.getMetaData());
      return new Configuration(templates);
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      throw new RuntimeException(e);
    }
  }

  @Override
  public BlobAccessor updateBlob(final long blobId) {
    Connection connection = getNewDatabaseConnection();
    return updateBlob(blobId, connection, true);
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
  protected BlobAccessor updateBlob(final long blobId, final Connection connection,
      final boolean incrementVersion) {
    ConnectedBlob connectedBlob = connectBlob(blobId, true, connection);
    return new JdbcBlobAccessor(connectedBlob, connection, querydslConfiguration,
        locatorUpdatesCopy, incrementVersion);
  }

}
