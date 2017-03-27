package com.netease.beauty.realtime.redis;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.annotation.PreDestroy;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import redis.clients.jedis.Connection;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.Tuple;
import redis.clients.jedis.exceptions.JedisException;
import redis.clients.util.SafeEncoder;

import com.netease.beauty.realtime.constant.ExceptionConst;
import com.netease.beauty.realtime.exception.MonitoredException;
import com.netease.beauty.realtime.util.BaseUtils;

@Service
public class RedisServiceImpl implements RedisService {
	private static final Logger logger = Logger.getLogger("hidebug");
	private static final Logger errorLog = Logger.getLogger(RedisServiceImpl.class);
	private static final int DEFAULT_EXPIRE_TIME = 3600 * 24 * 30;
	private ExecutorService threadPool = Executors.newCachedThreadPool();
	private static final String DELIMETER = ";";

	//纳秒和毫秒之间的转换率
	public static final long MILLI_NANO_TIME = 1000 ;
	
	public static final String LOCKED = "TRUE";
	
	private String masters;
	private String sentinels;
	private String password;
	private int timeout;
	private int maxActive;
	private int maxIdle=10;
	private int minIdle=5;
	
	

	public int getMaxIdle() {
		return maxIdle;
	}

	public void setMaxIdle(int maxIdle) {
		this.maxIdle = maxIdle;
	}

	public int getMinIdle() {
		return minIdle;
	}

	public void setMinIdle(int minIdle) {
		this.minIdle = minIdle;
	}

	public String getMasters() {
		return masters;
	}

	public void setMasters(String masters) {
		this.masters = masters;
	}

	public String getSentinels() {
		return sentinels;
	}

