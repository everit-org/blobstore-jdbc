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

import javax.sql.XADataSource;

import com.querydsl.sql.OracleTemplates;
import com.querydsl.sql.SQLTemplates;

import oracle.jdbc.xa.client.OracleXADataSource;

public class OracleJdbcBlobstoreTest extends AbstractJdbcBlobstoreTest {

  @Override
  protected XADataSource createXADataSource(final DatabaseAccessParametersDTO parameters) {
    OracleXADataSource xaDataSource;
    try {
      xaDataSource = new OracleXADataSource();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }

    String url = "jdbc:oracle:thin:@" + parameters.host;
    if (parameters.port != null) {
      url += ":" + parameters.port;
    }

    url += ":" + parameters.database;

    if (parameters.connectionAttributes != null) {
      url += "?" + parameters.connectionAttributes;
    }

    xaDataSource.setURL(url);

    if (parameters.user != null) {
      xaDataSource.setUser(parameters.user);
    }

    if (parameters.password != null) {
      xaDataSource.setPassword(parameters.password);
    }
    return xaDataSource;
  }

  @Override
  protected DatabaseTestAttributesDTO getDatabaseTestAttributes() {
    DatabaseTestAttributesDTO result = new DatabaseTestAttributesDTO();
    result.dbName = "oracle";
    result.enabledByDefault = false;

    DatabaseAccessParametersDTO accessParameters = new DatabaseAccessParametersDTO();
    accessParameters.host = "localhost";
    accessParameters.port = 1521;
    accessParameters.database = "orcl";
    accessParameters.user = "c##test";
    accessParameters.password = "test";

    result.defaultAccessParameters = accessParameters;
    return result;
  }

  @Override
  protected SQLTemplates getSQLTemplates() {
    return new OracleTemplates(true);
  }
}
