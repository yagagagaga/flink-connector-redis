package org.apache.flink.connector.redis.client;

import redis.clients.jedis.BinaryJedisPubSub;
import redis.clients.jedis.BitPosParams;
import redis.clients.jedis.GeoCoordinate;
import redis.clients.jedis.GeoRadiusResponse;
import redis.clients.jedis.GeoUnit;
import redis.clients.jedis.ListPosition;
import redis.clients.jedis.PipelineBase;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;
import redis.clients.jedis.SortingParams;
import redis.clients.jedis.StreamConsumersInfo;
import redis.clients.jedis.StreamEntry;
import redis.clients.jedis.StreamEntryID;
import redis.clients.jedis.StreamGroupInfo;
import redis.clients.jedis.StreamInfo;
import redis.clients.jedis.StreamPendingEntry;
import redis.clients.jedis.Tuple;
import redis.clients.jedis.commands.BinaryJedisCommands;
import redis.clients.jedis.commands.JedisCommands;
import redis.clients.jedis.params.GeoRadiusParam;
import redis.clients.jedis.params.LPosParams;
import redis.clients.jedis.params.SetParams;
import redis.clients.jedis.params.ZAddParams;
import redis.clients.jedis.params.ZIncrByParams;

import java.io.Closeable;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The container for all available Redis commands.
 */
@SuppressWarnings("all")
public interface RedisClientProxy extends Serializable, Closeable, JedisCommands, BinaryJedisCommands {

	RedisClientProxy BLACK_HOLE = new RedisBlackHoleProxy();

	PipelineBase pipelined();

	ScanResult<byte[]> scan(final byte[] cursor, final ScanParams params);

	void psubscribe(BinaryJedisPubSub jedisPubSub, final byte[]... patterns);

	@Override
	void close() throws IOException;

	@Override
	default String set(byte[] key, byte[] value) {
		return null;
	}

	@Override
	default String set(byte[] key, byte[] value, SetParams params) {
		return null;
	}

	@Override
	default byte[] get(byte[] key) {
		return new byte[0];
	}

	@Override
	default Boolean exists(byte[] key) {
		return null;
	}

	@Override
	default Long persist(byte[] key) {
		return null;
	}

	@Override
	default String type(byte[] key) {
		return null;
	}

	@Override
	default byte[] dump(byte[] key) {
		return new byte[0];
	}

	@Override
	default String restore(byte[] key, int ttl, byte[] serializedValue) {
		return null;
	}

	@Override
	default String restoreReplace(byte[] key, int ttl, byte[] serializedValue) {
		return null;
	}

	@Override
	default Long expire(byte[] key, int seconds) {
		return null;
	}

	@Override
	default Long pexpire(byte[] key, long milliseconds) {
		return null;
	}

	@Override
	default Long expireAt(byte[] key, long unixTime) {
		return null;
	}

	@Override
	default Long pexpireAt(byte[] key, long millisecondsTimestamp) {
		return null;
	}

	@Override
	default Long ttl(byte[] key) {
		return null;
	}

	@Override
	default Long pttl(byte[] key) {
		return null;
	}

	@Override
	default Long touch(byte[] key) {
		return null;
	}

	@Override
	default Boolean setbit(byte[] key, long offset, boolean value) {
		return null;
	}

	@Override
	default Boolean setbit(byte[] key, long offset, byte[] value) {
		return null;
	}

	@Override
	default Boolean getbit(byte[] key, long offset) {
		return null;
	}

	@Override
	default Long setrange(byte[] key, long offset, byte[] value) {
		return null;
	}

	@Override
	default byte[] getrange(byte[] key, long startOffset, long endOffset) {
		return new byte[0];
	}

	@Override
	default byte[] getSet(byte[] key, byte[] value) {
		return new byte[0];
	}

	@Override
	default Long setnx(byte[] key, byte[] value) {
		return null;
	}

	@Override
	default String setex(byte[] key, int seconds, byte[] value) {
		return null;
	}

	@Override
	default String psetex(byte[] key, long milliseconds, byte[] value) {
		return null;
	}

	@Override
	default Long decrBy(byte[] key, long decrement) {
		return null;
	}

	@Override
	default Long decr(byte[] key) {
		return null;
	}

	@Override
	default Long incrBy(byte[] key, long increment) {
		return null;
	}

	@Override
	default Double incrByFloat(byte[] key, double increment) {
		return null;
	}

	@Override
	default Long incr(byte[] key) {
		return null;
	}

	@Override
	default Long append(byte[] key, byte[] value) {
		return null;
	}

	@Override
	default byte[] substr(byte[] key, int start, int end) {
		return new byte[0];
	}

