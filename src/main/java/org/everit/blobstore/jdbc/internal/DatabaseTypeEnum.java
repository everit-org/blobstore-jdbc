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
package org.everit.blobstore.jdbc.internal;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * The database types known by JdbcBlobstore by default.
 */
public enum DatabaseTypeEnum {

  DERBY("Apache Derby"), HSQLDB("HSQL Database Engine"), MYSQL("MySQL", "MariaDB"), ORACLE(
      "oracle"), POSTGRESQL("PostgreSQL"), SQLSERVER("Microsoft SQL Server", "SQLOLEDB"), UNKNOWN();

  private final List<String> databaseProductNames;

  DatabaseTypeEnum(final String... databaseProductNames) {
    this.databaseProductNames = Collections.unmodifiableList(Arrays.asList(databaseProductNames));
  }

  public List<String> getDatabaseProductNames() {
    return databaseProductNames;
  }
}
