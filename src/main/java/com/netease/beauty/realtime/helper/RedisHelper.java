package com.netease.beauty.realtime.helper;

import java.util.Map;
import java.util.Set;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import com.netease.beauty.realtime.constant.ExceptionConst;
import com.netease.beauty.realtime.constant.StormConstants;
import com.netease.beauty.realtime.exception.MonitoredException;

/**
 * 封装redis相关操作，storm计算的数据最终流向redis，
 * @author hzliyong
 *
 */
public class RedisHelper {

	private static final Logger logger = LoggerFactory.getLogger(RedisHelper.class);
	
	private static final long EVICTION_RUNS_MILLIS = 20000;
	
	private static final long NUMTESTS_PEREVICTION_RUN = 30000;
	
	private static JedisPool jedisPool;
	
	private static final int DEFAULT_EXPIRE_TIME = 3600 * 24;//默认超时时间
	
	/**
	 * 初始化redis
	 * @param envMap
	 * @return
	 * hzliyong
	 */
	public static boolean init(Map envMap) {
		logger.info("Redis#init，初始化");
		GenericObjectPoolConfig config = getGenericObjectPoolConfig(envMap);
		if (jedisPool == null) {
			if("null".equalsIgnoreCase(envMap.get(StormConstants.REDIS_PASSWORD).toString())){
				jedisPool = new JedisPool(config, envMap.get(StormConstants.REDIS_MASTER).toString(), 
						Integer.valueOf(envMap.get(StormConstants.REDIS_PORT).toString()), 
						Integer.valueOf(envMap.get(StormConstants.REDIS_TIMEOUT).toString()));
			}else{
				jedisPool = new JedisPool(config, envMap.get(StormConstants.REDIS_MASTER).toString(), 
						Integer.valueOf(envMap.get(StormConstants.REDIS_PORT).toString()), 
						Integer.valueOf(envMap.get(StormConstants.REDIS_TIMEOUT).toString()),
						envMap.get(StormConstants.REDIS_PASSWORD).toString());
			}
		}
		return jedisPool != null;
	}
	
	/**
	 * 
	 * @param envMap
	 * @return
	 * hzliyong
	 */
	private static GenericObjectPoolConfig getGenericObjectPoolConfig(Map envMap) {
		GenericObjectPoolConfig config = new GenericObjectPoolConfig();
		config.setMaxTotal(Integer.valueOf(envMap.get(StormConstants.REDIS_MAXACTIVE).toString()));
		config.setMaxIdle(Integer.valueOf(envMap.get(StormConstants.REDIS_MAX_IDLE).toString()));
		config.setMinIdle(Integer.valueOf(envMap.get(StormConstants.REDIS_MIN_IDLE).toString()));
		config.setTestOnBorrow(false);
		config.setTimeBetweenEvictionRunsMillis(EVICTION_RUNS_MILLIS);
		config.setSoftMinEvictableIdleTimeMillis(NUMTESTS_PEREVICTION_RUN);
		config.setNumTestsPerEvictionRun(Integer.valueOf(envMap.get(StormConstants.REDIS_MAXACTIVE).toString()));
		config.setTestWhileIdle(true);
		return config;
	}
	
	/**
	 * 从jedis池中获取jdedis
	 * @return
	 * hzliyong
	 */
	private static Jedis getJedis() {
		try {
			return jedisPool.getResource();
		} catch (Exception e) {
			logger.error("从jedis pool中获取jedis失败", e);
			throw new MonitoredException(ExceptionConst.REDIS_POLL_EXCEPTION, "get jedis failed!", e);
		}
	}
	
	/**
	 * 添加key+value到redis中，默认有效期为DEFAULT_EXPIRE_TIME
	 * @param key
	 * @param value
	 * @return
	 * hzliyong
	 */
	public static boolean set(String key, String value) {
		Jedis jedis = getJedis();
		try {
			String result = jedis.setex(key, DEFAULT_EXPIRE_TIME, value);
			jedisPool.returnResource(jedis);//将资源放回池中
			return "OK".equalsIgnoreCase(result);
		} catch (Exception e) {
			logger.error("添加记录错误，e", e);
			jedisPool.returnBrokenResource(jedis);
			throw new MonitoredException(ExceptionConst.REDIS_EXCEPTION, "设置Redis失败", e);
		}
	}
	
	/**
	 * 超时时间为seconds
	 * @param key
	 * @param value
	 * @param seconds
	 * @return
	 * hzliyong
	 */
	public static boolean setWithExpireTime(String key, String value, int seconds) {
		Jedis jedis = getJedis();
		try {
			String result = jedis.setex(key, seconds, value);
			jedisPool.returnResource(jedis);
			return "OK".equalsIgnoreCase(result);
		} catch (Exception e) {
			logger.error("setWithExpireTime失败，", e);
			jedisPool.returnBrokenResource(jedis);
			throw new MonitoredException(ExceptionConst.REDIS_EXCEPTION, "setWithExpireTime失败", e);
		}
	}
	