	@Override
	default Long hset(byte[] key, byte[] field, byte[] value) {
		return null;
	}

	@Override
	default Long hset(byte[] key, Map<byte[], byte[]> hash) {
		return null;
	}

	@Override
	default byte[] hget(byte[] key, byte[] field) {
		return new byte[0];
	}

	@Override
	default Long hsetnx(byte[] key, byte[] field, byte[] value) {
		return null;
	}

	@Override
	default String hmset(byte[] key, Map<byte[], byte[]> hash) {
		return null;
	}

	@Override
	default List<byte[]> hmget(byte[] key, byte[]... fields) {
		return null;
	}

	@Override
	default Long hincrBy(byte[] key, byte[] field, long value) {
		return null;
	}

	@Override
	default Double hincrByFloat(byte[] key, byte[] field, double value) {
		return null;
	}

	@Override
	default Boolean hexists(byte[] key, byte[] field) {
		return null;
	}

	@Override
	default Long hdel(byte[] key, byte[]... field) {
		return null;
	}

	@Override
	default Long hlen(byte[] key) {
		return null;
	}

	@Override
	default Set<byte[]> hkeys(byte[] key) {
		return null;
	}

	@Override
	default List<byte[]> hvals(byte[] key) {
		return null;
	}

	@Override
	default Map<byte[], byte[]> hgetAll(byte[] key) {
		return null;
	}

	@Override
	default Long rpush(byte[] key, byte[]... args) {
		return null;
	}

	@Override
	default Long lpush(byte[] key, byte[]... args) {
		return null;
	}

	@Override
	default Long llen(byte[] key) {
		return null;
	}

	@Override
	default List<byte[]> lrange(byte[] key, long start, long stop) {
		return null;
	}

	@Override
	default String ltrim(byte[] key, long start, long stop) {
		return null;
	}

	@Override
	default byte[] lindex(byte[] key, long index) {
		return new byte[0];
	}

	@Override
	default String lset(byte[] key, long index, byte[] value) {
		return null;
	}

	@Override
	default Long lrem(byte[] key, long count, byte[] value) {
		return null;
	}

	@Override
	default byte[] lpop(byte[] key) {
		return new byte[0];
	}

	@Override
	default List<byte[]> lpop(byte[] key, int count) {
		return null;
	}

	@Override
	default Long lpos(byte[] key, byte[] element) {
		return null;
	}

	@Override
	default Long lpos(byte[] key, byte[] element, LPosParams params) {
		return null;
	}

	@Override
	default List<Long> lpos(byte[] key, byte[] element, LPosParams params, long count) {
		return null;
	}

	@Override
	default byte[] rpop(byte[] key) {
		return new byte[0];
	}

	@Override
	default List<byte[]> rpop(byte[] key, int count) {
		return null;
	}

	@Override
	default Long sadd(byte[] key, byte[]... member) {
		return null;
	}

	@Override
	default Set<byte[]> smembers(byte[] key) {
		return null;
	}

	@Override
	default Long srem(byte[] key, byte[]... member) {
		return null;
	}

	@Override
	default byte[] spop(byte[] key) {
		return new byte[0];
	}

	@Override
	default Set<byte[]> spop(byte[] key, long count) {
		return null;
	}

	@Override
	default Long scard(byte[] key) {
		return null;
	}

	@Override
	default Boolean sismember(byte[] key, byte[] member) {
		return null;
	}

	@Override
	default List<Boolean> smismember(byte[] key, byte[]... members) {
		return null;
	}

	@Override
	default byte[] srandmember(byte[] key) {
		return new byte[0];
	}

	@Override
	default List<byte[]> srandmember(byte[] key, int count) {
		return null;
	}

	@Override
	default Long strlen(byte[] key) {
		return null;
	}

	@Override
	default Long zadd(byte[] key, double score, byte[] member) {
		return null;
	}

	@Override
	default Long zadd(byte[] key, double score, byte[] member, ZAddParams params) {
		return null;
	}

	@Override
	default Long zadd(byte[] key, Map<byte[], Double> scoreMembers) {
		return null;
	}

	@Override
	default Long zadd(byte[] key, Map<byte[], Double> scoreMembers, ZAddParams params) {
		return null;
	}

	@Override
	default Set<byte[]> zrange(byte[] key, long start, long stop) {
		return null;
	}

	@Override
	default Long zrem(byte[] key, byte[]... members) {
		return null;
	}

	@Override
	default Double zincrby(byte[] key, double increment, byte[] member) {
		return null;
	}

	@Override
	default Double zincrby(byte[] key, double increment, byte[] member, ZIncrByParams params) {
		return null;
	}

