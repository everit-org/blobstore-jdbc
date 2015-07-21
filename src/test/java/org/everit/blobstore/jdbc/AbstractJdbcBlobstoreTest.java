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

import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.XADataSource;
import javax.transaction.xa.XAException;

import org.apache.commons.dbcp2.managed.BasicManagedDataSource;
import org.apache.geronimo.transaction.manager.GeronimoTransactionManager;
import org.everit.blobstore.api.Blobstore;
import org.everit.blobstore.testbase.AbstractBlobstoreTest;
import org.everit.osgi.transaction.helper.api.TransactionHelper;
import org.everit.osgi.transaction.helper.internal.TransactionHelperImpl;
import org.junit.After;
import org.junit.Before;

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
    GeronimoTransactionManager transactionManager;
    try {
      transactionManager = new GeronimoTransactionManager(6000);
    } catch (XAException e) {
      throw new RuntimeException(e);
    }

    XADataSource xaDataSource = getXADataSource();

    managedDataSource = createManagedDataSource(transactionManager, xaDataSource);

    try (Connection connection = managedDataSource.getConnection()) {
      DatabaseConnection databaseConnection = new JdbcConnection(connection);

      Liquibase liquibase =
          new Liquibase("META-INF/liquibase/org.everit.blobstore.jdbc.changelog.xml",
              new ClassLoaderResourceAccessor(), databaseConnection);

      StringWriter sw = new StringWriter();
      liquibase.update((Contexts) null, sw);
      System.out.println("Update SQL: \n" + sw.toString());
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
}
