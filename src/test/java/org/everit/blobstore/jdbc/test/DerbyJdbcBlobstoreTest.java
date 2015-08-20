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

import org.apache.derby.jdbc.EmbeddedXADataSource;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class DerbyJdbcBlobstoreTest extends AbstractJdbcBlobstoreTest {

  @BeforeClass
  public static void beforeClass() {
    System.setProperty("derby.stream.error.file", "");
  }

  @Override
  protected XADataSource createXADataSource(final DatabaseAccessParametersDTO parameters) {
    EmbeddedXADataSource embeddedXADataSource = new EmbeddedXADataSource();

    embeddedXADataSource.setDatabaseName(parameters.database);

    if (parameters.connectionAttributes != null) {
      embeddedXADataSource.setConnectionAttributes(parameters.connectionAttributes);
    }

    if (parameters.user != null) {
      embeddedXADataSource.setUser(parameters.user);
    }

    if (parameters.password != null) {
      embeddedXADataSource.setPassword(parameters.password);
    }

    return embeddedXADataSource;
  }

  @Override
  protected DatabaseTestAttributesDTO getDatabaseTestAttributes() {
    DatabaseTestAttributesDTO result = new DatabaseTestAttributesDTO();
    result.dbName = "derby";
    result.enabledByDefault = true;

    DatabaseAccessParametersDTO accessParameters = new DatabaseAccessParametersDTO();
    accessParameters.database = "memory:testDB";
    accessParameters.connectionAttributes = "create=true";

    result.defaultAccessParameters = accessParameters;
    return result;
  }

  @Test
  @Ignore
  @Override
  public void testParallelBlobManipulationWithTwoTransactions() {
    super.testParallelBlobManipulationWithTwoTransactions();
  }

  @Override
  @Test
  @Ignore
  public void testVersionIsUpgradedDuringUpdate() {
    super.testVersionIsUpgradedDuringUpdate();
  }

}