	@Override
	default Long zrank(byte[] key, byte[] member) {
		return null;
	}

	@Override
	default Long zrevrank(byte[] key, byte[] member) {
		return null;
	}

	@Override
	default Set<byte[]> zrevrange(byte[] key, long start, long stop) {
		return null;
	}

	@Override
	default Set<Tuple> zrangeWithScores(byte[] key, long start, long stop) {
		return null;
	}

	@Override
	default Set<Tuple> zrevrangeWithScores(byte[] key, long start, long stop) {
		return null;
	}

	@Override
	default Long zcard(byte[] key) {
		return null;
	}

	@Override
	default Double zscore(byte[] key, byte[] member) {
		return null;
	}

	@Override
	default List<Double> zmscore(byte[] key, byte[]... members) {
		return null;
	}

	@Override
	default Tuple zpopmax(byte[] key) {
		return null;
	}

	@Override
	default Set<Tuple> zpopmax(byte[] key, int count) {
		return null;
	}

	@Override
	default Tuple zpopmin(byte[] key) {
		return null;
	}

	@Override
	default Set<Tuple> zpopmin(byte[] key, int count) {
		return null;
	}

	@Override
	default List<byte[]> sort(byte[] key) {
		return null;
	}

	@Override
	default List<byte[]> sort(byte[] key, SortingParams sortingParameters) {
		return null;
	}

	@Override
	default Long zcount(byte[] key, double min, double max) {
		return null;
	}

	@Override
	default Long zcount(byte[] key, byte[] min, byte[] max) {
		return null;
	}

	@Override
	default Set<byte[]> zrangeByScore(byte[] key, double min, double max) {
		return null;
	}

	@Override
	default Set<byte[]> zrangeByScore(byte[] key, byte[] min, byte[] max) {
		return null;
	}

	@Override
	default Set<byte[]> zrevrangeByScore(byte[] key, double max, double min) {
		return null;
	}

	@Override
	default Set<byte[]> zrangeByScore(byte[] key, double min, double max, int offset, int count) {
		return null;
	}

	@Override
	default Set<byte[]> zrevrangeByScore(byte[] key, byte[] max, byte[] min) {
		return null;
	}

	@Override
	default Set<byte[]> zrangeByScore(byte[] key, byte[] min, byte[] max, int offset, int count) {
		return null;
	}

	@Override
	default Set<byte[]> zrevrangeByScore(byte[] key, double max, double min, int offset, int count) {
		return null;
	}

	@Override
	default Set<Tuple> zrangeByScoreWithScores(byte[] key, double min, double max) {
		return null;
	}

	@Override
	default Set<Tuple> zrevrangeByScoreWithScores(byte[] key, double max, double min) {
		return null;
	}

	@Override
	default Set<Tuple> zrangeByScoreWithScores(byte[] key, double min, double max, int offset, int count) {
		return null;
	}

	@Override
	default Set<byte[]> zrevrangeByScore(byte[] key, byte[] max, byte[] min, int offset, int count) {
		return null;
	}

	@Override
	default Set<Tuple> zrangeByScoreWithScores(byte[] key, byte[] min, byte[] max) {
		return null;
	}

	@Override
	default Set<Tuple> zrevrangeByScoreWithScores(byte[] key, byte[] max, byte[] min) {
		return null;
	}

	@Override
	default Set<Tuple> zrangeByScoreWithScores(byte[] key, byte[] min, byte[] max, int offset, int count) {
		return null;
	}

	@Override
	default Set<Tuple> zrevrangeByScoreWithScores(byte[] key, double max, double min, int offset, int count) {
		return null;
	}

	@Override
	default Set<Tuple> zrevrangeByScoreWithScores(byte[] key, byte[] max, byte[] min, int offset, int count) {
		return null;
	}

	@Override
	default Long zremrangeByRank(byte[] key, long start, long stop) {
		return null;
	}

	@Override
	default Long zremrangeByScore(byte[] key, double min, double max) {
		return null;
	}

	@Override
	default Long zremrangeByScore(byte[] key, byte[] min, byte[] max) {
		return null;
	}

	@Override
	default Long zlexcount(byte[] key, byte[] min, byte[] max) {
		return null;
	}

	@Override
	default Set<byte[]> zrangeByLex(byte[] key, byte[] min, byte[] max) {
		return null;
	}

	@Override
	default Set<byte[]> zrangeByLex(byte[] key, byte[] min, byte[] max, int offset, int count) {
		return null;
	}

	@Override
	default Set<byte[]> zrevrangeByLex(byte[] key, byte[] max, byte[] min) {
		return null;
	}

