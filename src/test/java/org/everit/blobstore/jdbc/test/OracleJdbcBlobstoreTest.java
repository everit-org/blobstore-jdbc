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
  protected SQLTemplates getSQLTemplates() {
    return new OracleTemplates(true);
  }

  @Override
  protected XADataSource getXADataSource() {
    OracleXADataSource oracleXADataSource;
    try {
      oracleXADataSource = new OracleXADataSource();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    oracleXADataSource.setURL("jdbc:oracle:thin:@localhost:1521/orcl");
    oracleXADataSource.setUser("c##test");
    oracleXADataSource.setPassword("test");
    // TODO Auto-generated method stub
    return oracleXADataSource;
  }
}
