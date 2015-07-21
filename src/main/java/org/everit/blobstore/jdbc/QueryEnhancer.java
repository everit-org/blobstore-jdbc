package org.everit.blobstore.jdbc;

import com.querydsl.sql.SQLQuery;

/**
 * Implementations of this interface can enhance {@link SQLQuery}s.
 *
 */
public interface QueryEnhancer {

  /**
   * Enhances the query with some logic.
   *
   * @param query
   *          The query to enhance.
   */
  <T> void enhanceQuery(SQLQuery<T> query);
}
