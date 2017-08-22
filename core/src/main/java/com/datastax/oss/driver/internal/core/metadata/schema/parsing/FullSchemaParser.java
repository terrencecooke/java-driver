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
package com.datastax.oss.driver.internal.core.metadata.schema.parsing;

import com.datastax.oss.driver.api.core.metadata.schema.KeyspaceMetadata;
import com.datastax.oss.driver.internal.core.adminrequest.AdminRow;
import com.datastax.oss.driver.internal.core.metadata.MetadataRefresh;
import com.datastax.oss.driver.internal.core.metadata.schema.refresh.FullSchemaRefresh;
import com.google.common.collect.ImmutableList;

class FullSchemaParser extends SchemaElementParser {

  private final KeyspaceParser keyspaceParser;

  FullSchemaParser(SchemaParser parent) {
    super(parent);
    this.keyspaceParser = new KeyspaceParser(parent);
  }

  MetadataRefresh parse() {
    ImmutableList.Builder<KeyspaceMetadata> keyspacesBuilder = ImmutableList.builder();
    for (AdminRow row : rows.keyspaces) {
      keyspacesBuilder.add(keyspaceParser.parseKeyspace(row));
    }
    return new FullSchemaRefresh(currentMetadata, keyspacesBuilder.build(), logPrefix);
  }
}
