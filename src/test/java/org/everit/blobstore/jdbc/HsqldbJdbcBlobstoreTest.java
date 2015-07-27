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

import java.sql.SQLException;

import javax.sql.XADataSource;

import org.hsqldb.jdbc.pool.JDBCXADataSource;
import org.junit.Ignore;
import org.junit.Test;

import com.querydsl.sql.HSQLDBTemplates;
import com.querydsl.sql.SQLTemplates;

public class HsqldbJdbcBlobstoreTest extends AbstractJdbcBlobstoreTest {

  @Override
  protected SQLTemplates getSQLTemplates() {
    return new HSQLDBTemplates(true);
  }

  @Override
  protected XADataSource getXADataSource() {
    JDBCXADataSource xaDataSource;
    try {
      xaDataSource = new JDBCXADataSource();
      xaDataSource.setUrl("jdbc:hsqldb:mem:test");
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    return xaDataSource;
  }

  @Test
  @Ignore
  @Override
  public void test03ParallelBlobUpdateAndRead() {
    super.test03ParallelBlobUpdateAndRead();
  }

  @Test
  @Ignore
  @Override
  public void test04ParallelBlobManipulationWithTwoTransactions() {
    super.test04ParallelBlobManipulationWithTwoTransactions();
  }
}
