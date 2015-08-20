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

import javax.sql.XADataSource;

import org.apache.commons.dbcp2.managed.BasicManagedDataSource;
import org.apache.geronimo.transaction.manager.GeronimoTransactionManager;
import org.junit.Ignore;
import org.junit.Test;

import net.sourceforge.jtds.jdbcx.JtdsDataSource;

public class JtdsJdbcBlobstoreTest extends AbstractJdbcBlobstoreTest {

  @Override
  protected BasicManagedDataSource createManagedDataSource(
      final GeronimoTransactionManager transactionManager, final XADataSource xaDataSource) {

    BasicManagedDataSource lManagedDataSource =
        super.createManagedDataSource(transactionManager, xaDataSource);
    lManagedDataSource.setValidationQuery("select 1");
    return lManagedDataSource;
  }

  @Override
  protected XADataSource createXADataSource(final DatabaseAccessParametersDTO parameters) {
    JtdsDataSource jtdsDataSource = new JtdsDataSource();
    jtdsDataSource.setServerName(parameters.host);

    if (parameters.port != null) {
      jtdsDataSource.setPortNumber(parameters.port);
    }

    jtdsDataSource.setDatabaseName(parameters.database);

    if (parameters.user != null) {
      jtdsDataSource.setUser(parameters.user);
    }

    if (parameters.password != null) {
      jtdsDataSource.setPassword(parameters.password);
    }

    return jtdsDataSource;
  }

  @Override
  protected DatabaseTestAttributesDTO getDatabaseTestAttributes() {
    DatabaseTestAttributesDTO result = new DatabaseTestAttributesDTO();
    result.dbName = "sqlserver.jtds";
    result.enabledByDefault = false;

    DatabaseAccessParametersDTO accessParameters = new DatabaseAccessParametersDTO();
    accessParameters.host = "localhost";
    accessParameters.database = "test";
    accessParameters.user = "test";
    accessParameters.password = "test";

    result.defaultAccessParameters = accessParameters;
    return result;
  }

  @Test
  @Ignore
  @Override
  public void testParallelBlobManipulationWithTwoTransactions() {
    super.testParallelBlobManipulationWithTwoTransactions();
  }

}