	/**
	 * 如果key存在，则不添加，否则添加
	 * 返回的结果：1表示添加成功，0表示未添加
	 * @param key
	 * @param value
	 * @return
	 * hzliyong
	 */
	public static Long setnx(String key, String value) {
		Jedis jedis = getJedis();
		try {
			Long code = jedis.setnx(key, value);
			jedisPool.returnResource(jedis);
			return code;
		} catch (Exception e) {
			logger.error("setnx失败，", e);
			jedisPool.returnBrokenResource(jedis);
			throw new MonitoredException(ExceptionConst.REDIS_EXCEPTION, "setnx失败", e);
		}
	}
	
	/**
	 * 重置超时时间，重置值为seconds
	 * @param mark
	 * @param value
	 * @param seconds
	 * @return
	 * hzliyong
	 */
	public static boolean setNoConflictMark(String mark, String value, int seconds) {
		Jedis jedis = getJedis();
		try {
			Long code = jedis.setnx(mark, value);
			boolean result = false;
			if (code != null && code == 1) {
				Long expire = jedis.expire(mark, seconds);
				if (expire != null && expire > 0) {//重置成功
					result = true;
				} else {
					jedis.del(mark);
					result = false;
				}
			}
			jedisPool.returnResource(jedis);
			return result;
		} catch (Exception e) {
			logger.error("setNoConflictMark失败，", e);
			jedisPool.returnBrokenResource(jedis);
			throw new MonitoredException(ExceptionConst.REDIS_EXCEPTION, "setNoConflictMark失败，", e);
		}
	}
	
	/**
	 * 根据key获取value
	 * @param key
	 * @return
	 * hzliyong
	 */
	public static String get(String key) {
		Jedis jedis = getJedis();
		try {
			String value = jedis.get(key);
			jedisPool.returnResource(jedis);
			return value;
		} catch (Exception e) {
			logger.error("get失败", e);
			jedisPool.returnBrokenResource(jedis);
			throw new MonitoredException(ExceptionConst.REDIS_EXCEPTION, "get失败", e);
		}
	}
	
	/**
	 * 根据key删除缓存中的记录
	 * @param key
	 * @return
	 * hzliyong
	 */
	public static boolean del(String key) {
		Jedis jedis = getJedis();
		try {
			long count = jedis.del(key);
			jedisPool.returnResource(jedis);
			return true;
		} catch (Exception e) {
			logger.error("del失败", e);
			jedisPool.returnBrokenResource(jedis);
			throw new MonitoredException(ExceptionConst.REDIS_EXCEPTION, "del失败", e);
		}
	}
	
	/**
	 * 为哈希表 key 中的域 field 的值加上增量 increment 
	 * 增量也可以为负数，相当于对给定域进行减法操作。
	 * 如果 key 不存在，一个新的哈希表被创建并执行 HINCRBY 命令。
	 * 如果域 field 不存在，那么在执行命令前，域的值被初始化为 0 。
	 * 对一个储存字符串值的域 field 执行 HINCRBY 命令将造成一个错误。
	 * 本操作的值被限制在 64 位(bit)有符号数字表示之内。
	 * @param key
	 * @param field
	 * @param value
	 * @return
	 * hzliyong
	 */
	public static Long hincrBy(String key, String field, long value) {
		Jedis jedis = getJedis();
		try {
			Long result = jedis.hincrBy(key, field, value);
			jedisPool.returnResource(jedis);
			return result;
		} catch (Exception e) {
			logger.error("hincrBy失败", e);
			jedisPool.returnBrokenResource(jedis);
			throw new MonitoredException(ExceptionConst.REDIS_EXCEPTION, "hincrBy失败", e);
		}
	}
	
	/**
	 * 
	 * @param key
	 * @param field
	 * @param value
	 * @return
	 * hzliyong
	 */
	public static Double zincrBy(String key, String field, double value) {
		Jedis jedis = getJedis();
		try {
			Double result = jedis.zincrby(key, value, field);
			jedisPool.returnResource(jedis);
			return result;
		} catch (Exception e) {
			logger.error("zincrBy失败", e);
			jedisPool.returnBrokenResource(jedis);
			throw new MonitoredException(ExceptionConst.REDIS_EXCEPTION, "zincrBy失败", e);
		}
	}
	
	/**
	 * 根据key获取值集合
	 * @param key
	 * @return
	 * hzliyong
	 */
	public static Set<String> hkeys(String key) {
		Jedis jedis = getJedis();
		try {
			Set<String> result = jedis.hkeys(key);
			jedisPool.returnResource(jedis);
			return result;
		} catch (Exception e) {
			jedisPool.returnBrokenResource(jedis);
			logger.error("hkeys error " + key, e);
			throw new MonitoredException(ExceptionConst.REDIS_EXCEPTION, "hkeys key failed!", e);
		}
	}
	
	/**
	 * 根据key和field获取值
	 * @param key
	 * @param field
	 * @return
	 * hzliyong
	 */
	public static String hget(String key, String field) {
		Jedis jedis = getJedis();
		try {
			String result = jedis.hget(key, field);
			jedisPool.returnResource(jedis);
			return result;
		} catch (Exception e) {
			logger.error("hget error " + key + "," + field, e);
			jedisPool.returnBrokenResource(jedis);
			throw new MonitoredException(ExceptionConst.REDIS_EXCEPTION, "hget key failed!", e);
		}
	}
	
