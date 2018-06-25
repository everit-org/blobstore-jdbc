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

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QBlobstoreBlob is a Querydsl query type for QBlobstoreBlob
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QBlobstoreBlob extends com.querydsl.sql.RelationalPathBase<QBlobstoreBlob> {

    private static final long serialVersionUID = -447041485;

    public static final QBlobstoreBlob blobstoreBlob = new QBlobstoreBlob("blobstore_blob");

    public class PrimaryKeys {

        public final com.querydsl.sql.PrimaryKey<QBlobstoreBlob> blobPk = createPrimaryKey(blobId);

    }

    public final SimplePath<java.sql.Blob> blob = createSimple("blob", java.sql.Blob.class);

    public final NumberPath<Long> blobId = createNumber("blobId", Long.class);

    public final NumberPath<Long> version = createNumber("version", Long.class);

    public final PrimaryKeys pk = new PrimaryKeys();

    public QBlobstoreBlob(String variable) {
        super(QBlobstoreBlob.class, forVariable(variable), "org.everit.blobstore.jdbc", "blobstore_blob");
        addMetadata();
    }

    public QBlobstoreBlob(String variable, String schema, String table) {
        super(QBlobstoreBlob.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QBlobstoreBlob(String variable, String schema) {
        super(QBlobstoreBlob.class, forVariable(variable), schema, "blobstore_blob");
        addMetadata();
    }

    public QBlobstoreBlob(Path<? extends QBlobstoreBlob> path) {
        super(path.getType(), path.getMetadata(), "org.everit.blobstore.jdbc", "blobstore_blob");
        addMetadata();
    }

    public QBlobstoreBlob(PathMetadata metadata) {
        super(QBlobstoreBlob.class, metadata, "org.everit.blobstore.jdbc", "blobstore_blob");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(blob, ColumnMetadata.named("blob_").withIndex(3).ofType(Types.BLOB).withSize(2147483647).notNull());
        addMetadata(blobId, ColumnMetadata.named("blob_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(version, ColumnMetadata.named("version_").withIndex(2).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

