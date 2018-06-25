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

import java.sql.SQLException;
import java.util.Locale;

import javax.sql.XADataSource;

import org.everit.blobstore.jdbc.JdbcBlobstoreConfiguration;
import org.hsqldb.jdbc.pool.JDBCXADataSource;
import org.junit.Ignore;
import org.junit.Test;

import com.querydsl.sql.Configuration;
import com.querydsl.sql.HSQLDBTemplates;
import com.querydsl.sql.namemapping.ChangeLetterCaseNameMapping;
import com.querydsl.sql.namemapping.ChangeLetterCaseNameMapping.LetterCase;

public class HsqldbJdbcBlobstoreTest extends AbstractJdbcBlobstoreTest {

  @Override
  protected JdbcBlobstoreConfiguration createJdbcBlobstoreConfiguration() {
    JdbcBlobstoreConfiguration result = new JdbcBlobstoreConfiguration();
    result.querydslConfiguration = new Configuration(new HSQLDBTemplates());
    result.querydslConfiguration
        .setDynamicNameMapping(new ChangeLetterCaseNameMapping(LetterCase.UPPER, Locale.US));
    return super.createJdbcBlobstoreConfiguration();
  }

  @Override
  protected XADataSource createXADataSource(final DatabaseAccessParametersDTO parameters) {
    JDBCXADataSource xaDataSource;
    try {
      xaDataSource = new JDBCXADataSource();

      String url = "jdbc:hsqldb:" + parameters.database;

      xaDataSource.setUrl(url);

      if (parameters.user != null) {
        xaDataSource.setUser(parameters.user);
      }

      if (parameters.password != null) {
        xaDataSource.setPassword(parameters.password);
      }

    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    return xaDataSource;
  }

  @Override
  protected DatabaseTestAttributesDTO getDatabaseTestAttributes() {
    DatabaseTestAttributesDTO result = new DatabaseTestAttributesDTO();
    result.dbName = "hsqldb";
    result.enabledByDefault = true;

    DatabaseAccessParametersDTO accessParameters = new DatabaseAccessParametersDTO();
    accessParameters.database = "mem:test";

    result.defaultAccessParameters = accessParameters;
    return result;
  }

  @Test
  @Ignore
  @Override
  public void testParallelBlobManipulationWithTwoTransactions() {
    super.testParallelBlobManipulationWithTwoTransactions();
  }

  @Test
  @Ignore
  @Override
  public void testReadBlobDuringOngoingUpdateOnOtherThread() {
    super.testReadBlobDuringOngoingUpdateOnOtherThread();
  }
}
