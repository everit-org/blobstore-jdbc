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
import java.sql.Blob;

/**
 * Channel to access {@link Blob}s for read and write.
 */
public interface BlobChannel extends Closeable {

  @Override
  void close();

  Blob getBlob();

  int read(long position, byte[] buffer, int offset, int length);

  void write(long position, byte[] buffer, int offset, int length);
}
