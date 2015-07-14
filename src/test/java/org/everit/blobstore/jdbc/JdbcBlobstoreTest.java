package org.everit.blobstore.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

import javax.transaction.xa.XAException;

import org.apache.commons.dbcp2.managed.BasicManagedDataSource;
import org.apache.geronimo.transaction.manager.GeronimoTransactionManager;
import org.everit.blobstore.api.BlobAccessor;
import org.everit.blobstore.api.Blobstore;
import org.hsqldb.jdbc.pool.JDBCXADataSource;
import org.junit.Test;

import com.querydsl.sql.Configuration;
import com.querydsl.sql.HSQLDBTemplates;

import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;

public class JdbcBlobstoreTest {

  @Test
  public void testBlobCreation() {
    GeronimoTransactionManager transactionManager;
    try {
      transactionManager = new GeronimoTransactionManager(6000);
    } catch (XAException e) {
      throw new RuntimeException(e);
    }

    JDBCXADataSource xaDataSource;
    try {
      xaDataSource = new JDBCXADataSource();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }

    xaDataSource.setUrl("jdbc:hsqldb:mem:test");
    BasicManagedDataSource managedDataSource = new BasicManagedDataSource();
    managedDataSource.setTransactionManager(transactionManager);
    managedDataSource.setXaDataSourceInstance(xaDataSource);

    try (Connection connection = managedDataSource.getConnection()) {
      DatabaseConnection databaseConnection = new JdbcConnection(connection);

      Liquibase liquibase =
          new Liquibase("META-INF/liquibase/org.everit.blobstore.jdbc.changelog.xml",
              new ClassLoaderResourceAccessor(), databaseConnection);

      liquibase.update((Contexts) null);
    } catch (LiquibaseException | SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    Configuration querydslConfiguration = new Configuration(new HSQLDBTemplates(true));
    Blobstore blobstore =
        new JdbcBlobstore(managedDataSource, querydslConfiguration);

    BlobAccessor blobAccessor = blobstore.createBlob();
    System.out.println(blobAccessor.getBlobId());
    blobAccessor.close();

    try {
      managedDataSource.close();
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
