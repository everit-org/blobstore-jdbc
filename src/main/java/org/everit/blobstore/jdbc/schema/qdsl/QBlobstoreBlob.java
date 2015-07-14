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

  public static final QBlobstoreBlob blobstoreBlob = new QBlobstoreBlob("blobstore_blob");

  private static final long serialVersionUID = -447041485;

  public final NumberPath<Long> blobId = createNumber("blobId", Long.class);

  public final com.querydsl.sql.PrimaryKey<QBlobstoreBlob> blobstoreBlobPk =
      createPrimaryKey(blobId);

  public final SimplePath<Blob> data_ = createSimple("data_", Blob.class);

  public final NumberPath<Long> version_ = createNumber("version_", Long.class);

  public QBlobstoreBlob(final Path<? extends QBlobstoreBlob> path) {
    super(path.getType(), path.getMetadata(), "public", "blobstore_blob");
    addMetadata();
  }

  public QBlobstoreBlob(final PathMetadata metadata) {
    super(QBlobstoreBlob.class, metadata, "public", "blobstore_blob");
    addMetadata();
  }

  public QBlobstoreBlob(final String variable) {
    super(QBlobstoreBlob.class, forVariable(variable), "public", "blobstore_blob");
    addMetadata();
  }

  public QBlobstoreBlob(final String variable, final String schema, final String table) {
    super(QBlobstoreBlob.class, forVariable(variable), schema, table);
    addMetadata();
  }

  public void addMetadata() {
    addMetadata(blobId,
        ColumnMetadata.named("blob_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
    addMetadata(data_, ColumnMetadata.named("data_").withIndex(3).ofType(Types.BINARY)
        .withSize(2147483647).notNull());
    addMetadata(version_,
        ColumnMetadata.named("version_").withIndex(2).ofType(Types.BIGINT).withSize(19).notNull());
  }

}
