# Flink Connector Redis

The Redis connector allows for reading data from and writing data into Redis.

## How to create a Redis Table

### string

```sql
CREATE TABLE RedisTable (
  `key`   STRING,
  `value` STRING
) WITH (
  'connector' = 'redis',
  'redis.host' = 'localhost',
  'redis.port' = '6379',
  'redis.data-type' = 'string',
  'redis.key-pattern' = '*',
)
```

### list/set

```sql
CREATE TABLE RedisTable (
  `key`   STRING,
  `value` ARRAY<STRING>
) WITH (
  'connector' = 'redis',
  'redis.host' = 'localhost',
  'redis.port' = '6379',
  'redis.data-type' = 'list', -- or 'set'
  'redis.key-pattern' = '*',
)
```

### sorted set

```sql
CREATE TABLE RedisTable (
  `key`   STRING,
  `value` ARRAY<ROW<score DOUBLE, elem STRING>>
) WITH (
  'connector' = 'redis',
  'redis.host' = 'localhost',
  'redis.port' = '6379',
  'redis.data-type' = 'sorted_set',
  'redis.key-pattern' = '*',
)
```

or

```sql
CREATE TABLE RedisTable (
  `key`   STRING,
  `score` DOUBLE,
  `elem`  STRING
) WITH (
  'connector' = 'redis',
  'redis.host' = 'localhost',
  'redis.port' = '6379',
  'redis.data-type' = 'sorted_set',
  'redis.key-pattern' = '*',
)
```

### hash

```sql
CREATE TABLE RedisTable (
  `key`   STRING,
  `value` MAP<STRING, STRING>
) WITH (
  'connector' = 'redis',
  'redis.host' = 'localhost',
  'redis.port' = '6379',
  'redis.data-type' = 'hash',
  'redis.key-pattern' = '*',
)
```

or

```sql
CREATE TABLE RedisTable (
  `key`   STRING,
  `field` STRING,
  `value` STRING
) WITH (
  'connector' = 'redis',
  'redis.host' = 'localhost',
  'redis.port' = '6379',
  'redis.data-type' = 'hash',
  'redis.key-pattern' = '*',
)
```

### pubsub

publish:

```sql
CREATE TABLE RedisTable_publish (
  `id`   INT,
  `name` STRING,
  `age`  INT
) WITH (
  'connector' = 'redis',
  'redis.host' = 'localhost',
  'redis.port' = '6379',
  'redis.data-type' = 'pubsub',
  'redis.pubsub.publish-channel' = 'test',
  'format' = 'csv'
)
```

subscribe:

```sql
CREATE TABLE RedisTable_publish (
  `id`   INT,
  `name` STRING,
  `age`  INT
) WITH (
  'connector' = 'redis',
  'redis.host' = 'localhost',
  'redis.port' = '6379',
  'redis.data-type' = 'pubsub',
  'redis.pubsub.subscribe-patterns' = 'test',
  'format' = 'csv'
)
```

## Connector Options

Basic options to using flink-connector-redis:

| Option                          | Required | Default | Type   | Description                                                  |
| :------------------------------ | :------- | :------ | :----- | :----------------------------------------------------------- |
| connector                       | required | (none)  | String | Specify what connector to use, for Redis use: 'redis'.       |
| redis.deploy-mode               | required | single  | enum   | Deploy mode for Redis, other options: 'sentinel', 'cluster', 'sharded'. |
| redis.host                      | optional | (none)  | String | Required if redis in `single` mode.                          |
| redis.port                      | optional | (none)  | String | Required if redis in `single` mode.                          |
| redis.master                    | optional | (none)  | String | Required if redis in `sentinel` mode.                        |
| redis.masters                   | optional | (none)  | String | Required if redis in `sharded` mode.                         |
| redis.sentinels                 | optional | (none)  | String | Required if redis in `sentinel`/`sharded` mode. Comma separated list for redis sentinel. |
| redis.host-and-ports            | optional | (none)  | String | Required if redis in `cluster` mode. Comma separated list for redis cluster HostAndPort. |
| redis.user                      | optional | (none)  | String | Required if Redis set user string.                           |
| redis.password                  | optional | (none)  | String | Required if Redis set password string.                       |
| redis.data-type                 | required | (none)  | enum   | Data type for Redis. Possible options include: string, list, set, sorted_set, hash, pubsub. |
| redis.key-pattern               | optional | (none)  | String | Required if scan Redis.                                      |
| redis.pubsub.subscribe-patterns | optional | (none)  | String | Subscribe patterns for Redis PUBSUB.                         |
| redis.pubsub.publish-channel    | optional | (none)  | String | Publish channel for Redis PUBSUB.                            |

