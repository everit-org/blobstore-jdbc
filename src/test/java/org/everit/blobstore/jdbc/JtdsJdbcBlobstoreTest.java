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

import javax.sql.XADataSource;

import org.apache.commons.dbcp2.managed.BasicManagedDataSource;
import org.apache.geronimo.transaction.manager.GeronimoTransactionManager;
import org.junit.Ignore;
import org.junit.Test;

import com.querydsl.sql.SQLServer2012Templates;
import com.querydsl.sql.SQLTemplates;

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
  protected SQLTemplates getSQLTemplates() {
    return new SQLServer2012Templates(true);
  }

  @Override
  protected XADataSource getXADataSource() {
    JtdsDataSource jtdsDataSource = new JtdsDataSource();
    jtdsDataSource.setServerName("localhost");
    jtdsDataSource.setDatabaseName("test");
    jtdsDataSource.setUser("test");
    jtdsDataSource.setPassword("test");
    return jtdsDataSource;
  }

  @Test
  @Ignore
  @Override
  public void test04UpdateParallelBlobManipulationWithTransaction() {
    super.test04UpdateParallelBlobManipulationWithTransaction();
  }

}
