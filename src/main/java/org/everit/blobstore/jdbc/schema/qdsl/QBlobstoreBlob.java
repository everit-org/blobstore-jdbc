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
package org.everit.blobstore.jdbc.schema.qdsl;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

import java.sql.Blob;
import java.sql.Types;

import javax.annotation.Generated;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.SimplePath;
import com.querydsl.sql.ColumnMetadata;

/**
 * QBlobstoreBlob is a Querydsl query type for QBlobstoreBlob.
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QBlobstoreBlob extends com.querydsl.sql.RelationalPathBase<QBlobstoreBlob> {

  public static final QBlobstoreBlob blobstoreBlob = new QBlobstoreBlob("blobstoreBlob");

  private static final long serialVersionUID = -447041485;

  public final SimplePath<Blob> blob_ = createSimple("blob_", Blob.class);

  public final NumberPath<Long> blobId = createNumber("blobId", Long.class);

  public final com.querydsl.sql.PrimaryKey<QBlobstoreBlob> blobstoreBlobPk =
      createPrimaryKey(blobId);

  public final NumberPath<Long> version_ = createNumber("version_", Long.class);

  public QBlobstoreBlob(final Path<? extends QBlobstoreBlob> path) {
    super(path.getType(), path.getMetadata(), "public", "BLOBSTORE_BLOB");
    addMetadata();
  }

  public QBlobstoreBlob(final PathMetadata metadata) {
    super(QBlobstoreBlob.class, metadata, "public", "BLOBSTORE_BLOB");
    addMetadata();
  }

  public QBlobstoreBlob(final String variable) {
    super(QBlobstoreBlob.class, forVariable(variable), "public", "BLOBSTORE_BLOB");
    addMetadata();
  }

  public QBlobstoreBlob(final String variable, final String schema, final String table) {
    super(QBlobstoreBlob.class, forVariable(variable), schema, table);
    addMetadata();
  }

  public void addMetadata() {
    addMetadata(blobId,
        ColumnMetadata.named("BLOB_ID").withIndex(1).ofType(Types.BIGINT).notNull());
    addMetadata(blob_, ColumnMetadata.named("BLOB_").withIndex(3).ofType(Types.BLOB).notNull());
    addMetadata(version_,
        ColumnMetadata.named("VERSION_").withIndex(2).ofType(Types.BIGINT).notNull());
  }

}