	@Override
	default Set<byte[]> zrevrangeByLex(byte[] key, byte[] max, byte[] min, int offset, int count) {
		return null;
	}

	@Override
	default Long zremrangeByLex(byte[] key, byte[] min, byte[] max) {
		return null;
	}

	@Override
	default Long linsert(byte[] key, ListPosition where, byte[] pivot, byte[] value) {
		return null;
	}

	@Override
	default Long lpushx(byte[] key, byte[]... arg) {
		return null;
	}

	@Override
	default Long rpushx(byte[] key, byte[]... arg) {
		return null;
	}

	@Override
	default Long del(byte[] key) {
		return null;
	}

	@Override
	default Long unlink(byte[] key) {
		return null;
	}

	@Override
	default byte[] echo(byte[] arg) {
		return new byte[0];
	}

	@Override
	default Long move(byte[] key, int dbIndex) {
		return null;
	}

	@Override
	default Long bitcount(byte[] key) {
		return null;
	}

	@Override
	default Long bitcount(byte[] key, long start, long end) {
		return null;
	}

	@Override
	default Long pfadd(byte[] key, byte[]... elements) {
		return null;
	}

	@Override
	default long pfcount(byte[] key) {
		return 0;
	}

	@Override
	default Long geoadd(byte[] key, double longitude, double latitude, byte[] member) {
		return null;
	}

	@Override
	default Long geoadd(byte[] key, Map<byte[], GeoCoordinate> memberCoordinateMap) {
		return null;
	}

	@Override
	default Double geodist(byte[] key, byte[] member1, byte[] member2) {
		return null;
	}

	@Override
	default Double geodist(byte[] key, byte[] member1, byte[] member2, GeoUnit unit) {
		return null;
	}

	@Override
	default List<byte[]> geohash(byte[] key, byte[]... members) {
		return null;
	}

	@Override
	default List<GeoCoordinate> geopos(byte[] key, byte[]... members) {
		return null;
	}

	@Override
	default List<GeoRadiusResponse> georadius(byte[] key, double longitude, double latitude, double radius, GeoUnit unit) {
		return null;
	}

	@Override
	default List<GeoRadiusResponse> georadiusReadonly(byte[] key, double longitude, double latitude, double radius, GeoUnit unit) {
		return null;
	}

	@Override
	default List<GeoRadiusResponse> georadius(byte[] key, double longitude, double latitude, double radius, GeoUnit unit, GeoRadiusParam param) {
		return null;
	}

	@Override
	default List<GeoRadiusResponse> georadiusReadonly(byte[] key, double longitude, double latitude, double radius, GeoUnit unit, GeoRadiusParam param) {
		return null;
	}

	@Override
	default List<GeoRadiusResponse> georadiusByMember(byte[] key, byte[] member, double radius, GeoUnit unit) {
		return null;
	}

	@Override
	default List<GeoRadiusResponse> georadiusByMemberReadonly(byte[] key, byte[] member, double radius, GeoUnit unit) {
		return null;
	}

	@Override
	default List<GeoRadiusResponse> georadiusByMember(byte[] key, byte[] member, double radius, GeoUnit unit, GeoRadiusParam param) {
		return null;
	}

	@Override
	default List<GeoRadiusResponse> georadiusByMemberReadonly(byte[] key, byte[] member, double radius, GeoUnit unit, GeoRadiusParam param) {
		return null;
	}

	@Override
	default ScanResult<Map.Entry<byte[], byte[]>> hscan(byte[] key, byte[] cursor) {
		return null;
	}

	@Override
	default ScanResult<Map.Entry<byte[], byte[]>> hscan(byte[] key, byte[] cursor, ScanParams params) {
		return null;
	}

	@Override
	default ScanResult<byte[]> sscan(byte[] key, byte[] cursor) {
		return null;
	}

	@Override
	default ScanResult<byte[]> sscan(byte[] key, byte[] cursor, ScanParams params) {
		return null;
	}

	@Override
	default ScanResult<Tuple> zscan(byte[] key, byte[] cursor) {
		return null;
	}

	@Override
	default ScanResult<Tuple> zscan(byte[] key, byte[] cursor, ScanParams params) {
		return null;
	}

	@Override
	default List<Long> bitfield(byte[] key, byte[]... arguments) {
		return null;
	}

	@Override
	default List<Long> bitfieldReadonly(byte[] key, byte[]... arguments) {
		return null;
	}

	@Override
	default Long hstrlen(byte[] key, byte[] field) {
		return null;
	}

	@Override
	default byte[] xadd(byte[] key, byte[] id, Map<byte[], byte[]> hash, long maxLen, boolean approximateLength) {
		return new byte[0];
	}

