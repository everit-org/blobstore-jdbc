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

import com.querydsl.core.QueryFlag;
import com.querydsl.core.types.Expression;
import com.querydsl.sql.Configuration;

/**
 * Configuration of JdbcBlobstore.
 */
public class JdbcBlobstoreConfiguration {

  /**
   * The mode how the blob is accessed. If <code>null</code>, the mode is automatically guessed from
   * the type of the database.
   */
  public BlobAccessMode blobAccessMode;

  /**
   * Selection expression in SQL queries of the Blob field. If <code>null</code> it is automatically
   * derived based on the database metadata.
   */
  public Expression<Blob> blobSelectionExpression;

  /**
   * Expression that generates the empty blob. If <code>null</code> the empty blob expression is
   * automatically guessed from the type of the database.
   */
  public Expression<Blob> emptyBlobExpression;

  /**
   * Configuration of queryDsl to construct the SQL queries. If null, it is guessed based on the
   * metadata of the database connection.
   */
  public Configuration querydslConfiguration;

  /**
   * An optional query flag that can extend blob non-for-update blob selections. This might be
   * necessary for databases where a row-level read lock must be applied to blobs when they are
   * selected.
   */
  public QueryFlag lockBlobForShareQueryFlag;

  /**
   * Whether calling update SQL after manipulating the Blob instance is necessary or not.
   */
  public Boolean updateSQLAfterBlobContentManipulation;
}
