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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
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
import org.everit.blobstore.jdbc.JdbcBlobstoreConfiguration;
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
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
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
    if (this.skipped) {
      return;
    }
    super.after();
    if (this.managedDataSource != null) {
      try {
        this.managedDataSource.close();
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Before
  public void before() {
    DatabaseAccessParametersDTO databaseAccessParameters = resolveDatabaseAccessParameters();

    this.skipped = databaseAccessParameters == null;
    Assume.assumeFalse("Tests are not enabled for database " + getDatabaseTestAttributes().dbName,
        this.skipped);

    try {
      this.transactionManager = new GeronimoTransactionManager(6000);
    } catch (XAException e) {
      throw new RuntimeException(e);
    }

    XADataSource xaDataSource = createXADataSource(databaseAccessParameters);

    this.managedDataSource = createManagedDataSource(this.transactionManager, xaDataSource);

    try (Connection connection = this.managedDataSource.getConnection()) {
      DatabaseConnection databaseConnection = new JdbcConnection(connection);

      Liquibase liquibase = new Liquibase(
          "META-INF/liquibase/org.everit.blobstore.jdbc.changelog.xml",
          new ClassLoaderResourceAccessor(), databaseConnection);

      String sqlOutputFolder = System.getProperty("blobstore.sql.outputFolder");
      if (sqlOutputFolder != null) {
        File folder = new File(sqlOutputFolder);
        folder.mkdirs();

        File outputFile =
            new File(folder, "blobstore-" + getDatabaseTestAttributes().dbName + ".sql");

        try (FileWriter fw = new FileWriter(outputFile, true)) {

          liquibase.update((Contexts) null, fw);
        } catch (IOException e) {
          throw new UncheckedIOException(e);
        }
      }

      liquibase.update((Contexts) null);
    } catch (LiquibaseException | SQLException e) {
      try {
        this.managedDataSource.close();
      } catch (SQLException e1) {
        e.addSuppressed(e1);
      }
      throw new RuntimeException(e);
    }

    this.blobstore = new JdbcBlobstore(this.managedDataSource);

    this.transactionPropagator = new JTATransactionPropagator(this.transactionManager);

  }

  protected JdbcBlobstoreConfiguration createJdbcBlobstoreConfiguration() {
    return null;
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
    return this.blobstore;
  }

  protected abstract DatabaseTestAttributesDTO getDatabaseTestAttributes();

  @Override
  protected TransactionPropagator getTransactionPropagator() {
    return this.transactionPropagator;
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
  @Ignore("Will be refactored later to be able to run stress tests easily with different"
      + " configurations")
  public void testConsistency() {
    System.out
        .println(
            "---------- Consistency Test (" + this.getClass().getSimpleName() + ") --------------");
    MemBlobstore memBlobstore = new MemBlobstore(this.transactionManager);
    BlobstoreStressAndConsistencyTester.BlobstoreStressTestConfiguration testConfiguration =
        new BlobstoreStressAndConsistencyTester.BlobstoreStressTestConfiguration();

    BlobstoreStressAndConsistencyTester.runStressTest(testConfiguration, this.transactionPropagator,
        memBlobstore, getBlobStore());
  }

  @Test
  @Ignore("Will be refactored later to be able to run stress tests easily with different"
      + " configurations")
  public void testPerformance() {
    System.out
        .println(
            "---------- Performance Test (" + this.getClass().getSimpleName() + ") --------------");
    BlobstoreStressAndConsistencyTester.BlobstoreStressTestConfiguration testConfiguration =
        new BlobstoreStressAndConsistencyTester.BlobstoreStressTestConfiguration();

    testConfiguration.initialBlobNum = 100;
    testConfiguration.createActionChancePart = 5;
    testConfiguration.updateActionChancePart = 5;
    testConfiguration.deleteActionChancePart = 5;
    testConfiguration.readActionChancePart = 85;
    testConfiguration.iterationNumPerThread = 500;

    BlobstoreStressAndConsistencyTester.runStressTest(testConfiguration, this.transactionPropagator,
        getBlobStore());
  }

  @Test
  public void testPerformanceWithCache() {
    System.out.println(
        "---------- Performance with Cache Test (" + this.getClass().getSimpleName()
            + ") --------------");

    BlobstoreStressAndConsistencyTester.BlobstoreStressTestConfiguration testConfiguration =
        new BlobstoreStressAndConsistencyTester.BlobstoreStressTestConfiguration();

    testConfiguration.initialBlobNum = 100;
    testConfiguration.createActionChancePart = 5;
    testConfiguration.updateActionChancePart = 5;
    testConfiguration.deleteActionChancePart = 5;
    testConfiguration.readActionChancePart = 85;
    testConfiguration.iterationNumPerThread = 500;

    CachedBlobstore cachedBlobstore = new CachedBlobstore(getBlobStore(),
        new ManagedMap<>(new ReadCommitedTransactionalMap<>(new HashMap<>()),
            this.transactionManager),
        1024, this.transactionManager);

    BlobstoreStressAndConsistencyTester.runStressTest(testConfiguration, this.transactionPropagator,
        cachedBlobstore);
  }

}