	@Override
	default Long xlen(byte[] key) {
		return null;
	}

	@Override
	default List<byte[]> xrange(byte[] key, byte[] start, byte[] end, long count) {
		return null;
	}

	@Override
	default List<byte[]> xrevrange(byte[] key, byte[] end, byte[] start, int count) {
		return null;
	}

	@Override
	default Long xack(byte[] key, byte[] group, byte[]... ids) {
		return null;
	}

	@Override
	default String xgroupCreate(byte[] key, byte[] consumer, byte[] id, boolean makeStream) {
		return null;
	}

	@Override
	default String xgroupSetID(byte[] key, byte[] consumer, byte[] id) {
		return null;
	}

	@Override
	default Long xgroupDestroy(byte[] key, byte[] consumer) {
		return null;
	}

	@Override
	default Long xgroupDelConsumer(byte[] key, byte[] consumer, byte[] consumerName) {
		return null;
	}

	@Override
	default Long xdel(byte[] key, byte[]... ids) {
		return null;
	}

	@Override
	default Long xtrim(byte[] key, long maxLen, boolean approximateLength) {
		return null;
	}

	@Override
	default List<byte[]> xpending(byte[] key, byte[] groupname, byte[] start, byte[] end, int count, byte[] consumername) {
		return null;
	}

	@Override
	default List<byte[]> xclaim(byte[] key, byte[] groupname, byte[] consumername, long minIdleTime, long newIdleTime, int retries, boolean force, byte[][] ids) {
		return null;
	}

	@Override
	default StreamInfo xinfoStream(byte[] key) {
		return null;
	}

	@Override
	default List<StreamGroupInfo> xinfoGroup(byte[] key) {
		return null;
	}

	@Override
	default List<StreamConsumersInfo> xinfoConsumers(byte[] key, byte[] group) {
		return null;
	}

	@Override
	default String set(String key, String value) {
		return null;
	}

	@Override
	default String set(String key, String value, SetParams params) {
		return null;
	}

	@Override
	default String get(String key) {
		return null;
	}

	@Override
	default Boolean exists(String key) {
		return null;
	}

	@Override
	default Long persist(String key) {
		return null;
	}

	@Override
	default String type(String key) {
		return null;
	}

	@Override
	default byte[] dump(String key) {
		return new byte[0];
	}

	@Override
	default String restore(String key, int ttl, byte[] serializedValue) {
		return null;
	}

	@Override
	default String restoreReplace(String key, int ttl, byte[] serializedValue) {
		return null;
	}

	@Override
	default Long expire(String key, int seconds) {
		return null;
	}

	@Override
	default Long pexpire(String key, long milliseconds) {
		return null;
	}

	@Override
	default Long expireAt(String key, long unixTime) {
		return null;
	}

	@Override
	default Long pexpireAt(String key, long millisecondsTimestamp) {
		return null;
	}

	@Override
	default Long ttl(String key) {
		return null;
	}

	@Override
	default Long pttl(String key) {
		return null;
	}

	@Override
	default Long touch(String key) {
		return null;
	}

	@Override
	default Boolean setbit(String key, long offset, boolean value) {
		return null;
	}

	@Override
	default Boolean setbit(String key, long offset, String value) {
		return null;
	}

	@Override
	default Boolean getbit(String key, long offset) {
		return null;
	}

	@Override
	default Long setrange(String key, long offset, String value) {
		return null;
	}

	@Override
	default String getrange(String key, long startOffset, long endOffset) {
		return null;
	}

	@Override
	default String getSet(String key, String value) {
		return null;
	}

	@Override
	default Long setnx(String key, String value) {
		return null;
	}

	@Override
	default String setex(String key, int seconds, String value) {
		return null;
	}

	@Override
	default String psetex(String key, long milliseconds, String value) {
		return null;
	}

	@Override
	default Long decrBy(String key, long decrement) {
		return null;
	}

	@Override
	default Long decr(String key) {
		return null;
	}

	@Override
	default Long incrBy(String key, long increment) {
		return null;
	}

	@Override
	default Double incrByFloat(String key, double increment) {
		return null;
	}

	@Override
	default Long incr(String key) {
		return null;
	}

	@Override
	default Long append(String key, String value) {
		return null;
	}

	@Override
	default String substr(String key, int start, int end) {
		return null;
	}

	@Override
	default Long hset(String key, String field, String value) {
		return null;
	}

	@Override
	default Long hset(String key, Map<String, String> hash) {
		return null;
	}

	@Override
	default String hget(String key, String field) {
		return null;
	}

