/*
 * Copyright (C) 2026 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.cloud.teleport.v2.spanner.migrations.connection;

import static com.google.common.truth.Truth.assertThat;

import com.google.cloud.teleport.v2.spanner.migrations.shard.Shard;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ConnectionHelperRequestTest {
  @Test
  public void testGetters_returnConstructorArgumentsWithJdbcUrlPrefix() {
    Shard shard = new Shard("id", "host", "5432", "user", "pass", "db", null, null, null);
    List<Shard> shards = ImmutableList.of(shard);
    ConnectionHelperRequest request =
        new ConnectionHelperRequest(
            shards, "key=val", 7, "org.postgresql.Driver", "SELECT 1", "jdbc:postgresql://");

    assertThat(request.getShards()).containsExactlyElementsIn(shards);
    assertThat(request.getConnectionUrlToShardMap())
        .containsExactly("jdbc:postgresql://host:5432/db", shard);
    assertThat(request.getProperties()).isEqualTo("key=val");
    assertThat(request.getMaxConnections()).isEqualTo(7);
    assertThat(request.getDriver()).isEqualTo("org.postgresql.Driver");
    assertThat(request.getConnectionInitQuery()).isEqualTo("SELECT 1");
    assertThat(request.getJdbcUrlPrefix()).isEqualTo("jdbc:postgresql://");
  }

  @Test
  public void testCreateDefaultConnectionUrl() {
    Shard shard = new Shard("id", "host", "5432", "user", "pass", "db", null, null, null);
    String url = ConnectionHelperRequest.createDefaultConnectionUrl(shard, "jdbc:postgresql://");
    assertThat(url).isEqualTo("jdbc:postgresql://host:5432/db");
  }

  @Test
  public void testMapConstructor() {
    Shard shard = new Shard("id", "host", "1433", "user", "pass", "db", null, null, null);
    Map<String, Shard> shardMap =
        ImmutableMap.of("jdbc:sqlserver://host:1433;databaseName=db", shard);
    ConnectionHelperRequest request =
        new ConnectionHelperRequest(
            shardMap,
            "key=val",
            7,
            "com.microsoft.sqlserver.jdbc.SQLServerDriver",
            "SELECT 1",
            null);

    assertThat(request.getConnectionUrlToShardMap()).isSameInstanceAs(shardMap);
    assertThat(request.getShards()).containsExactly(shard);
    assertThat(request.getProperties()).isEqualTo("key=val");
    assertThat(request.getMaxConnections()).isEqualTo(7);
    assertThat(request.getDriver()).isEqualTo("com.microsoft.sqlserver.jdbc.SQLServerDriver");
    assertThat(request.getConnectionInitQuery()).isEqualTo("SELECT 1");
    assertThat(request.getJdbcUrlPrefix()).isNull();
  }

  @Test
  public void testMapConstructorWithJdbcUrlPrefix() {
    Shard shard = new Shard("id", "host", "1433", "user", "pass", "db", null, null, null);
    Map<String, Shard> shardMap =
        ImmutableMap.of("jdbc:sqlserver://host:1433;databaseName=db", shard);
    ConnectionHelperRequest request =
        new ConnectionHelperRequest(
            shardMap,
            "key=val",
            7,
            "com.microsoft.sqlserver.jdbc.SQLServerDriver",
            "SELECT 1",
            "jdbc:sqlserver://");

    assertThat(request.getConnectionUrlToShardMap()).isSameInstanceAs(shardMap);
    assertThat(request.getShards()).containsExactly(shard);
    assertThat(request.getJdbcUrlPrefix()).isEqualTo("jdbc:sqlserver://");
  }

  @Test
  public void testNullShards() {
    ConnectionHelperRequest request =
        new ConnectionHelperRequest(
            (List<Shard>) null,
            "key=val",
            7,
            "org.postgresql.Driver",
            "SELECT 1",
            "jdbc:postgresql://");

    assertThat(request.getConnectionUrlToShardMap()).isNull();
    assertThat(request.getShards()).isEmpty();
  }
}
