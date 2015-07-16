package org.everit.blobstore.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

import javax.transaction.xa.XAException;

import org.apache.commons.dbcp2.managed.BasicManagedDataSource;
import org.apache.geronimo.transaction.manager.GeronimoTransactionManager;
import org.everit.blobstore.api.Blobstore;
import org.everit.blobstore.testbase.AbstractBlobstoreTest;
import org.everit.osgi.transaction.helper.api.TransactionHelper;
import org.everit.osgi.transaction.helper.internal.TransactionHelperImpl;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.postgresql.xa.PGXADataSource;

import com.querydsl.sql.Configuration;
import com.querydsl.sql.MySQLTemplates;

import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;

public class JdbcBlobstoreTest extends AbstractBlobstoreTest {

  private static Blobstore blobstore;

  private static BasicManagedDataSource managedDataSource;

  private static TransactionHelperImpl transactionHelper;

  @AfterClass
  public static void afterClass() {
    if (managedDataSource != null) {
      try {
        managedDataSource.close();
      } catch (SQLException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

  @BeforeClass
  public static void beforeClass() {
    GeronimoTransactionManager transactionManager;
    try {
      transactionManager = new GeronimoTransactionManager(6000);
    } catch (XAException e) {
      throw new RuntimeException(e);
    }

    // EmbeddedXADataSource xaDataSource = new EmbeddedXADataSource();
    // xaDataSource.setCreateDatabase("create");
    // xaDataSource.setDatabaseName("target/testdb");
    // xaDataSource.setUser("sa");
    // xaDataSource.setPassword("");

    PGXADataSource xaDataSource = new PGXADataSource();
    xaDataSource.setServerName("localhost");
    xaDataSource.setPortNumber(5433);
    xaDataSource.setUser("test");
    xaDataSource.setPassword("test");
    xaDataSource.setDatabaseName("blobstore_jdbc");

    // MysqlXADataSource xaDataSource = new MysqlXADataSource();
    // xaDataSource.setServerName("localhost");
    // xaDataSource.setUser("test");
    // xaDataSource.setPassword("test");
    // xaDataSource.setDatabaseName("blobstore");

    // JDBCXADataSource xaDataSource;
    // try {
    // xaDataSource = new JDBCXADataSource();
    // xaDataSource.setUrl("jdbc:hsqldb:mem:test");
    // } catch (SQLException e) {
    // throw new RuntimeException(e);
    // }

    managedDataSource = new BasicManagedDataSource();
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

    Configuration querydslConfiguration = new Configuration(new MySQLTemplates('\\', false));
    blobstore = new JdbcBlobstore(managedDataSource, querydslConfiguration);

    transactionHelper = new TransactionHelperImpl();
    transactionHelper.setTransactionManager(transactionManager);

  }

  @Override
  protected Blobstore getBlobStore() {
    return blobstore;
  }

  @Override
  protected TransactionHelper getTransactionHelper() {
    return transactionHelper;
  }

}