	@Override
	default Long hsetnx(String key, String field, String value) {
		return null;
	}

	@Override
	default String hmset(String key, Map<String, String> hash) {
		return null;
	}

	@Override
	default List<String> hmget(String key, String... fields) {
		return null;
	}

	@Override
	default Long hincrBy(String key, String field, long value) {
		return null;
	}

	@Override
	default Double hincrByFloat(String key, String field, double value) {
		return null;
	}

	@Override
	default Boolean hexists(String key, String field) {
		return null;
	}

	@Override
	default Long hdel(String key, String... field) {
		return null;
	}

	@Override
	default Long hlen(String key) {
		return null;
	}

	@Override
	default Set<String> hkeys(String key) {
		return null;
	}

	@Override
	default List<String> hvals(String key) {
		return null;
	}

	@Override
	default Map<String, String> hgetAll(String key) {
		return null;
	}

	@Override
	default Long rpush(String key, String... string) {
		return null;
	}

	@Override
	default Long lpush(String key, String... string) {
		return null;
	}

	@Override
	default Long llen(String key) {
		return null;
	}

	@Override
	default List<String> lrange(String key, long start, long stop) {
		return null;
	}

	@Override
	default String ltrim(String key, long start, long stop) {
		return null;
	}

	@Override
	default String lindex(String key, long index) {
		return null;
	}

	@Override
	default String lset(String key, long index, String value) {
		return null;
	}

	@Override
	default Long lrem(String key, long count, String value) {
		return null;
	}

	@Override
	default String lpop(String key) {
		return null;
	}

	@Override
	default List<String> lpop(String key, int count) {
		return null;
	}

	@Override
	default Long lpos(String key, String element) {
		return null;
	}

	@Override
	default Long lpos(String key, String element, LPosParams params) {
		return null;
	}

	@Override
	default List<Long> lpos(String key, String element, LPosParams params, long count) {
		return null;
	}

	@Override
	default String rpop(String key) {
		return null;
	}

	@Override
	default List<String> rpop(String key, int count) {
		return null;
	}

	@Override
	default Long sadd(String key, String... member) {
		return null;
	}

	@Override
	default Set<String> smembers(String key) {
		return null;
	}

	@Override
	default Long srem(String key, String... member) {
		return null;
	}

	@Override
	default String spop(String key) {
		return null;
	}

	@Override
	default Set<String> spop(String key, long count) {
		return null;
	}

	@Override
	default Long scard(String key) {
		return null;
	}

	@Override
	default Boolean sismember(String key, String member) {
		return null;
	}

	@Override
	default List<Boolean> smismember(String key, String... members) {
		return null;
	}

	@Override
	default String srandmember(String key) {
		return null;
	}

	@Override
	default List<String> srandmember(String key, int count) {
		return null;
	}

	@Override
	default Long strlen(String key) {
		return null;
	}

	@Override
	default Long zadd(String key, double score, String member) {
		return null;
	}

	@Override
	default Long zadd(String key, double score, String member, ZAddParams params) {
		return null;
	}

	@Override
	default Long zadd(String key, Map<String, Double> scoreMembers) {
		return null;
	}

	@Override
	default Long zadd(String key, Map<String, Double> scoreMembers, ZAddParams params) {
		return null;
	}

	@Override
	default Set<String> zrange(String key, long start, long stop) {
		return null;
	}

	@Override
	default Long zrem(String key, String... members) {
		return null;
	}

	@Override
	default Double zincrby(String key, double increment, String member) {
		return null;
	}

	@Override
	default Double zincrby(String key, double increment, String member, ZIncrByParams params) {
		return null;
	}

	@Override
	default Long zrank(String key, String member) {
		return null;
	}

	@Override
	default Long zrevrank(String key, String member) {
		return null;
	}

	@Override
	default Set<String> zrevrange(String key, long start, long stop) {
		return null;
	}

	@Override
	default Set<Tuple> zrangeWithScores(String key, long start, long stop) {
		return null;
	}

	@Override
	default Set<Tuple> zrevrangeWithScores(String key, long start, long stop) {
		return null;
	}

	@Override
	default Long zcard(String key) {
		return null;
	}

	@Override
	default Double zscore(String key, String member) {
		return null;
	}

	@Override
	default List<Double> zmscore(String key, String... members) {
		return null;
	}

	@Override
	default Tuple zpopmax(String key) {
		return null;
	}

	@Override
	default Set<Tuple> zpopmax(String key, int count) {
		return null;
	}

	@Override
	default Tuple zpopmin(String key) {
		return null;
	}

	@Override
	default Set<Tuple> zpopmin(String key, int count) {
		return null;
	}

