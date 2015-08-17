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
import org.junit.Ignore;
import org.junit.Test;

import com.microsoft.sqlserver.jdbc.SQLServerXADataSource;
import com.querydsl.sql.OracleTemplates;
import com.querydsl.sql.SQLTemplates;

public class SQLServerJdbcBlobstoreTest extends AbstractJdbcBlobstoreTest {

  @BeforeClass
  public static void beforeClass() {
    Assume.assumeTrue("Testing SQLServer with proprietary driver is skipped. If you want to test"
        + " SQLServer with proprietary driver, define -Dsqlserver.proprietary.enabled=true",
        Boolean.valueOf(System.getProperty("sqlserver.proprietary.enabled")));
  }

  @Override
  protected SQLTemplates getSQLTemplates() {
    return new OracleTemplates(true);
  }

  @Override
  protected XADataSource getXADataSource() {
    SQLServerXADataSource sqlServerXADataSource;
    sqlServerXADataSource = new SQLServerXADataSource();
    sqlServerXADataSource.setURL("jdbc:sqlserver://localhost");
    sqlServerXADataSource.setDatabaseName("test");
    sqlServerXADataSource.setUser("test");
    sqlServerXADataSource.setPassword("test");
    return sqlServerXADataSource;
  }

  @Test
  @Ignore
  @Override
  public void testParallelBlobManipulationWithTwoTransactions() {
    super.testParallelBlobManipulationWithTwoTransactions();
  }

}
