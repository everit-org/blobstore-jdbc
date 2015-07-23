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

import java.sql.Blob;
import java.util.HashMap;
import java.util.Map;

import org.everit.blobstore.jdbc.QueryEnhancer;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.sql.DerbyTemplates;
import com.querydsl.sql.HSQLDBTemplates;
import com.querydsl.sql.MySQLTemplates;
import com.querydsl.sql.OracleTemplates;
import com.querydsl.sql.PostgreSQLTemplates;
import com.querydsl.sql.SQLExpressions;
import com.querydsl.sql.SQLServerTemplates;
import com.querydsl.sql.SQLTemplates;

/**
 * Default configurations for the supported database types.
 */
public final class JdbcBlobstoreConfigurationUtil {

  private static final Map<DatabaseTypeEnum, Expression<Blob>> DEFAULT_EMPTY_BLOB_EXPRESSIONS =
      new HashMap<>();

  private static final Map<DatabaseTypeEnum, SQLTemplates> DEFAULT_SQL_TEMPLATES = new HashMap<>();

  private static final Map<DatabaseTypeEnum, Boolean> DEFAULT_UPDATE_SQL_FOR_MODIFIED_BLOB_CONTENT_NECESSARY =
      new HashMap<>();

  static {
    Expression<Blob> readOnlyBlobConstant = Expressions.constant(new EmptyReadOnlyBlob());

    DEFAULT_EMPTY_BLOB_EXPRESSIONS.put(DatabaseTypeEnum.DERBY, readOnlyBlobConstant);
    DEFAULT_EMPTY_BLOB_EXPRESSIONS.put(DatabaseTypeEnum.HSQLDB, readOnlyBlobConstant);
    DEFAULT_EMPTY_BLOB_EXPRESSIONS.put(DatabaseTypeEnum.MYSQL, readOnlyBlobConstant);
    DEFAULT_EMPTY_BLOB_EXPRESSIONS.put(DatabaseTypeEnum.POSTGRESQL, readOnlyBlobConstant);
    DEFAULT_EMPTY_BLOB_EXPRESSIONS.put(DatabaseTypeEnum.SQLSERVER, readOnlyBlobConstant);
    DEFAULT_EMPTY_BLOB_EXPRESSIONS.put(DatabaseTypeEnum.UNKNOWN, readOnlyBlobConstant);

    DEFAULT_EMPTY_BLOB_EXPRESSIONS.put(DatabaseTypeEnum.ORACLE,
        SQLExpressions.relationalFunctionCall(Blob.class, "empty_blob"));

    DEFAULT_SQL_TEMPLATES.put(DatabaseTypeEnum.DERBY, new DerbyTemplates(true));
    DEFAULT_SQL_TEMPLATES.put(DatabaseTypeEnum.HSQLDB, new HSQLDBTemplates(true));
    DEFAULT_SQL_TEMPLATES.put(DatabaseTypeEnum.MYSQL, new MySQLTemplates(true));
    DEFAULT_SQL_TEMPLATES.put(DatabaseTypeEnum.ORACLE, new OracleTemplates(true));
    DEFAULT_SQL_TEMPLATES.put(DatabaseTypeEnum.POSTGRESQL, new PostgreSQLTemplates(true));
    DEFAULT_SQL_TEMPLATES.put(DatabaseTypeEnum.SQLSERVER, new SQLServerTemplates(true));
    DEFAULT_SQL_TEMPLATES.put(DatabaseTypeEnum.UNKNOWN, SQLTemplates.DEFAULT);

    DEFAULT_UPDATE_SQL_FOR_MODIFIED_BLOB_CONTENT_NECESSARY.put(DatabaseTypeEnum.DERBY, true);
    DEFAULT_UPDATE_SQL_FOR_MODIFIED_BLOB_CONTENT_NECESSARY.put(DatabaseTypeEnum.HSQLDB, true);
    DEFAULT_UPDATE_SQL_FOR_MODIFIED_BLOB_CONTENT_NECESSARY.put(DatabaseTypeEnum.MYSQL, true);
    DEFAULT_UPDATE_SQL_FOR_MODIFIED_BLOB_CONTENT_NECESSARY.put(DatabaseTypeEnum.ORACLE, false);
    DEFAULT_UPDATE_SQL_FOR_MODIFIED_BLOB_CONTENT_NECESSARY.put(DatabaseTypeEnum.POSTGRESQL, false);
    DEFAULT_UPDATE_SQL_FOR_MODIFIED_BLOB_CONTENT_NECESSARY.put(DatabaseTypeEnum.SQLSERVER, true);
    DEFAULT_UPDATE_SQL_FOR_MODIFIED_BLOB_CONTENT_NECESSARY.put(DatabaseTypeEnum.UNKNOWN, true);
  }

  public static Expression<Blob> getDefaultEmptyBlobExpressionForDatabase(
      final DatabaseTypeEnum databaseType) {

    return DEFAULT_EMPTY_BLOB_EXPRESSIONS.get(databaseType);
  }

  public static QueryEnhancer getDefaultPessimisticLockQueryEnhancerForDatabase(
      final DatabaseTypeEnum databaseType) {

    if (databaseType == DatabaseTypeEnum.SQLSERVER) {
      return SqlServerPessimisticLockQueryEnhancer.INSTANCE;
    } else {
      return DefaultPessimisticLockQueryEnhancer.INSTANCE;
    }
  }

  public static SQLTemplates getDefaultSQLTemplatesForDatabase(
      final DatabaseTypeEnum databaseType) {
    return DEFAULT_SQL_TEMPLATES.get(databaseType);
  }

  public static boolean getDefaultUpdateSQLForModifiedBlobContentNecessaryForDatabase(
      final DatabaseTypeEnum databaseType) {
    return DEFAULT_UPDATE_SQL_FOR_MODIFIED_BLOB_CONTENT_NECESSARY.get(databaseType);
  }

  private JdbcBlobstoreConfigurationUtil() {
  }
}
