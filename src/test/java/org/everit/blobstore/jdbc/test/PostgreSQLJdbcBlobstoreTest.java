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

import org.junit.Assume;
import org.junit.BeforeClass;
import org.postgresql.xa.PGXADataSource;

import com.querydsl.sql.PostgreSQLTemplates;
import com.querydsl.sql.SQLTemplates;

public class PostgreSQLJdbcBlobstoreTest extends AbstractJdbcBlobstoreTest {

  @BeforeClass
  public static void beforeClass() {
    Assume.assumeTrue("Testing PostgreSQL Server is skipped. If you want to test PostgreSQL"
        + " , define -Dpostgresql.enabled=true",
        Boolean.valueOf(System.getProperty("postgresql.enabled")));
  }

  @Override
  protected SQLTemplates getSQLTemplates() {
    return new PostgreSQLTemplates(true);
  }

  @Override
  protected XADataSource getXADataSource() {
    PGXADataSource xaDataSource = new PGXADataSource();
    xaDataSource.setServerName("localhost");
    xaDataSource.setPortNumber(5433);
    xaDataSource.setUser("test");
    xaDataSource.setPassword("test");
    xaDataSource.setDatabaseName("blobstore_jdbc");
    return xaDataSource;
  }

}
