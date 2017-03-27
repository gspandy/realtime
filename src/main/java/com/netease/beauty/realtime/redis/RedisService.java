package com.netease.beauty.realtime.redis;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface RedisService {
	public boolean set(String key, String value);

	public boolean setWithExpireTime(String key, String value, int seconds);

	public String get(String key);

	public Long incr(String key);

	public Long incrBy(String key, int incr);

	public boolean del(String key);

	public boolean expire(String key, int seconds);

	public boolean exists(String key);

	public Long hincrBy(String key, String field, long value);

	public Set<String> hkeys(String key);

	public String hget(String key, String field);

	public boolean hset(String key, String field, String value);

	public Long hdel(String key, String field);

	public boolean hmset(String key, Map<String, String> map);

	public List<String> hmget(String key, Collection<String> fields);

	public Map<String, String> hgetall(String key);

	public Long lpush(String key, String value);

	public long lpush(String key, List<String> value);

	public Long lpushx(String key, String value);

	public boolean sismember(String key, String value);

	/**
	 * 最后列表的顺序和放入的参数顺序一致
	 *
	 * @param key
	 * @param value
	 * @return
	 */
	public Long lpush(String key, String... value);

	public long rpush(String key, String... value);

	public Long llen(String key);

	public String rpop(String key);

	public boolean ltrim(String key, long start, long end);

	public List<String> lrange(String key, long start, long end);

	public boolean lrem(String key, int count, String value);

	public Double zincrBy(String key, String field, double value);

	public Long zcount(String key, long minScore, long maxScore);

	public boolean zadd(String key, long score, String value);

	public Long zcard(String key);

	public Long zrem(String key, String... values);

	public boolean zremrangebyrank(String key, int start, int end);

	public boolean zremrangebyscore(String key, double minScore, double maxScore);

	public List<String> zrangebyscore(String key, double minScore, double maxScore, int offset, int count);

	public Set<String> zrevRange(String key, int start, int end);

	public List<String> zrevrangebyscore(String key, double minScore, double maxScore, int offset, int count);

	public boolean setInBytes(String key, byte[] value);

	public byte[] getInBytes(String key);

	public byte[] getrange(String key, long begin, long end);

	public long strlen(String key);

	public void useJedis(JedisCallback callback);

	public Double zScore(String key, String member);

	public Long setnx(String key, String value);

	public boolean setNoConflictMark(String mark, int expireSeconds);

	public Map<String, String> gets(final Collection<String> keys);

	boolean zadd(String key, String field, double score);

	CounterSet newCounterSet(String key,int expireSeconds, long sizeLimit);

	boolean setNoConflictMark(String mark, String value, int expireSeconds);

	public String lpop(String key);

	public Long sadd(String key, String value);

	public Long scard(String key);

	public long sremove(String setKey,String... memebers);

	RedisList newRedisList(String key, int expireSeconds, long sizeLimit);

	public Set<String> smembers(String key);

	long ttl(String key);

	public Long zrank(String key, String member);

	public boolean zadd(String key, Map<Double, String> values);

	public Map<String, Double> zrangebyscoreWithScores(String key, int offset, int count);

	Long zremrangeByRank(String key, long start, long end);

	public Map<String,DebugObject> debugObject(String ... keys);

	public Long sadd(String key, List<String> values);
	
	/**
	 * 分布式锁
	 * expire 过期时间单位秒
	 * @author hzliamo
	 */
	public boolean setLock(long timeout,int expire,String key);
}
