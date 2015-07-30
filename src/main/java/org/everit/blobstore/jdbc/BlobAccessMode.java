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
package org.everit.blobstore.jdbc;

/**
 * The mode how {@link java.sql.Blob} instances should be accessed.
 */
public enum BlobAccessMode {

  /**
   * {@link java.sql.Blob#setBytes(long, byte[], int, int)} and
   * {@link java.sql.Blob#getBytes(long, int)} are called inside.
   */
  BYTES,

  /**
   * {@link java.sql.Blob#setBinaryStream(long)} and
   * {@link java.sql.Blob#getBinaryStream(long, long)} are used. In case of calling
   * {@link java.sql.Blob#getBinaryStream(long, long)} the full remaining size of the blob is passed
   * as the length that would be read. This can be an issue in case of databases that read the full
   * requested length into memory.
   */
  STREAM
}