	@Override
	default List<String> sort(String key) {
		return null;
	}

	@Override
	default List<String> sort(String key, SortingParams sortingParameters) {
		return null;
	}

	@Override
	default Long zcount(String key, double min, double max) {
		return null;
	}

	@Override
	default Long zcount(String key, String min, String max) {
		return null;
	}

	@Override
	default Set<String> zrangeByScore(String key, double min, double max) {
		return null;
	}

	@Override
	default Set<String> zrangeByScore(String key, String min, String max) {
		return null;
	}

	@Override
	default Set<String> zrevrangeByScore(String key, double max, double min) {
		return null;
	}

	@Override
	default Set<String> zrangeByScore(String key, double min, double max, int offset, int count) {
		return null;
	}

	@Override
	default Set<String> zrevrangeByScore(String key, String max, String min) {
		return null;
	}

	@Override
	default Set<String> zrangeByScore(String key, String min, String max, int offset, int count) {
		return null;
	}

	@Override
	default Set<String> zrevrangeByScore(String key, double max, double min, int offset, int count) {
		return null;
	}

	@Override
	default Set<Tuple> zrangeByScoreWithScores(String key, double min, double max) {
		return null;
	}

	@Override
	default Set<Tuple> zrevrangeByScoreWithScores(String key, double max, double min) {
		return null;
	}

	@Override
	default Set<Tuple> zrangeByScoreWithScores(String key, double min, double max, int offset, int count) {
		return null;
	}

	@Override
	default Set<String> zrevrangeByScore(String key, String max, String min, int offset, int count) {
		return null;
	}

	@Override
	default Set<Tuple> zrangeByScoreWithScores(String key, String min, String max) {
		return null;
	}

	@Override
	default Set<Tuple> zrevrangeByScoreWithScores(String key, String max, String min) {
		return null;
	}

	@Override
	default Set<Tuple> zrangeByScoreWithScores(String key, String min, String max, int offset, int count) {
		return null;
	}

	@Override
	default Set<Tuple> zrevrangeByScoreWithScores(String key, double max, double min, int offset, int count) {
		return null;
	}

	@Override
	default Set<Tuple> zrevrangeByScoreWithScores(String key, String max, String min, int offset, int count) {
		return null;
	}

	@Override
	default Long zremrangeByRank(String key, long start, long stop) {
		return null;
	}

	@Override
	default Long zremrangeByScore(String key, double min, double max) {
		return null;
	}

	@Override
	default Long zremrangeByScore(String key, String min, String max) {
		return null;
	}

	@Override
	default Long zlexcount(String key, String min, String max) {
		return null;
	}

	@Override
	default Set<String> zrangeByLex(String key, String min, String max) {
		return null;
	}

	@Override
	default Set<String> zrangeByLex(String key, String min, String max, int offset, int count) {
		return null;
	}

	@Override
	default Set<String> zrevrangeByLex(String key, String max, String min) {
		return null;
	}

	@Override
	default Set<String> zrevrangeByLex(String key, String max, String min, int offset, int count) {
		return null;
	}

	@Override
	default Long zremrangeByLex(String key, String min, String max) {
		return null;
	}

	@Override
	default Long linsert(String key, ListPosition where, String pivot, String value) {
		return null;
	}

	@Override
	default Long lpushx(String key, String... string) {
		return null;
	}

	@Override
	default Long rpushx(String key, String... string) {
		return null;
	}

	@Override
	default List<String> blpop(int timeout, String key) {
		return null;
	}

	@Override
	default List<String> brpop(int timeout, String key) {
		return null;
	}

	@Override
	default Long del(String key) {
		return null;
	}

	@Override
	default Long unlink(String key) {
		return null;
	}

	@Override
	default String echo(String string) {
		return null;
	}

	@Override
	default Long move(String key, int dbIndex) {
		return null;
	}

	@Override
	default Long bitcount(String key) {
		return null;
	}

	@Override
	default Long bitcount(String key, long start, long end) {
		return null;
	}

	@Override
	default Long bitpos(String key, boolean value) {
		return null;
	}

	@Override
	default Long bitpos(String key, boolean value, BitPosParams params) {
		return null;
	}

	@Override
	default ScanResult<Map.Entry<String, String>> hscan(String key, String cursor) {
		return null;
	}

	@Override
	default ScanResult<Map.Entry<String, String>> hscan(String key, String cursor, ScanParams params) {
		return null;
	}

	@Override
	default ScanResult<String> sscan(String key, String cursor) {
		return null;
	}

	@Override
	default ScanResult<Tuple> zscan(String key, String cursor) {
		return null;
	}

