package org.everit.blobstore.jdbc.internal;

import org.everit.blobstore.jdbc.QueryEnhancer;

import com.querydsl.sql.SQLQuery;

/**
 * Query enhancer for all databases except SQLServer.
 */
public class DefaultPessimisticLockQueryEnhancer implements QueryEnhancer {

  public static final DefaultPessimisticLockQueryEnhancer INSTANCE =
      new DefaultPessimisticLockQueryEnhancer();

  @Override
  public <T> void enhanceQuery(final SQLQuery<T> query) {
    query.forUpdate();
  }
}
