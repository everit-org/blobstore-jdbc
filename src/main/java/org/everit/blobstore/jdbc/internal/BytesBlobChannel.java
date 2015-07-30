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
import java.sql.SQLException;

/**
 * {@link BlobChannel} that uses {@link java.sql.Blob#getBytes(long, int)} and
 * {@link java.sql.Blob#setBytes(long, byte[], int, int)} to read and write blobs. *
 */
public class BytesBlobChannel implements BlobChannel {

  protected final Blob blob;

  public BytesBlobChannel(final Blob blob) {
    this.blob = blob;
  }

  @Override
  public void close() {
    // Do nothing
  }

  @Override
  public Blob getBlob() {
    return blob;
  }

  @Override
  public int read(final long position, final byte[] buffer, final int offset, final int length) {
    try {
      byte[] bytes = blob.getBytes(position + 1, length);
      int readByteNum = bytes.length;
      System.arraycopy(bytes, 0, buffer, offset, readByteNum);
      return readByteNum;
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      throw new RuntimeException(e);
    }
  }

  @Override
  public void write(final long position, final byte[] buffer, final int offset, final int length) {
    try {
      blob.setBytes(position + 1, buffer, offset, length);
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      throw new RuntimeException(e);
    }
  }
}