	@Override
	default ScanResult<Tuple> zscan(String key, String cursor, ScanParams params) {
		return null;
	}

	@Override
	default ScanResult<String> sscan(String key, String cursor, ScanParams params) {
		return null;
	}

	@Override
	default Long pfadd(String key, String... elements) {
		return null;
	}

	@Override
	default long pfcount(String key) {
		return 0;
	}

	@Override
	default Long geoadd(String key, double longitude, double latitude, String member) {
		return null;
	}

	@Override
	default Long geoadd(String key, Map<String, GeoCoordinate> memberCoordinateMap) {
		return null;
	}

	@Override
	default Double geodist(String key, String member1, String member2) {
		return null;
	}

	@Override
	default Double geodist(String key, String member1, String member2, GeoUnit unit) {
		return null;
	}

	@Override
	default List<String> geohash(String key, String... members) {
		return null;
	}

	@Override
	default List<GeoCoordinate> geopos(String key, String... members) {
		return null;
	}

	@Override
	default List<GeoRadiusResponse> georadius(String key, double longitude, double latitude, double radius, GeoUnit unit) {
		return null;
	}

	@Override
	default List<GeoRadiusResponse> georadiusReadonly(String key, double longitude, double latitude, double radius, GeoUnit unit) {
		return null;
	}

	@Override
	default List<GeoRadiusResponse> georadius(String key, double longitude, double latitude, double radius, GeoUnit unit, GeoRadiusParam param) {
		return null;
	}

	@Override
	default List<GeoRadiusResponse> georadiusReadonly(String key, double longitude, double latitude, double radius, GeoUnit unit, GeoRadiusParam param) {
		return null;
	}

	@Override
	default List<GeoRadiusResponse> georadiusByMember(String key, String member, double radius, GeoUnit unit) {
		return null;
	}

	@Override
	default List<GeoRadiusResponse> georadiusByMemberReadonly(String key, String member, double radius, GeoUnit unit) {
		return null;
	}

	@Override
	default List<GeoRadiusResponse> georadiusByMember(String key, String member, double radius, GeoUnit unit, GeoRadiusParam param) {
		return null;
	}

	@Override
	default List<GeoRadiusResponse> georadiusByMemberReadonly(String key, String member, double radius, GeoUnit unit, GeoRadiusParam param) {
		return null;
	}

	@Override
	default List<Long> bitfield(String key, String... arguments) {
		return null;
	}

	@Override
	default List<Long> bitfieldReadonly(String key, String... arguments) {
		return null;
	}

	@Override
	default Long hstrlen(String key, String field) {
		return null;
	}

	@Override
	default StreamEntryID xadd(String key, StreamEntryID id, Map<String, String> hash) {
		return null;
	}

	@Override
	default StreamEntryID xadd(String key, StreamEntryID id, Map<String, String> hash, long maxLen, boolean approximateLength) {
		return null;
	}

	@Override
	default Long xlen(String key) {
		return null;
	}

	@Override
	default List<StreamEntry> xrange(String key, StreamEntryID start, StreamEntryID end, int count) {
		return null;
	}

	@Override
	default List<StreamEntry> xrevrange(String key, StreamEntryID end, StreamEntryID start, int count) {
		return null;
	}

	@Override
	default long xack(String key, String group, StreamEntryID... ids) {
		return 0;
	}

	@Override
	default String xgroupCreate(String key, String groupname, StreamEntryID id, boolean makeStream) {
		return null;
	}

	@Override
	default String xgroupSetID(String key, String groupname, StreamEntryID id) {
		return null;
	}

	@Override
	default long xgroupDestroy(String key, String groupname) {
		return 0;
	}

	@Override
	default Long xgroupDelConsumer(String key, String groupname, String consumername) {
		return null;
	}

	@Override
	default List<StreamPendingEntry> xpending(String key, String groupname, StreamEntryID start, StreamEntryID end, int count, String consumername) {
		return null;
	}

	@Override
	default long xdel(String key, StreamEntryID... ids) {
		return 0;
	}

	@Override
	default long xtrim(String key, long maxLen, boolean approximate) {
		return 0;
	}

	@Override
	default List<StreamEntry> xclaim(String key, String group, String consumername, long minIdleTime, long newIdleTime, int retries, boolean force, StreamEntryID... ids) {
		return null;
	}

	@Override
	default StreamInfo xinfoStream(String key) {
		return null;
	}

	@Override
	default List<StreamGroupInfo> xinfoGroup(String key) {
		return null;
	}

	@Override
	default List<StreamConsumersInfo> xinfoConsumers(String key, String group) {
		return null;
	}
}
