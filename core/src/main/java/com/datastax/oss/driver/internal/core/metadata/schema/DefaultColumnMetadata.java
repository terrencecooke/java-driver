/*
 * Copyright (C) 2017-2017 DataStax Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.datastax.oss.driver.internal.core.metadata.schema;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.metadata.schema.ColumnMetadata;
import com.datastax.oss.driver.api.core.type.DataType;

public class DefaultColumnMetadata implements ColumnMetadata {
  private final CqlIdentifier keyspace;
  private final CqlIdentifier parent;
  private final CqlIdentifier name;
  private final DataType dataType;
  private final boolean isStatic;

  public DefaultColumnMetadata(
      CqlIdentifier keyspace,
      CqlIdentifier parent,
      CqlIdentifier name,
      DataType dataType,
      boolean isStatic) {
    this.keyspace = keyspace;
    this.parent = parent;
    this.name = name;
    this.dataType = dataType;
    this.isStatic = isStatic;
  }

  @Override
  public CqlIdentifier getKeyspace() {
    return keyspace;
  }

  @Override
  public CqlIdentifier getParent() {
    return parent;
  }

  @Override
  public CqlIdentifier getName() {
    return name;
  }

  @Override
  public DataType getType() {
    return dataType;
  }

  @Override
  public boolean isStatic() {
    return isStatic;
  }
}
