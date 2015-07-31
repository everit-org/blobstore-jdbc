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

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import javax.sql.XADataSource;

import org.apache.derby.jdbc.EmbeddedXADataSource;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.querydsl.sql.DerbyTemplates;
import com.querydsl.sql.SQLTemplates;

public class DerbyJdbcBlobstoreTest extends AbstractJdbcBlobstoreTest {

  private static File dbDirFile;

  private static EmbeddedXADataSource embeddedXADataSource;

  @AfterClass
  public static void afterClass() {
    embeddedXADataSource.setShutdownDatabase("shutdown");
    embeddedXADataSource = null;

    deleteDirRecurse(dbDirFile);
  }

  @BeforeClass
  public static void beforeClass() {
    createTempDbDir();

    embeddedXADataSource = new EmbeddedXADataSource();
    embeddedXADataSource.setCreateDatabase("create");

    embeddedXADataSource.setDatabaseName(dbDirFile.getAbsolutePath());
    embeddedXADataSource.setUser("sa");
    embeddedXADataSource.setPassword("");
  }

  private static void createTempDbDir() {
    try {
      File tempFile = File.createTempFile("jdbcDerbyTest", "");
      File tempDirFile = tempFile.getParentFile();
      tempFile.delete();
      String dbDirName = "derbyJdbcBlobstoreTest-" + UUID.randomUUID().toString();
      dbDirFile = new File(tempDirFile, dbDirName);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static void deleteDirRecurse(final File directory) {
    File[] files = directory.listFiles();
    for (File file : files) {
      if (file.isDirectory()) {
        deleteDirRecurse(file);
      } else {
        file.delete();
      }
    }
    directory.delete();
  }

  @Override
  protected SQLTemplates getSQLTemplates() {
    return new DerbyTemplates(true);
  }

  @Override
  protected XADataSource getXADataSource() {
    return embeddedXADataSource;
  }

  @Test
  @Ignore
  @Override
  public void testParallelBlobManipulationWithTwoTransactions() {
    super.testParallelBlobManipulationWithTwoTransactions();
  }

}
