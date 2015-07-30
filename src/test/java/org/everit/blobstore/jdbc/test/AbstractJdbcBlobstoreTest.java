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
package org.everit.blobstore.jdbc.test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;

import javax.sql.XADataSource;
import javax.transaction.xa.XAException;

import org.apache.commons.dbcp2.managed.BasicManagedDataSource;
import org.apache.geronimo.transaction.manager.GeronimoTransactionManager;
import org.everit.blobstore.api.Blobstore;
import org.everit.blobstore.cache.CachedBlobstore;
import org.everit.blobstore.jdbc.JdbcBlobstore;
import org.everit.blobstore.mem.MemBlobstore;
import org.everit.blobstore.testbase.AbstractBlobstoreTest;
import org.everit.blobstore.testbase.BlobstoreStressAndConsistencyTester;
import org.everit.osgi.transaction.helper.api.TransactionHelper;
import org.everit.osgi.transaction.helper.internal.TransactionHelperImpl;
import org.everit.transaction.map.managed.ManagedMap;
import org.everit.transaction.map.readcommited.ReadCommitedTransactionalMap;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.querydsl.sql.SQLTemplates;

import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;

public abstract class AbstractJdbcBlobstoreTest extends AbstractBlobstoreTest {
  protected JdbcBlobstore blobstore;

  protected BasicManagedDataSource managedDataSource;

  protected TransactionHelperImpl transactionHelper;

  protected GeronimoTransactionManager transactionManager;

  @Override
  @After
  public void after() {
    super.after();
    if (managedDataSource != null) {
      try {
        managedDataSource.close();
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Before
  public void before() {
    try {
      transactionManager = new GeronimoTransactionManager(6000);
    } catch (XAException e) {
      throw new RuntimeException(e);
    }

    XADataSource xaDataSource = getXADataSource();

    managedDataSource = createManagedDataSource(transactionManager, xaDataSource);

    try (Connection connection = managedDataSource.getConnection()) {
      DatabaseConnection databaseConnection = new JdbcConnection(connection);

      Liquibase liquibase = new Liquibase(
          "META-INF/liquibase/org.everit.blobstore.jdbc.changelog.xml",
          new ClassLoaderResourceAccessor(), databaseConnection);

      liquibase.update((Contexts) null);
    } catch (LiquibaseException | SQLException e) {
      try {
        managedDataSource.close();
      } catch (SQLException e1) {
        e.addSuppressed(e1);
      }
      throw new RuntimeException(e);
    }

    blobstore = new JdbcBlobstore(managedDataSource);

    transactionHelper = new TransactionHelperImpl();
    transactionHelper.setTransactionManager(transactionManager);

  }

  protected BasicManagedDataSource createManagedDataSource(
      final GeronimoTransactionManager transactionManager, final XADataSource xaDataSource) {
    BasicManagedDataSource lManagedDataSource = new BasicManagedDataSource();
    lManagedDataSource.setTransactionManager(transactionManager);
    lManagedDataSource.setXaDataSourceInstance(xaDataSource);
    return lManagedDataSource;
  }

  @Override
  protected Blobstore getBlobStore() {
    return blobstore;
  }

  protected abstract SQLTemplates getSQLTemplates();

  @Override
  protected TransactionHelper getTransactionHelper() {
    return transactionHelper;
  }

  protected abstract XADataSource getXADataSource();

  @Test
  public void testConsistency() {
    System.out.println("---------- Consistency Test --------------");
    MemBlobstore memBlobstore = new MemBlobstore(transactionManager);
    BlobstoreStressAndConsistencyTester.BlobstoreStressTestConfiguration testConfiguration =
        new BlobstoreStressAndConsistencyTester.BlobstoreStressTestConfiguration();

    BlobstoreStressAndConsistencyTester.runStressTest(testConfiguration, transactionHelper,
        getBlobStore(), memBlobstore);
  }

  @Test
  public void testPerformance() {
    System.out.println("---------- Performance Test --------------");
    BlobstoreStressAndConsistencyTester.BlobstoreStressTestConfiguration testConfiguration =
        new BlobstoreStressAndConsistencyTester.BlobstoreStressTestConfiguration();

    testConfiguration.initialBlobNum = 10;
    testConfiguration.createActionChancePart = 0;
    testConfiguration.updateActionChancePart = 0;
    testConfiguration.deleteActionChancePart = 0;

    BlobstoreStressAndConsistencyTester.runStressTest(testConfiguration, transactionHelper,
        getBlobStore());
  }

  @Test
  public void testPerformanceWithCache() {
    System.out.println("---------- Performance with Cache Test --------------");

    BlobstoreStressAndConsistencyTester.BlobstoreStressTestConfiguration testConfiguration =
        new BlobstoreStressAndConsistencyTester.BlobstoreStressTestConfiguration();

    testConfiguration.initialBlobNum = 10;
    testConfiguration.createActionChancePart = 0;
    testConfiguration.updateActionChancePart = 0;
    testConfiguration.deleteActionChancePart = 0;

    CachedBlobstore cachedBlobstore = new CachedBlobstore(getBlobStore(),
        new ManagedMap<>(new ReadCommitedTransactionalMap<>(new HashMap<>()), transactionManager),
        1024, transactionManager);

    BlobstoreStressAndConsistencyTester.runStressTest(testConfiguration, transactionHelper,
        cachedBlobstore);
  }

}
