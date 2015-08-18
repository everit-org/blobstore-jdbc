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
import org.everit.blobstore.Blobstore;
import org.everit.blobstore.cache.CachedBlobstore;
import org.everit.blobstore.jdbc.JdbcBlobstore;
import org.everit.blobstore.mem.MemBlobstore;
import org.everit.blobstore.testbase.AbstractBlobstoreTest;
import org.everit.blobstore.testbase.BlobstoreStressAndConsistencyTester;
import org.everit.transaction.map.managed.ManagedMap;
import org.everit.transaction.map.readcommited.ReadCommitedTransactionalMap;
import org.everit.transaction.propagator.TransactionPropagator;
import org.everit.transaction.propagator.jta.JTATransactionPropagator;
import org.junit.After;
import org.junit.Assume;
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
  private static String nullOrNonEmptyString(final String text) {
    if (text == null) {
      return null;
    }

    if ("".equals(text.trim())) {
      return null;
    }

    return text;
  }

  protected JdbcBlobstore blobstore;

  protected BasicManagedDataSource managedDataSource;

  private boolean skipped = false;

  protected GeronimoTransactionManager transactionManager;

  protected TransactionPropagator transactionPropagator;

  @Override
  @After
  public void after() {
    if (skipped) {
      return;
    }
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
    DatabaseAccessParametersDTO databaseAccessParameters = resolveDatabaseAccessParameters();

    skipped = databaseAccessParameters == null;
    Assume.assumeFalse("Tests are not enabled for database " + getDatabaseTestAttributes().dbName,
        skipped);

    try {
      transactionManager = new GeronimoTransactionManager(6000);
    } catch (XAException e) {
      throw new RuntimeException(e);
    }

    XADataSource xaDataSource = createXADataSource(databaseAccessParameters);

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

    transactionPropagator = new JTATransactionPropagator(transactionManager);

  }

  protected BasicManagedDataSource createManagedDataSource(
      final GeronimoTransactionManager transactionManager, final XADataSource xaDataSource) {
    BasicManagedDataSource lManagedDataSource = new BasicManagedDataSource();
    lManagedDataSource.setTransactionManager(transactionManager);
    lManagedDataSource.setXaDataSourceInstance(xaDataSource);
    return lManagedDataSource;
  }

  protected abstract XADataSource createXADataSource(DatabaseAccessParametersDTO parameters);

  @Override
  protected Blobstore getBlobStore() {
    return blobstore;
  }

  protected abstract DatabaseTestAttributesDTO getDatabaseTestAttributes();

  protected abstract SQLTemplates getSQLTemplates();

  @Override
  protected TransactionPropagator getTransactionPropagator() {
    return transactionPropagator;
  }

  protected DatabaseAccessParametersDTO resolveDatabaseAccessParameters() {
    DatabaseTestAttributesDTO databaseTestAttributes = getDatabaseTestAttributes();
    String sysPropPrefix = databaseTestAttributes.dbName + ".";

    boolean enabled = databaseTestAttributes.enabledByDefault;
    String enabledSysProp = System.getProperty(sysPropPrefix + "enabled");
    if (enabledSysProp != null) {
      enabled = Boolean.parseBoolean(enabledSysProp);
    }
    if (!enabled) {
      return null;
    }

    DatabaseAccessParametersDTO defaultAccessParameters =
        databaseTestAttributes.defaultAccessParameters;
    DatabaseAccessParametersDTO result = new DatabaseAccessParametersDTO();
    result.host = nullOrNonEmptyString(
        System.getProperty(sysPropPrefix + "host", defaultAccessParameters.host));

    String portString = nullOrNonEmptyString(System.getProperty(sysPropPrefix + "port"));

    if (portString == null) {
      result.port = defaultAccessParameters.port;
    } else {
      result.port = Integer.parseInt(portString);
    }

    result.database = nullOrNonEmptyString(
        System.getProperty(sysPropPrefix + "database", defaultAccessParameters.database));

    result.password = nullOrNonEmptyString(
        System.getProperty(sysPropPrefix + "password", defaultAccessParameters.password));

    result.user = nullOrNonEmptyString(
        System.getProperty(sysPropPrefix + "user", defaultAccessParameters.user));

    result.connectionAttributes =
        nullOrNonEmptyString(System.getProperty(sysPropPrefix + "connectionAttributes",
            defaultAccessParameters.connectionAttributes));

    return result;
  }

  @Test
  public void testConsistency() {
    System.out
        .println("---------- Consistency Test " + this.getClass().getName() + " --------------");
    MemBlobstore memBlobstore = new MemBlobstore(transactionManager);
    BlobstoreStressAndConsistencyTester.BlobstoreStressTestConfiguration testConfiguration =
        new BlobstoreStressAndConsistencyTester.BlobstoreStressTestConfiguration();

    BlobstoreStressAndConsistencyTester.runStressTest(testConfiguration, transactionPropagator,
        memBlobstore, getBlobStore());
  }

  @Test
  public void testPerformance() {
    System.out
        .println("---------- Performance Test " + this.getClass().getName() + " --------------");
    BlobstoreStressAndConsistencyTester.BlobstoreStressTestConfiguration testConfiguration =
        new BlobstoreStressAndConsistencyTester.BlobstoreStressTestConfiguration();

    testConfiguration.initialBlobNum = 100;
    testConfiguration.createActionChancePart = 5;
    testConfiguration.updateActionChancePart = 5;
    testConfiguration.deleteActionChancePart = 5;
    testConfiguration.readActionChancePart = 85;
    testConfiguration.iterationNumPerThread = 2000;

    BlobstoreStressAndConsistencyTester.runStressTest(testConfiguration, transactionPropagator,
        getBlobStore());
  }

  @Test
  public void testPerformanceWithCache() {
    System.out.println(
        "---------- Performance with Cache Test " + this.getClass().getName() + " --------------");

    BlobstoreStressAndConsistencyTester.BlobstoreStressTestConfiguration testConfiguration =
        new BlobstoreStressAndConsistencyTester.BlobstoreStressTestConfiguration();

    testConfiguration.initialBlobNum = 100;
    testConfiguration.createActionChancePart = 5;
    testConfiguration.updateActionChancePart = 5;
    testConfiguration.deleteActionChancePart = 5;
    testConfiguration.readActionChancePart = 85;
    testConfiguration.iterationNumPerThread = 2000;

    CachedBlobstore cachedBlobstore = new CachedBlobstore(getBlobStore(),
        new ManagedMap<>(new ReadCommitedTransactionalMap<>(new HashMap<>()), transactionManager),
        1024, transactionManager);

    BlobstoreStressAndConsistencyTester.runStressTest(testConfiguration, transactionPropagator,
        cachedBlobstore);
  }

}
