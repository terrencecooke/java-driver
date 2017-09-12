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
package com.datastax.oss.driver.internal.core.metadata.schema.refresh;

import com.datastax.oss.driver.api.core.metadata.schema.KeyspaceMetadata;
import com.datastax.oss.driver.internal.core.metadata.DefaultMetadata;
import com.datastax.oss.driver.internal.core.metadata.MetadataRefresh;
import com.datastax.oss.driver.internal.core.metadata.schema.events.AggregateChangeEvent;
import com.datastax.oss.driver.internal.core.metadata.schema.events.FunctionChangeEvent;
import com.datastax.oss.driver.internal.core.metadata.schema.events.KeyspaceChangeEvent;
import com.datastax.oss.driver.internal.core.metadata.schema.events.TableChangeEvent;
import com.datastax.oss.driver.internal.core.metadata.schema.events.TypeChangeEvent;
import com.datastax.oss.driver.internal.core.metadata.schema.events.ViewChangeEvent;
import com.google.common.collect.Sets;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

abstract class KeyspaceRefresh extends MetadataRefresh {

  protected KeyspaceRefresh(DefaultMetadata current, String logPrefix) {
    super(current, logPrefix);
  }

  protected static boolean shallowEquals(KeyspaceMetadata keyspace1, KeyspaceMetadata keyspace2) {
    return Objects.equals(keyspace1.getName(), keyspace2.getName())
        && keyspace1.isDurableWrites() == keyspace2.isDurableWrites()
        && Objects.equals(keyspace1.getReplication(), keyspace2.getReplication());
  }

  /**
   * Computes the exact set of events to emit when a keyspace has changed.
   *
   * <p>We can't simply emit {@link KeyspaceChangeEvent#updated(KeyspaceMetadata, KeyspaceMetadata)}
   * because this method might be called as part of a full schema refresh, or a keyspace refresh
   * initiated by coalesced child element refreshes. We need to traverse all children to check what
   * has exactly changed.
   */
  protected void computeEvents(KeyspaceMetadata oldKeyspace, KeyspaceMetadata newKeyspace) {
    if (oldKeyspace == null) {
      events.add(KeyspaceChangeEvent.created(newKeyspace));
    } else {
      if (!shallowEquals(oldKeyspace, newKeyspace)) {
        events.add(KeyspaceChangeEvent.updated(oldKeyspace, newKeyspace));
      }
      computeChildEvents(oldKeyspace, newKeyspace);
    }
  }

  private void computeChildEvents(KeyspaceMetadata oldKeyspace, KeyspaceMetadata newKeyspace) {
    computeChildEvents(
        oldKeyspace.getTables(),
        newKeyspace.getTables(),
        TableChangeEvent::dropped,
        TableChangeEvent::created,
        TableChangeEvent::updated);
    computeChildEvents(
        oldKeyspace.getViews(),
        newKeyspace.getViews(),
        ViewChangeEvent::dropped,
        ViewChangeEvent::created,
        ViewChangeEvent::updated);
    computeChildEvents(
        oldKeyspace.getUserDefinedTypes(),
        newKeyspace.getUserDefinedTypes(),
        TypeChangeEvent::dropped,
        TypeChangeEvent::created,
        TypeChangeEvent::updated);
    computeChildEvents(
        oldKeyspace.getFunctions(),
        newKeyspace.getFunctions(),
        FunctionChangeEvent::dropped,
        FunctionChangeEvent::created,
        FunctionChangeEvent::updated);
    computeChildEvents(
        oldKeyspace.getAggregates(),
        newKeyspace.getAggregates(),
        AggregateChangeEvent::dropped,
        AggregateChangeEvent::created,
        AggregateChangeEvent::updated);
  }

  private <K, V> void computeChildEvents(
      Map<K, V> oldChildren,
      Map<K, V> newChildren,
      Function<V, Object> newDroppedEvent,
      Function<V, Object> newCreatedEvent,
      BiFunction<V, V, Object> newUpdatedEvent) {
    for (K removedKey : Sets.difference(oldChildren.keySet(), newChildren.keySet())) {
      events.add(newDroppedEvent.apply(oldChildren.get(removedKey)));
    }
    for (Map.Entry<K, V> entry : newChildren.entrySet()) {
      K key = entry.getKey();
      V newChild = entry.getValue();
      V oldChild = oldChildren.get(key);
      if (oldChild == null) {
        events.add(newCreatedEvent.apply(newChild));
      } else if (!oldChild.equals(newChild)) {
        events.add(newUpdatedEvent.apply(oldChild, newChild));
      }
    }
  }
}
