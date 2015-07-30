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

import com.mysql.jdbc.jdbc2.optional.MysqlXADataSource;
import com.querydsl.sql.MySQLTemplates;
import com.querydsl.sql.SQLTemplates;

public class MySQLJdbcBlobstoreTest extends AbstractJdbcBlobstoreTest {

  @Override
  protected SQLTemplates getSQLTemplates() {
    return new MySQLTemplates(true);
  }

  @Override
  protected XADataSource getXADataSource() {
    MysqlXADataSource xaDataSource = new MysqlXADataSource();
    xaDataSource.setUrl("jdbc:mysql://localhost/blobstore?=blobSendChunkSize=1000000");
    // xaDataSource.setServerName("localhost");
    xaDataSource.setUser("test");
    xaDataSource.setPassword("test");
    // xaDataSource.setEmulateLocators(true);
    // xaDataSource.setDatabaseName("blobstore");
    // try {
    // xaDataSource.setMaxAllowedPacket(1024 * 1024 * 11);
    // xaDataSource.setBlobSendChunkSize("1M");
    // } catch (SQLException e) {
    // // TODO Auto-generated catch block
    // throw new RuntimeException(e);
    // }
    return xaDataSource;
  }

}