	/**
	 * 增加key的值，每次加1，初始为0
	 * @param key
	 * @return
	 * hzliyong
	 */
	public static Long incr(String key) {
		Jedis jedis = getJedis();
		try {
			Long result = jedis.incr(key);
			jedisPool.returnResource(jedis);
			return result;
		} catch (Exception e) {
			logger.error("incr error :" + key, e);
			jedisPool.returnBrokenResource(jedis);
			throw new MonitoredException(ExceptionConst.REDIS_EXCEPTION, "incr key failed!", e);
		}
	}

	/**
	 * key对应的值每次增加incr
	 * @param key
	 * @param incr
	 * @return
	 * hzliyong
	 */
	public static Long incrBy(String key, int incr) {
		Jedis jedis = getJedis();
		try {
			Long result = jedis.incrBy(key, incr);
			jedisPool.returnResource(jedis);
			return result;
		} catch (Exception e) {
			logger.error("incrby error :" + key + "," + incr, e);
			jedisPool.returnBrokenResource(jedis);
			throw new MonitoredException(ExceptionConst.REDIS_EXCEPTION, "incrBy key failed!", e);
		}
	}
	
	/**
	 * 根据key，将值从左侧插入
	 * @param key
	 * @param value
	 * @return
	 * hzliyong
	 */
	public static Long lpush(String key, String value) {
		Jedis jedis = getJedis();
		try {
			Long length = jedis.lpush(key, value);
			jedisPool.returnResource(jedis);
			return length;
		} catch (Exception e) {
			jedisPool.returnBrokenResource(jedis);
			logger.error("lpush error,key:" + key + ",value:" + value, e);
			throw new MonitoredException(ExceptionConst.REDIS_EXCEPTION, "lpush key failed!", e);
		}
	}

	/**
	 * 根据key，将多个值从左侧插入
	 * @param key
	 * @param values
	 * @return
	 * hzliyong
	 */
	public static Long lpush(String key, String... values) {
		Jedis jedis = getJedis();
		try {
			Long length = jedis.lpush(key, values);
			jedisPool.returnResource(jedis);
			return length;
		} catch (Exception e) {
			jedisPool.returnBrokenResource(jedis);
			logger.error("lpush error,key:" + key + ",value:" + values, e);
			throw new MonitoredException(ExceptionConst.REDIS_EXCEPTION, "lpush key failed!", e);
		}
	}
	
	/**
	 * 判断key是否存在
	 * @param key
	 * @return
	 * hzliyong
	 */
	public static boolean exists(String key) {
		Jedis jedis = getJedis();
		try {
			boolean b = jedis.exists(key);
			jedisPool.returnResource(jedis);
			return b;
		} catch (Exception e) {
			jedisPool.returnBrokenResource(jedis);
			logger.error("exists error,key:" + key, e);
			throw new MonitoredException(ExceptionConst.REDIS_EXCEPTION, "exists key failed!", e);
		}
	}
	
	/**
	 * 对key设置过期时间，key的过期时间从设置后开始的seconds秒后过期。
	 * 比如之前的还剩8秒过期，然后设置过期时间是30秒，则从设置后的30秒后过期
	 * @param key
	 * @param seconds
	 * @return
	 * hzliyong
	 */
	public static boolean expire(String key, int seconds) {
		Jedis jedis = getJedis();
		try {
			boolean b = jedis.expire(key, seconds) == 1;
			jedisPool.returnResource(jedis);
			return b;
		} catch (Exception e) {
			logger.error("expire error,key:" + key, e);
			jedisPool.returnBrokenResource(jedis);
			throw new MonitoredException(ExceptionConst.REDIS_EXCEPTION, "expire key failed!", e);
		}
	}
	
	/**
	 * 获取key的剩余时间
	 * 如果key未设置过期时间则返回-1
	 * 如果key不存在，则返回-2
	 * @param key
	 * @return
	 * hzliyong
	 */
	public static long ttl(String key) {
		Jedis jedis = getJedis();
		try {
			Long ttl = jedis.ttl(key);
			jedisPool.returnResource(jedis);
			if (ttl == null) {
				return -1L;
			}
			return ttl;
		} catch (Exception e) {
			logger.error("ttl错误", e);
			jedisPool.returnBrokenResource(jedis);
			throw new MonitoredException(ExceptionConst.REDIS_EXCEPTION, "ttl错误", e);
		}
	}
	
	/**
	 * 
	 * 关闭jedis
	 * hzliyong
	 */
	public static void close() {
		Jedis jedis = getJedis();
		try {
			if (jedis != null) {
				jedis.close();
			}
			if (jedisPool != null) {
				jedisPool.close();
			}
		} catch (Exception e) {
			logger.error("close失败", e);
			throw new MonitoredException(ExceptionConst.REDIS_EXCEPTION, "close错误", e);
		}
	}
}
