package org.everit.blobstore.jdbc.internal;

import java.sql.Blob;

/**
 * Holder class to get version and blob together from the database.
 *
 */
public class BlobAndVersion {

  public final Blob blob;

  public final long version;

  public BlobAndVersion(final Blob blob, final long version) {
    this.blob = blob;
    this.version = version;
  }

}