Lookup options for flink-connector-redis:

| Option                | Required | Default | Type     | Description                                                  |
| :-------------------- | :------- | :------ | :------- | :----------------------------------------------------------- |
| lookup.cache.max-rows | required | -1      | Long     | the max number of rows of lookup cache, over this value, the oldest rows will be eliminated. 'cache.max-rows' and 'cache.ttl' options must all be specified if any of them is specified. Cache is not enabled as default. |
| lookup.cache.ttl      | required | 10s     | Duration | the cache time to live.                                      |
| lookup.async          | required | false   | Boolean  | whether to use asynchronous.                                 |

Advanced options for flink-connector-redis:

|Option | Required | Default|Type | Description |
|:-----|:---------|:-------|:-----|:------------|
|redis.ignore-error|required|false|Boolean|Ignore error when redis query/insert.|
|redis.batch-size|optional|1|Integer|Batch of Redis sink.|
|redis.linger-ms|optional|200|Long|Max delay to send a Batch of Redis sink.|
|redis.client.timeout|required|30s|Duration|Required Redis client timeout duration.|
|redis.pool.maxWaitMillis|required|10000|Long|Required Redis sentinel pool max wait millis.|
|redis.pool.testWhileIdle|required|false|Boolean|Required Redis connection pool test while idle.|
|redis.pool.timeBetweenEvictionRunsMillis|required|30000|Long|Required Redis connection pool time between eviction runs millis.|
|redis.pool.numTestsPerEvictionRun|required|-1|Integer|Required Redis connection pool num tests per eviction run.|
|redis.pool.minEvictableIdleTimeMillis|required|60000|Long|Required Redis connection pool min evictable idle time millis.|
|redis.pool.maxTotal|required| 2       |Integer|Required Redis connection pool max total.|
|redis.pool.maxIdle|required| 1       |Integer|Required Redis connection pool max idle.|
|redis.pool.minIdle|required|1|Integer|Required Redis connection pool min idle.|
|redis.pool.testOnBorrow|required|true|Boolean|Required Redis connection pool test on borrow.|
|redis.pool.testOnReturn|required|true|Boolean|Required Redis connection pool test on return.|

## UDF

We also provide some udf, this is the example:

```java
public static void main(String[] args) throws Exception {
  StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment(new Configuration());
  StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);

  String host = "localhost";
  String port = "6379";
  
  final Properties properties = new Properties();
  properties.put(REDIS_HOST.key(), host);
  properties.put(REDIS_PORT.key(), port);
  final RedisOptions options = new RedisOptions(properties);

  FlinkRedisUdf.registerTo(tableEnv, options);

  tableEnv.executeSql("select redis_get('foo')").print();
  /* 
     +----+--------+
     | op | EXPR$0 |
     +----+--------+
     | +I |    bar |
     +----+--------+
     1 row in set
  */
}
```

## Data Type Mapping

Redis stores message keys and values as bytes/string. Generally, the data type of Redis is bytes/string. The Redis messages also can be deserialized and serialized by formats, e.g. csv, json, avro. Thus, the data type mapping is determined by specific formats. Please refer to [Formats](https://ci.apache.org/projects/flink/flink-docs-release-1.12/zh/dev/table/connectors/formats/) pages for more details.
