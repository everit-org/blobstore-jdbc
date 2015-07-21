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

import java.sql.Blob;

import com.querydsl.core.types.Expression;
import com.querydsl.sql.Configuration;

/**
 * Configuration of JdbcBlobstore.
 */
public class JdbcBlobstoreConfiguration {

  /**
   * Expression that generates the empty blob. If <code>null</code> the empty blob expression is
   * automatically guessed from the type of the database.
   */
  public Expression<Blob> emptyBlobExpression = null;

  /**
   * Query enhancer to do pessimistic locking on the selected record.
   */
  public QueryEnhancer pessimisticLockQueryEnhancer = null;

  /**
   * Configuration of queryDsl to construct the SQL queries. If null, it is guessed based on the
   * metadata of the database connection.
   */
  public Configuration querydslConfiguration = null;

  /**
   * Whether calling update SQL after manipulating the Blob instance is necessary or not.
   */
  public Boolean updateSQLForModifiedBlobContentNecessary = null;
}
