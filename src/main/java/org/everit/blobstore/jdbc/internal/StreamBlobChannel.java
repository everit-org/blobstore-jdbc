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

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.sql.Blob;
import java.sql.SQLException;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.Expressions;

/**
 * {@link BlobChannel} implementation that uses {@link Blob#getBinaryStream(long, long)} and
 * {@link Blob#setBinaryStream(long)} functions to read and write data. The streams are only closed
 * when necessary (if seek happened, write or read replaced the previous operation or the channel is
 * closed). For reading, the whole lenght of the blob is always passed.
 */
public class StreamBlobChannel implements BlobChannel {

  protected final Blob blob;

  protected InputStream inputStream;

  protected OutputStream outputStream;

  protected long streamPosition;

  public StreamBlobChannel(final Blob blob) {
    this.blob = blob;
  }

  @Override
  public void close() {
    Closeable closeable = null;
    if (outputStream != null) {
      closeable = outputStream;
      outputStream = null;
    } else if (inputStream != null) {
      closeable = inputStream;
      inputStream = null;
    }
    closeCloseable(closeable);
  }

  private void closeCloseable(final Closeable closeable) {
    if (closeable != null) {
      try {
        closeable.close();
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }
  }

  @Override
  public Expression<Blob> getBlobExpression() {
    return Expressions.constant(blob);
  }

  @Override
  public long getBlobSize() {
    if (outputStream != null) {
      close();
    }
    try {
      return blob.length();
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      throw new RuntimeException(e);
    }
  }

  @Override
  public int read(final long position, final byte[] buffer, final int offset, final int length) {
    if (streamPosition != position || outputStream != null) {
      close();
    }
    if (inputStream == null) {
      try {
        inputStream = blob.getBinaryStream(position + 1, blob.length() - position);
      } catch (SQLException e) {
        // TODO Auto-generated catch block
        throw new RuntimeException(e);
      }
    }
    try {
      int readBytes = inputStream.read(buffer, offset, length);
      streamPosition = position + readBytes;
      return readBytes;
    } catch (IOException e) {
      // TODO Auto-generated catch block
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void truncate(final long length) {
    close();
    try {
      blob.truncate(length);
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      throw new RuntimeException(e);
    }
  }

  @Override
  public void write(final long position, final byte[] buffer, final int offset, final int length) {
    if (streamPosition != position || inputStream != null) {
      close();
    }
    if (outputStream == null) {
      try {
        outputStream = blob.setBinaryStream(position + 1);
      } catch (SQLException e) {
        // TODO Auto-generated catch block
        throw new RuntimeException(e);
      }
    }

    try {
      outputStream.write(buffer, offset, length);
      streamPosition = position + length;
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }

  }

}
