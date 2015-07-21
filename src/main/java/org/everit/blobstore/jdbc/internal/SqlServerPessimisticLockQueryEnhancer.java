package org.everit.blobstore.jdbc.internal;

import org.everit.blobstore.jdbc.QueryEnhancer;

import com.querydsl.core.JoinFlag.Position;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.mssql.SQLServerTableHints;

/**
 * Enhances SQLServer related queries to use pessimistic locking.
 */
public class SqlServerPessimisticLockQueryEnhancer implements QueryEnhancer {

  public static final SqlServerPessimisticLockQueryEnhancer INSTANCE =
      new SqlServerPessimisticLockQueryEnhancer();

  @Override
  public <T> void enhanceQuery(final SQLQuery<T> query) {
    query.addJoinFlag(" with (" + SQLServerTableHints.UPDLOCK.name() + ")", Position.END);
  }
}