	public void setSentinels(String sentinels) {
		this.sentinels = sentinels;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public int getMaxActive() {
		return maxActive;
	}

	public void setMaxActive(int maxActive) {
		this.maxActive = maxActive;
	}

	ShardedJedisSentinelPool jedisPool;

	public RedisServiceImpl() {

	}

	public RedisServiceImpl(String password, String hosts, int maxActive) {
		this.password = password;
		this.masters = hosts;
		this.maxActive = maxActive;
	}

	public void init() {
		List<String> masterList = BaseUtils.splitToList(masters,DELIMETER);
		Set<String> sentinelSet = new HashSet<String>(BaseUtils.splitToList(sentinels, DELIMETER));
		GenericObjectPoolConfig config = new GenericObjectPoolConfig();
		config.setMaxTotal(maxActive);
		config.setMaxIdle(maxIdle);
		config.setMinIdle(minIdle);
		config.setTestOnBorrow(false);
		config.setTimeBetweenEvictionRunsMillis(20000); //10 seconds.
		config.setSoftMinEvictableIdleTimeMillis(30000);
		config.setNumTestsPerEvictionRun(maxActive);
		config.setTestWhileIdle(true);
		if("null".equalsIgnoreCase(password)){
			jedisPool = new ShardedJedisSentinelPool(masterList, sentinelSet, config, timeout);
		}else{
			jedisPool = new ShardedJedisSentinelPool(masterList, sentinelSet, config, timeout, password);
		}
	}

	@Override
	public boolean set(String key, String value) {
		ShardedJedis sJedis = getJedis();
		try {
			// String result = sJedis.set(key, value,);
			// 为了防止redis内存耗尽,设置一下内存超时
			String result = sJedis.setex(key, DEFAULT_EXPIRE_TIME, value);
			boolean isSucc = "OK".equalsIgnoreCase(result);
			if (logger.isDebugEnabled()) {
				logger.debug("set " + key + "->" + value + "," + isSucc);
			}
			jedisPool.returnResource(sJedis);
			return isSucc;
		} catch (Exception e) {
			errorLog.error("set error :" + key + ", " + value, e);
			jedisPool.returnBrokenResource(sJedis);
			throw new MonitoredException(ExceptionConst.REDIS_EXCEPTION, "set reid failed!", e);
		}
	}

	@Override
	public Long setnx(String key, String value) {
		ShardedJedis sJedis = getJedis();
		try {
			Long code = sJedis.setnx(key, value);
			jedisPool.returnResource(sJedis);
			return code;
		} catch (Exception e) {
			errorLog.error("set error :" + key + ", " + value, e);
			jedisPool.returnBrokenResource(sJedis);
			throw new MonitoredException(ExceptionConst.REDIS_EXCEPTION, "set reid failed!", e);
		}
	}

	@Override
	public boolean setNoConflictMark(String mark, int expireSeconds) {
		return setNoConflictMark(mark, "NoConflictMark", expireSeconds);
	}

	@Override
	public boolean setNoConflictMark(String mark, String value, int expireSeconds) {
		ShardedJedis sJedis = getJedis();
		try {
			Long code = sJedis.setnx(mark, value);
			boolean ret = false;
			if (code != null && code == 1) {
				Long expire = sJedis.expire(mark, expireSeconds);
				if (expire != null && expire > 0) {
					ret = true;
				} else {
					sJedis.del(mark);
					ret = false;
				}
			}
			jedisPool.returnResource(sJedis);
			return ret;
		} catch (Exception e) {
			errorLog.error("set error :" + mark + ", " + "noConfictMark", e);
			jedisPool.returnBrokenResource(sJedis);
			throw new MonitoredException(ExceptionConst.REDIS_EXCEPTION, "set redis failed!", e);
		}
	}

	@Override
	public boolean setWithExpireTime(String key, String value, int seconds) {
		ShardedJedis sJedis = getJedis();
		try {
			String result = sJedis.setex(key, seconds, value);
			boolean isSucc = "OK".equalsIgnoreCase(result);
			if (logger.isDebugEnabled()) {
				logger.debug("set " + key + "->" + value + "," + seconds + "," + isSucc);
			}
			jedisPool.returnResource(sJedis);
			return isSucc;
		} catch (Exception e) {
			errorLog.error("set error :" + key + ", " + value + ", " + seconds, e);
			jedisPool.returnBrokenResource(sJedis);
			throw new MonitoredException(ExceptionConst.REDIS_EXCEPTION, "expire key failed!", e);
		}
	}

	private ShardedJedis getJedis() {
		try {
			ShardedJedis jedis = jedisPool.getResource();
			return jedis;
		} catch (Exception e) {
			logger.error("can't get client from jedis pool:"+this.masters, e);
			throw new MonitoredException(ExceptionConst.REDIS_POLL_EXCEPTION, "get jedis failed!", e);
		}
	}

	@Override
	public String get(String key) {
		ShardedJedis sJedis = getJedis();
		try {
			String result = sJedis.get(key);
			if (logger.isDebugEnabled()) {

				logger.debug("get " + key + " -> " + result);
			}
			jedisPool.returnResource(sJedis);
			return result;
		} catch (Exception e) {
			jedisPool.returnBrokenResource(sJedis);
			errorLog.error("get error :" + key, e);
			throw new MonitoredException(ExceptionConst.REDIS_POLL_EXCEPTION, "get jedis failed!", e);
		}
	}

	@Override
	public boolean del(String key) {
		ShardedJedis sJedis = getJedis();
		try {
			long count = sJedis.del(key);
			if (logger.isDebugEnabled()) {
				logger.debug("del " + key + " -> " + count);
			}
			jedisPool.returnResource(sJedis);
			return true;
		} catch (Exception e) {
			errorLog.error("del error :" + key, e);
			jedisPool.returnBrokenResource(sJedis);
			throw new MonitoredException(ExceptionConst.REDIS_EXCEPTION, "delete key failed!", e);
		}
	}

	@Override
	public Long hincrBy(String key, String field, long value) {
		ShardedJedis sJedis = getJedis();
		try {
			Long result = sJedis.hincrBy(key, field, value);
			if (logger.isDebugEnabled()) {
				logger.debug("hincrby  " + key + "," + field + "," + value + " -> " + result);
			}
			this.jedisPool.returnResource(sJedis);
			return result;
		} catch (Exception e) {
			errorLog.error("hincrby error " + key + "," + field + "," + value, e);
			this.jedisPool.returnBrokenResource(sJedis);
			throw new MonitoredException(ExceptionConst.REDIS_EXCEPTION, "hincrBy key failed!", e);
		}
	}

	@Override
	public Double zincrBy(String key, String field, double value) {
		ShardedJedis sJedis = getJedis();
		try {
			Double result = sJedis.zincrby(key, value, field);
			if (logger.isDebugEnabled()) {
				logger.debug("zincrby  " + key + "," + field + "," + value + " -> " + result);
			}
			jedisPool.returnResource(sJedis);
			return result;
		} catch (Exception e) {
			errorLog.error("zincrby error " + key + "," + field + "," + value, e);
			this.jedisPool.returnBrokenResource(sJedis);
			throw new MonitoredException(ExceptionConst.REDIS_EXCEPTION, "zincrBy key failed!", e);
		}
	}

	@Override
	public Set<String> hkeys(String key) {
		ShardedJedis sJedis = getJedis();
		try {
			Set<String> result = sJedis.hkeys(key);
			if (logger.isDebugEnabled()) {
				logger.debug("hkeys " + key + " -> " + result);
			}
			jedisPool.returnResource(sJedis);
			return result;
		} catch (Exception e) {
			jedisPool.returnBrokenResource(sJedis);
			errorLog.error("hkeys error " + key, e);
			throw new MonitoredException(ExceptionConst.REDIS_EXCEPTION, "hkeys key failed!", e);
		}
	}

	@Override
	public String hget(String key, String field) {
		ShardedJedis sJedis = getJedis();
		try {
			String result = sJedis.hget(key, field);
			if (logger.isDebugEnabled()) {
				logger.debug("hget " + key + "," + field + " -> " + result);
			}
			jedisPool.returnResource(sJedis);
			return result;
		} catch (Exception e) {
			errorLog.error("hget error " + key + "," + field, e);
			jedisPool.returnBrokenResource(sJedis);
			throw new MonitoredException(ExceptionConst.REDIS_EXCEPTION, "hget key failed!", e);
		}
	}

	@Override
	public List<String> hmget(String key, Collection<String> fields) {
		ShardedJedis sJedis = getJedis();
		try {
			List<String> result = sJedis.hmget(key, fields.toArray(new String[fields.size()]));
			if (logger.isDebugEnabled()) {
				logger.debug("hmget " + key + "," + fields + " -> " + result);
			}
			jedisPool.returnResource(sJedis);
			return result;
		} catch (Exception e) {
			jedisPool.returnBrokenResource(sJedis);
			errorLog.error("hmget error " + key + "," + fields, e);
			throw new MonitoredException(ExceptionConst.REDIS_EXCEPTION, "hmget key failed!", e);
		}
	}

	@Override
	public boolean hset(String key, String field, String value) {
		ShardedJedis sJedis = getJedis();
		try {
			Long result = sJedis.hset(key, field, value);
			jedisPool.returnResource(sJedis);
			if (result.longValue() == 1 || result.longValue() == 0) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			errorLog.error("hset error", e);
			this.jedisPool.returnBrokenResource(sJedis);
			throw new MonitoredException(ExceptionConst.REDIS_EXCEPTION, "hset key failed!", e);
		}
	}

	@Override
	public Long hdel(String key, String field) {
		ShardedJedis sJedis = getJedis();
		try {
			Long result = sJedis.hdel(key, field);
			jedisPool.returnResource(sJedis);
			if (logger.isDebugEnabled()) {
				logger.debug("hdel " + key + "," + field + " -> " + result);
			}
			return result;
		} catch (Exception e) {
			this.jedisPool.returnBrokenResource(sJedis);
			errorLog.error("hdel error " + key + "," + field, e);
			throw new MonitoredException(ExceptionConst.REDIS_EXCEPTION, "hdel key failed!", e);
		}
	}

	@Override
	public boolean hmset(String key, Map<String, String> map) {
		ShardedJedis sJedis = getJedis();
		try {
			String result = sJedis.hmset(key, map);
			this.jedisPool.returnResource(sJedis);
			return "OK".equalsIgnoreCase(result);
		} catch (Exception e) {
			this.jedisPool.returnBrokenResource(sJedis);
			errorLog.error("hmset error", e);
			throw new MonitoredException(ExceptionConst.REDIS_EXCEPTION, "hmset key failed!", e);
		}
	}

	@Override
	public Long incr(String key) {
		ShardedJedis sJedis = getJedis();
		try {
			Long result = sJedis.incr(key);
			if (logger.isDebugEnabled()) {
				logger.debug("incr " + key + "," + result);
			}
			this.jedisPool.returnResource(sJedis);
			return result;
		} catch (Exception e) {
			errorLog.error("incr error :" + key, e);
			jedisPool.returnBrokenResource(sJedis);
			throw new MonitoredException(ExceptionConst.REDIS_EXCEPTION, "incr key failed!", e);
		}
	}

	@Override
	public Long incrBy(String key, int incr) {
		ShardedJedis sJedis = getJedis();
		try {
			Long result = sJedis.incrBy(key, incr);
			if (logger.isDebugEnabled()) {
				logger.debug("incrby " + key + "," + incr + "," + result);
			}
			this.jedisPool.returnResource(sJedis);
			return result;
		} catch (Exception e) {
			errorLog.error("incrby error :" + key + "," + incr, e);
			this.jedisPool.returnBrokenResource(sJedis);
			throw new MonitoredException(ExceptionConst.REDIS_EXCEPTION, "incrBy key failed!", e);
		}
	}

	@Override
	public Long lpush(String key, String value) {
		ShardedJedis sJedis = getJedis();
		try {
			Long length = sJedis.lpush(key, value);
			this.jedisPool.returnResource(sJedis);
			return length;
		} catch (Exception e) {
			this.jedisPool.returnBrokenResource(sJedis);
			errorLog.error("lpush error,key:" + key + ",value:" + value, e);
			throw new MonitoredException(ExceptionConst.REDIS_EXCEPTION, "lpush key failed!", e);
		}
	}

	@Override
	public Long lpush(String key, String... values) {
		ShardedJedis sJedis = getJedis();
		try {
			Long length = sJedis.lpush(key, values);
			jedisPool.returnResource(sJedis);
			return length;
		} catch (Exception e) {
			this.jedisPool.returnBrokenResource(sJedis);
			errorLog.error("lpush error,key:" + key + ",value:" + values, e);
			throw new MonitoredException(ExceptionConst.REDIS_EXCEPTION, "lpush key failed!", e);
		}
	}

	@Override
	public Long llen(String key) {
		ShardedJedis sJedis = getJedis();
		try {
			Long length = sJedis.llen(key);
			jedisPool.returnResource(sJedis);
			return length;
		} catch (Exception e) {
			errorLog.error("llen error,key:" + key, e);
			this.jedisPool.returnBrokenResource(sJedis);
			throw new MonitoredException(ExceptionConst.REDIS_EXCEPTION, "llen key failed!", e);
		}
	}

	@Override
	public Long lpushx(String key, String value) {
		ShardedJedis sJedis = getJedis();
		try {
			Long length = sJedis.lpushx(key, value);
			jedisPool.returnResource(sJedis);
			return length;
		} catch (Exception e) {
			this.jedisPool.returnBrokenResource(sJedis);
			errorLog.error("lpush error,key:" + key + ",value:" + value, e);
			throw new MonitoredException(ExceptionConst.REDIS_EXCEPTION, "lpushx key failed!", e);
		}
	}

	@Override
	public String rpop(String key) {
		ShardedJedis sJedis = getJedis();
		try {
			String value = sJedis.rpop(key);
			jedisPool.returnResource(sJedis);
			return value;
		} catch (Exception e) {
			errorLog.error("rpop error,key:" + key, e);
			this.jedisPool.returnBrokenResource(sJedis);
			throw new MonitoredException(ExceptionConst.REDIS_EXCEPTION, "rpop key failed!", e);
		}
	}

	@Override
	public boolean ltrim(String key, long start, long end) {
		ShardedJedis sJedis = getJedis();
		try {
			String result = sJedis.getShard(key).ltrim(key, start, end);
			this.jedisPool.returnResource(sJedis);
			return "OK".equalsIgnoreCase(result);
		} catch (Exception e) {
			this.jedisPool.returnBrokenResource(sJedis);
			errorLog.error("ltrim error,key:" + key, e);
			throw new MonitoredException(ExceptionConst.REDIS_EXCEPTION, "rpop key failed!", e);
		}
	}

	@Override
	public List<String> lrange(String key, long start, long end) {
		ShardedJedis sJedis = getJedis();
		try {
			List<String> result = sJedis.getShard(key).lrange(key, start, end);
			this.jedisPool.returnResource(sJedis);
			return result;
		} catch (Exception e) {
			errorLog.error("lrange error,key:" + key, e);
			this.jedisPool.returnBrokenResource(sJedis);
			throw new MonitoredException(ExceptionConst.REDIS_EXCEPTION, "lrange key failed!", e);
		}
	}

	@Override
	public Long zcount(String key, long minScore, long maxScore) {
		ShardedJedis sJedis = getJedis();
		try {
			Long count = sJedis.getShard(key).zcount(key, minScore, maxScore);
			this.jedisPool.returnResource(sJedis);
			return count;
		} catch (Exception e) {
			errorLog.error("zcount error,key:" + key, e);
			this.jedisPool.returnBrokenResource(sJedis);
			throw new MonitoredException(ExceptionConst.REDIS_EXCEPTION, "zcount key failed!", e);
		}
	}

	@Override
	public boolean zadd(String key, long score, String value) {
		ShardedJedis sJedis = getJedis();
		try {
			Long ret = sJedis.getShard(key).zadd(key, score, value);
			this.jedisPool.returnResource(sJedis);
			if (ret != null && ret.longValue() >= 0) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			this.jedisPool.returnBrokenResource(sJedis);
			errorLog.error("zadd error,key:" + key, e);
			throw new MonitoredException(ExceptionConst.REDIS_EXCEPTION, "zadd key failed!", e);
		}
	}


	@Override
	public boolean zadd(String key, Map<Double, String> values) {
		ShardedJedis sJedis = getJedis();
		try {
//			Long ret = sJedis.getShard(key).zadd(key, values);
			Map<String,Double> memmbers = new HashMap<String,Double>();
			for(Double score: values.keySet()){
				memmbers.put(values.get(score), score);
			}
			Long ret = sJedis.getShard(key).zadd(key,memmbers);
			this.jedisPool.returnResource(sJedis);
			if (ret != null && ret.longValue() >= 0) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			this.jedisPool.returnBrokenResource(sJedis);
			errorLog.error("zadd error,key:" + key, e);
			throw new MonitoredException(ExceptionConst.REDIS_EXCEPTION, "zadd key failed!", e);
		}
	}

	@Override
	public boolean zadd(String key, String field, double score) {
		ShardedJedis sJedis = getJedis();
		try {
			Long ret = sJedis.getShard(key).zadd(key, score, field);
			this.jedisPool.returnResource(sJedis);
			if (ret != null && ret.longValue() >= 0) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			this.jedisPool.returnBrokenResource(sJedis);
			errorLog.error("zadd error,key:" + key, e);
			throw new MonitoredException(ExceptionConst.REDIS_EXCEPTION, "zadd key failed!", e);
		}
	}

	@Override
	public Long zcard(String key) {
		ShardedJedis sJedis = getJedis();
		try {
			Long count = sJedis.getShard(key).zcard(key);
			this.jedisPool.returnResource(sJedis);
			return count;
		} catch (Exception e) {
			this.jedisPool.returnBrokenResource(sJedis);
			errorLog.error("zcard error,key:" + key, e);
			throw new MonitoredException(ExceptionConst.REDIS_EXCEPTION, "zcard key failed!", e);
		}
	}

	@Override
	public Set<String> zrevRange(String key, int start, int end) {
		ShardedJedis sJedis = getJedis();
		try {
			Set<String> result = sJedis.getShard(key).zrevrange(key, start, end);
			this.jedisPool.returnResource(sJedis);
			return result;
		} catch (Exception e) {
			errorLog.error("zrevRange error,key:" + key, e);
			this.jedisPool.returnBrokenResource(sJedis);
			throw new MonitoredException(ExceptionConst.REDIS_EXCEPTION, "zrevRange key failed!", e);
		}
	}

	@Override
	public Double zScore(String key, String member) {
		ShardedJedis sJedis = getJedis();
		try {
			Double zscore = sJedis.getShard(key).zscore(key, member);
			this.jedisPool.returnResource(sJedis);
			return zscore;
		} catch (Exception e) {
			errorLog.error("zrevRange error,key:" + key, e);
			this.jedisPool.returnBrokenResource(sJedis);
			throw new MonitoredException(ExceptionConst.REDIS_EXCEPTION, "zrevRange key failed!", e);
		}
	}

	@Override
	public boolean sismember(String key, String value) {
		ShardedJedis sJedis = getJedis();
		try {
			boolean result = sJedis.getShard(key).sismember(key, value);
			this.jedisPool.returnResource(sJedis);
			return result;
		} catch (Exception e) {
			errorLog.error("zrevRange error,key:" + key, e);
			this.jedisPool.returnBrokenResource(sJedis);
			throw new MonitoredException(ExceptionConst.REDIS_EXCEPTION, "sismember key failed!", e);
		}
	}

	@Override
	public boolean zremrangebyrank(String key, int start, int end) {
		ShardedJedis sJedis = getJedis();
		try {
			sJedis.getShard(key).zremrangeByRank(key, start, end);
			this.jedisPool.returnResource(sJedis);
			return true;
		} catch (Exception e) {
			errorLog.error("zremrangebyrank error,key:" + key, e);
			this.jedisPool.returnBrokenResource(sJedis);
			throw new MonitoredException(ExceptionConst.REDIS_EXCEPTION, "zremrangebyrank key failed!", e);
		}
	}

	public Object getProperty(Object owner, String fieldName) throws Exception {
		Field field = Connection.class.getDeclaredField(fieldName);
		field.setAccessible(true);
		return field.get(owner);
	}

	@Override
	public boolean lrem(String key, int count, String value) {
		ShardedJedis sJedis = getJedis();
		try {
			boolean b = sJedis.lrem(key, count, value) >= 0;
			this.jedisPool.returnResource(sJedis);
			return b;
		} catch (Exception e) {
			errorLog.error("zremrangebyrank error,key:" + key, e);
			this.jedisPool.returnBrokenResource(sJedis);
			throw new MonitoredException(ExceptionConst.REDIS_EXCEPTION, "lrem key failed!", e);
		}
	}

	@Override
	public boolean exists(String key) {
		ShardedJedis sJedis = getJedis();
		try {
			boolean b = sJedis.getShard(key).exists(key);
			this.jedisPool.returnResource(sJedis);
			return b;
		} catch (Exception e) {
			this.jedisPool.returnBrokenResource(sJedis);
			errorLog.error("exists error,key:" + key, e);
			throw new MonitoredException(ExceptionConst.REDIS_EXCEPTION, "exists key failed!", e);
		}
	}

	@Override
	public boolean expire(String key, int seconds) {
		ShardedJedis sJedis = getJedis();
		try {
			boolean b = sJedis.expire(key, seconds) == 1;
			this.jedisPool.returnResource(sJedis);
			return b;
		} catch (Exception e) {
			errorLog.error("expire error,key:" + key, e);
			this.jedisPool.returnBrokenResource(sJedis);
			throw new MonitoredException(ExceptionConst.REDIS_EXCEPTION, "expire key failed!", e);
		}
	}

	@Override
	public long ttl(String key) {
		ShardedJedis sJedis = getJedis();
		try {
			Long ttl = sJedis.ttl(key);
			this.jedisPool.returnResource(sJedis);
			if (ttl == null) {
				return -1L;
			}
			return ttl;
		} catch (Exception e) {
			errorLog.error("ttl error,key:" + key, e);
			this.jedisPool.returnBrokenResource(sJedis);
			throw new MonitoredException(ExceptionConst.REDIS_EXCEPTION, "ttl key failed!", e);
		}
	}

	@Override
	public Map<String, Double> zrangebyscoreWithScores(String key, int offset, int count) {
		ShardedJedis sJedis = getJedis();
		try {
			Set<Tuple> result = sJedis.zrangeByScoreWithScores(key, 0, Long.MAX_VALUE, offset, count);
			Map<String, Double> ret = new HashMap<String, Double>();
			for (Tuple r : result) {
				ret.put(r.getElement(), r.getScore());
			}
			this.jedisPool.returnResource(sJedis);
			return ret;
		} catch (Exception e) {
			this.jedisPool.returnBrokenResource(sJedis);
			errorLog.error("zrangebyscore error,key:" + key, e);
			throw new MonitoredException(ExceptionConst.REDIS_EXCEPTION, "zrangebyscore key failed!", e);
		}
	}

	@Override
	public List<String> zrangebyscore(String key, double minScore, double maxScore, int offset, int count) {
		ShardedJedis sJedis = getJedis();
		try {
			Set<Tuple> result = sJedis.zrangeByScoreWithScores(key, minScore, maxScore, offset, count);
			List<String> ret = new ArrayList<String>();
			for (Tuple r : result) {
				ret.add(r.getElement());
			}
			this.jedisPool.returnResource(sJedis);
			return ret;
		} catch (Exception e) {
			this.jedisPool.returnBrokenResource(sJedis);
			errorLog.error("zrangebyscore error,key:" + key, e);
			throw new MonitoredException(ExceptionConst.REDIS_EXCEPTION, "zrangebyscore key failed!", e);
		}
	}

	@Override
	public Map<String, String> hgetall(String key) {
		ShardedJedis sJedis = getJedis();
		try {
			Map<String, String> rs = sJedis.hgetAll(key);
			this.jedisPool.returnResource(sJedis);
			return rs;
		} catch (Exception e) {
			errorLog.error("hgetAll error,key:" + key, e);
			this.jedisPool.returnBrokenResource(sJedis);
			throw new MonitoredException(ExceptionConst.REDIS_EXCEPTION, "hgetall key failed!", e);
		}
	}

	@Override
	public boolean zremrangebyscore(String key, double minScore, double maxScore) {
		ShardedJedis sJedis = getJedis();
		try {
			Long result = sJedis.zremrangeByScore(key, minScore, maxScore);
			this.jedisPool.returnResource(sJedis);
			if (result != null)
				return true;
			else
				return false;
		} catch (Exception e) {
			errorLog.error("zremrangebyscore error,key:" + key, e);
			this.jedisPool.returnBrokenResource(sJedis);
			throw new MonitoredException(ExceptionConst.REDIS_EXCEPTION, "zremrangebyscore key failed!", e);
		}
	}

	@Override
	public List<String> zrevrangebyscore(String key, double minScore, double maxScore, int offset, int count) {
		ShardedJedis sJedis = getJedis();
		try {
			Set<Tuple> result = sJedis.zrevrangeByScoreWithScores(key, maxScore, minScore, offset, count);
			this.jedisPool.returnResource(sJedis);
			List<String> ret = new ArrayList<String>();
			for (Tuple r : result) {
				ret.add(r.getElement());
			}
			return ret;
		} catch (Exception e) {
			this.jedisPool.returnBrokenResource(sJedis);
			errorLog.error("zrevrangebyscore error,key:" + key, e);
			throw new MonitoredException(ExceptionConst.REDIS_EXCEPTION, "zrevrangebyscore key failed!", e);
		}
	}

	@Override
	public boolean setInBytes(String key, byte[] value) {
		ShardedJedis sJedis = getJedis();
		try {
			String result = sJedis.set(SafeEncoder.encode(key), value);
			this.jedisPool.returnResource(sJedis);
			boolean isSucc = "OK".equalsIgnoreCase(result);
			if (logger.isDebugEnabled()) {
				logger.debug("setinbytes " + key + "->" + value + "," + isSucc);
			}
			return isSucc;
		} catch (Exception e) {
			errorLog.error("setinbytes error :" + key + ", " + value, e);
			this.jedisPool.returnBrokenResource(sJedis);
			throw new MonitoredException(ExceptionConst.REDIS_EXCEPTION, "setInBytes key failed!", e);
		}
	}

	@Override
	public byte[] getInBytes(String key) {
		ShardedJedis sJedis = getJedis();
		try {
			byte[] result = sJedis.get(SafeEncoder.encode(key));
			this.jedisPool.returnResource(sJedis);
			if (logger.isDebugEnabled()) {
				logger.debug("getinbytes " + key + " -> " + result);
			}
			return result;
		} catch (Exception e) {
			this.jedisPool.returnBrokenResource(sJedis);
			errorLog.error("getinbytes error :" + key, e);
			throw new MonitoredException(ExceptionConst.REDIS_EXCEPTION, "getInBytes key failed!", e);
		}
	}

	@Override
	public byte[] getrange(String key, long begin, long end) {
		ShardedJedis sJedis = getJedis();
		try {
			sJedis.getShard(key).getClient().getrange(key, begin, end);
			byte[] result = sJedis.getShard(key).getClient().getBinaryBulkReply();
			if (logger.isDebugEnabled()) {
				logger.debug("getrange " + key + " -> " + result);
			}
			this.jedisPool.returnResource(sJedis);
			return result;
		} catch (Exception e) {
			this.jedisPool.returnBrokenResource(sJedis);
			errorLog.error("getrange error :" + key, e);
			throw new MonitoredException(ExceptionConst.REDIS_EXCEPTION, "getrange key failed!", e);
		}
	}

	@Override
	public long strlen(String key) {
		ShardedJedis sJedis = getJedis();
		try {
			Long result = sJedis.getShard(key).strlen(key);
			this.jedisPool.returnResource(sJedis);
			if (logger.isDebugEnabled()) {
				logger.debug("strlen " + key + "," + result);
			}
			return result;
		} catch (Exception e) {
			this.jedisPool.returnBrokenResource(sJedis);
			errorLog.error("strlen error :" + key, e);
			throw new MonitoredException(ExceptionConst.REDIS_EXCEPTION, "strlen key failed!", e);
		}
	}

	@Override
	public void useJedis(JedisCallback callback) {
		ShardedJedis sJedis = getJedis();
		boolean returned = false;
		try {
			callback.useJedis(sJedis);
			this.jedisPool.returnResource(sJedis);
			returned = true;
		} catch (JedisException ex) {
			logger.error(ex);
			this.jedisPool.returnBrokenResource(sJedis);
			returned = true;
			throw new MonitoredException(ExceptionConst.REDIS_EXCEPTION, "useJedis failed!", ex);
		} catch (RuntimeException ex) {
			logger.error(ex);
			ex.printStackTrace();
		} finally {
			if (!returned) {
				this.jedisPool.returnResource(sJedis);
			}
		}
	}

	@Override
	public long rpush(final String key, final String... value) {
		ShardedJedis sJedis = getJedis();
		try {
			Long rs = null;
			if (value == null || value.length == 0) {
				rs = sJedis.llen(key);
			} else {
				rs = sJedis.rpush(key, value);
			}
			this.jedisPool.returnResource(sJedis);
			return rs;
		} catch (Exception e) {
			this.jedisPool.returnBrokenResource(sJedis);
			errorLog.error("strlen error :" + key, e);
			throw new MonitoredException(ExceptionConst.REDIS_EXCEPTION, "rpush key failed!", e);
		}
	}

	@Override
	public long lpush(String key, List<String> value) {
		ShardedJedis sJedis = getJedis();
		try {
			long rs = sJedis.lpush(key, value.toArray(new String[0]));
			this.jedisPool.returnResource(sJedis);
			return rs;
		} catch (Exception e) {
			this.jedisPool.returnBrokenResource(sJedis);
			errorLog.error("lpush error:" + key, e);
			throw new MonitoredException(ExceptionConst.REDIS_EXCEPTION, "lpush key failed!", e);
		}
	}

	@PreDestroy
	public void releaseResource() {
		this.jedisPool.destroy();
		logger.info("jedis poll released!");
	}

	@Override
	public Long zrem(final String key, final String... values) {
		final Long[] rs = new Long[1];
		this.useJedis(new JedisCallback() {
			@Override
			public void useJedis(ShardedJedis jedis) {
				Jedis shareJedis = jedis.getShard(key);
				rs[0] = shareJedis.zrem(key, values);
			}
		});
		return rs[0];
	}

	@Override
	public Long zremrangeByRank(final String key, final long start, final long end) {
		final Long[] rs = new Long[1];
		this.useJedis(new JedisCallback() {
			@Override
			public void useJedis(ShardedJedis jedis) {
				Jedis shareJedis = jedis.getShard(key);
				rs[0] = shareJedis.zremrangeByRank(key, start, end);
			}
		});
		return rs[0];
	}

	public Map<String, String> gets(final Collection<String> keys) {
		if (CollectionUtils.isEmpty(keys)) {
			return Collections.emptyMap();
		}
		final List<Future<Map<String, String>>> futures = new LinkedList<Future<Map<String, String>>>();

		final Map<String, String> rs = new HashMap<String, String>();

		this.useJedis(new JedisCallback() {
			@Override
			public void useJedis(ShardedJedis jedis) {
				final Map<Jedis, List<String>> batchMap = new HashMap<Jedis, List<String>>();
				for (String key : keys) {
					Jedis shard = jedis.getShard(key);
					List<String> batchKeys = batchMap.get(shard);
					if (batchKeys == null) {
						batchKeys = new ArrayList<String>();
						batchMap.put(shard, batchKeys);
					}
					batchKeys.add(key);
				}
				for (final Jedis key : batchMap.keySet()) {
					final String[] array = batchMap.get(key).toArray(new String[0]);
					futures.add(threadPool.submit(new Callable<Map<String, String>>() {
						@Override
						public Map<String, String> call() throws Exception {
							Map<String, String> hit = new HashMap<String, String>();
							List<String> values = key.mget(array);
							for (int i = 0; i < array.length; i++) {
								String count = values.get(i);
								if (StringUtils.isNotBlank(count)) {
									hit.put(array[i], count);
								}
							}
							return hit;
						}

					}));
				}
				for (Future<Map<String, String>> f : futures) {
					try {
						rs.putAll(f.get());
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (ExecutionException e) {
						e.printStackTrace();
					}
				}
			}
		});
		return rs;
	}

	public static void main(String[] args) throws Exception{
		// code to check if the size of tow ordered followers are same.
		System.out.println("xxxx");
		RedisServiceImpl service = new RedisServiceImpl();
//		service.password = "music_redis_123";
//		service.masters = "app6100;app6101;app6102;app6103;app6104";
//		service.sentinels="10.160.129.75:26100;10.160.129.76:26100;172.17.3.246:26379";
//		service.timeout=2000;
//		service.maxActive = 10;
		
//				comment.redis.master=music31.photo.163.org
//				comment.redis.port=6379
//				comment.redis.maxActive=10
//				comment.redis.timeout=5000
//				comment.redis.password=U9dnHd8
		service.password = "U9dnHd8";
		service.masters = "music31.photo.163.org:6379";
		service.timeout=2000;
		service.maxActive = 10;
		
		
		service.init();
		service.set("test_nyle", "test123");
		System.out.println(service.get("test_nyle"));
//		System.out.println(service.get("EventServiceImpl_getRcmdEventInfoCacheKey_"+7479357));
	}

	@Override
	public CounterSet newCounterSet(String key, int expireSeconds, long sizeLimit) {
		return new CounterSet(this, key, expireSeconds, sizeLimit);
	}

	@Override
	public RedisList newRedisList(String key, int expireSeconds, long sizeLimit) {
		return new RedisList(this, key, expireSeconds, sizeLimit);
	}

	@Override
	public String lpop(String key) {
		ShardedJedis sJedis = getJedis();
		try {
			String value = sJedis.lpop(key);
			this.jedisPool.returnResource(sJedis);
			return value;
		} catch (Exception e) {
			this.jedisPool.returnBrokenResource(sJedis);
			errorLog.error("lpop error,key:" + key);
			throw new MonitoredException(ExceptionConst.REDIS_EXCEPTION, "lpop key failed!", e);
		}
	}

	@Override
	public Long sadd(String key, String value) {
		ShardedJedis sJedis = getJedis();
		try {
			Long result = sJedis.getShard(key).sadd(key, value);
			this.jedisPool.returnResource(sJedis);
			return result;
		} catch (Exception e) {
			this.jedisPool.returnBrokenResource(sJedis);
			errorLog.error("sadd error,key:" + key);
			throw new MonitoredException(ExceptionConst.REDIS_EXCEPTION, "lpop key failed!", e);
		}
	}

	@Override
	public Long sadd(String key, List<String> values) {
		ShardedJedis sJedis = getJedis();
		try {
			Long result = sJedis.getShard(key).sadd(key, values.toArray(new String[0]));
			this.jedisPool.returnResource(sJedis);
			return result;
		} catch (Exception e) {
			this.jedisPool.returnBrokenResource(sJedis);
			errorLog.error("sadd error,key:" + key);
			throw new MonitoredException(ExceptionConst.REDIS_EXCEPTION, "lpop key failed!", e);
		}
	}

	@Override
	public Long scard(String key) {
		ShardedJedis sJedis = getJedis();
		try {
			Long result = sJedis.getShard(key).scard(key);
			this.jedisPool.returnResource(sJedis);
			return result;
		} catch (Exception e) {
			this.jedisPool.returnBrokenResource(sJedis);
			errorLog.error("scard error,key:" + key);
			throw new MonitoredException(ExceptionConst.REDIS_EXCEPTION, "lpop key failed!", e);
		}
	}

	@Override
	public Set<String> smembers(String key) {
		ShardedJedis sJedis = getJedis();
		try {
			Set<String> result = sJedis.getShard(key).smembers(key);
			this.jedisPool.returnResource(sJedis);
			return result;
		} catch (Exception e) {
			this.jedisPool.returnBrokenResource(sJedis);
			errorLog.error("scard error,key:" + key);
			throw new MonitoredException(ExceptionConst.REDIS_EXCEPTION, "lpop key failed!", e);
		}
	}

	@Override
	public long sremove(String setKey, String... members) {
		ShardedJedis sJedis = getJedis();
		try {
			long v = sJedis.srem(setKey, members);
			this.jedisPool.returnResource(sJedis);
			return v;
		} catch (Exception e) {
			this.jedisPool.returnBrokenResource(sJedis);
			errorLog.error("sremove error,key:" + setKey + " members:" + StringUtils.join(members));
			throw new MonitoredException(ExceptionConst.REDIS_EXCEPTION, "sremove failed!", e);
		}
	}

	@Override
	public Long zrank(String key, String member) {
		ShardedJedis sJedis = getJedis();
		try {
			Long result = sJedis.getShard(key).zrank(key, member);
			this.jedisPool.returnResource(sJedis);
			return result;
		} catch (Exception e) {
			this.jedisPool.returnBrokenResource(sJedis);
			errorLog.error("zrank error,key:" + key);
			throw new MonitoredException(ExceptionConst.REDIS_EXCEPTION, "zrank key failed!", e);
		}
	}

	@Override
	public Map<String, DebugObject> debugObject(String... keys) {
		return Collections.emptyMap();
//		if (keys == null || keys.length == 0) {
//			return Collections.emptyMap();
//		}
//		ShardedJedis sJedis = getJedis();
//		Map<String, DebugObject> dos = new HashMap<String, DebugObject>();
//		try {
//			Jedis shard = sJedis.getShard(keys[0]);
//			Pipeline pipelined = shard.pipelined();
//			List<Response<String> > results = new LinkedList<Response<String>>();
//			for(String key: keys){
//				Response<String> debug = pipelined.debug(DebugParams.OBJECT(key));
//				results.add(debug);
//			}
//			pipelined.sync();
//			int i = 0;
//			for(Response<String> result: results){
//				try{
//					String string = result.get();
//					//Value at:0x7fe7319c6a90 refcount:1 encoding:raw serializedlength:6 lru:1621290 lru_seconds_idle:15980
//					String patten = ".*refcount:(\\d+) encoding:([^ ]+) serializedlength:(\\d+) lru:(\\d+) lru_seconds_idle:(\\d+)";
//					Matcher matcher = Pattern.compile(patten).matcher(string);
//					if (matcher.find()) {
//						DebugObject debugObject = new DebugObject();
//						debugObject.setRefCount(Integer.valueOf(matcher.group(1)));
//						debugObject.setEncoding(matcher.group(2));
//						debugObject.setSerializedlength(Integer.valueOf(matcher.group(3)));
//						debugObject.setLru(Integer.valueOf(matcher.group(4)));
//						debugObject.setLru_seconds_idle(Integer.valueOf(matcher.group(5)));
//						dos.put(keys[i], debugObject);
//					} else {
//						continue;
//					}
//				}catch(JedisDataException ex){
//					if(ex.getMessage().contains("no such key")){
//						continue;
//					}else{
//						throw ex;
//					}
//				} finally {
//					i++;
//				}
//			}
//			this.jedisPool.returnResource(sJedis);
//			return dos;
//		} catch (Exception e) {
//			this.jedisPool.returnBrokenResource(sJedis);
//			throw new MonitoredException(ExceptionConst.REDIS_EXCEPTION, "debugObject key failed!", e);
//		}
	}

	@Override
	public boolean setLock(long timeout,int expire,String key) {
		String key_ = key + "_beauty_lock";
		System.out.println(key_);
		ShardedJedis sJedis = getJedis();
		long nanoTime = System.currentTimeMillis();
		timeout *= MILLI_NANO_TIME;
		boolean lock = false;
		try {
			//在timeout的时间范围内不断轮询锁
			while (System.currentTimeMillis() - nanoTime < timeout) {
				//锁不存在的话，设置锁并设置锁过期时间，即加锁
				if (sJedis.setnx(key_, LOCKED) == 1) {
					sJedis.expire(key_, expire);//设置锁过期时间是为了在没有释放
					//锁的情况下锁过期后消失，不会造成永久阻塞
					lock = true;
					return lock;
				}
				System.out.println("出现锁等待");
				//短暂休眠，避免可能的活锁
				Thread.sleep(3, RandomUtils.nextInt(30));
			} 
		} catch (Exception e) {
			throw new RuntimeException("locking error",e);
		}
		return false;
	}
	
	
	
}
