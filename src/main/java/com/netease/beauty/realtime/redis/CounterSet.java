package com.netease.beauty.realtime.redis;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.time.DateUtils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.ShardedJedis;

import com.netease.beauty.realtime.util.RealtimeCollectionUtils;

/**
 * @author Zhike
 *	对redis zset的封装，方便使用
 */
public class CounterSet  {
	private final RedisService redisService;
	private final String key;
	private volatile boolean expiredKey = false;
	private int expireSeconds; //必须在key存在的情况下才可以给key设置一个过期时间
	private long limitSize = -1; //限制大小，-1表示没有限制,如果超过大小会把尾部数据删除
	private final Semaphore semp = new Semaphore(1);
	private long deltaSize = 20;


	CounterSet(RedisService redisService,String key,int expireSeconds, long limitSize) {
		this.redisService=redisService;
		this.key = key;
		this.expireSeconds = expireSeconds;
		this.limitSize = limitSize;
	}

	private void checkAndExprieKey(){
		if (!expiredKey && redisService.ttl(key) == -1) {
			expiredKey = redisService.expire(key, expireSeconds);
		}
	}

	private void checkSizeAndRemoveTail(){
		long size = getSize();
		if (limitSize > 0 && size - limitSize > deltaSize) {
			try {
				if (semp.tryAcquire(2, TimeUnit.SECONDS)) {
					//double check
					size = getSize();
					if (size - limitSize > deltaSize) {
						long rank = size - limitSize;
						redisService.zremrangeByRank(key, 0 ,rank);
					}
					semp.release();
				}
			} catch (InterruptedException e) {
				//nothing
			}
		}
	}

	public String getKey() {
		return key;
	}

	public boolean expire(int expireSeconds){
		return redisService.expire(key, expireSeconds);
	}


	public boolean incr(long id){
		Double zincrBy = redisService.zincrBy(key, Long.toString(id), 1);
		if (zincrBy != null && zincrBy > 0) {
			checkAndExprieKey();
			checkSizeAndRemoveTail();
			return true;
		} else {
			return false;
		}
	}

	public boolean incrBy(long id,double score){
		Double zincrBy = redisService.zincrBy(key, Long.toString(id), score);
		if (zincrBy != null && zincrBy > 0) {
			checkAndExprieKey();
			checkSizeAndRemoveTail();
			return true;
		} else {
			return false;
		}
	}

	public boolean add(long id,double score){
		boolean zadd = redisService.zadd(key, Long.toString(id), score);
		if (zadd) {
			checkAndExprieKey();
			checkSizeAndRemoveTail();
		}
		return zadd;
	}

	public boolean add(final Collection<Long> ids,final double score){
		redisService.useJedis(new JedisCallback() {
			@Override
			public void useJedis(ShardedJedis jedis) {
				Jedis targetShard = jedis.getShard(key);
				Pipeline pipelined = targetShard.pipelined();
				for (Long id : ids) {
					pipelined.zadd(key, score, Long.toString(id));
				}
				pipelined.sync();
			}
		});
		checkAndExprieKey();
		checkSizeAndRemoveTail();
		return true;
	}

	public boolean add(final List<SetMeta> metas){
		//http://stackoverflow.com/questions/16697389/why-it-is-so-slow-with-100-000-records-when-using-pipeline-in-redis
		//优化redis couterset 批量操作collections可能过大的情况
		if (RealtimeCollectionUtils.isEmpty(metas)) {
			return true;
		}
		int step = 1000;
		int size = metas.size();
		if (size > step) {
			for (int offset = 0; offset < size; offset+=step) {
				List<SetMeta> subMetas = RealtimeCollectionUtils.subListByLimitAndOffset(metas, step, offset);
				batchAddOnce(subMetas);
			}
		} else {
			batchAddOnce(metas);
		}
		checkAndExprieKey();
		checkSizeAndRemoveTail();
		return true;
	}

	private void batchAddOnce(final List<SetMeta> metas) {
		redisService.useJedis(new JedisCallback() {
			@Override
			public void useJedis(ShardedJedis jedis) {
				Jedis targetShard = jedis.getShard(key);
				Pipeline pipelined = targetShard.pipelined();
				for (SetMeta m : metas) {
					pipelined.zadd(key, m.getScore(), m.getId());
				}
				pipelined.sync();
			}
		});
	}

	public boolean remove(long id){
		Long zrem = redisService.zrem(key, Long.toString(id));
		if(zrem!=null){
			checkAndExprieKey();
			return true;
		}
		return false;
	}
	
	public boolean remove(final List<Long> ids){
		if (RealtimeCollectionUtils.isEmpty(ids)) {
			return true;
		}
		int step = 1000;
		int size = ids.size();
		if (size > step) {
			for (int offset = 0; offset < size; offset+=step) {
				List<Long> subIds = RealtimeCollectionUtils.subListByLimitAndOffset(ids, step, offset);
				batchRemoveOnce(subIds);
			}
		} else {
			batchRemoveOnce(ids);
		}
		checkAndExprieKey();
		checkSizeAndRemoveTail();
		return true;
	}
	
	private void batchRemoveOnce(final List<Long> ids) {
		redisService.useJedis(new JedisCallback() {
			@Override
			public void useJedis(ShardedJedis jedis) {
				Jedis targetShard = jedis.getShard(key);
				Pipeline pipelined = targetShard.pipelined();
				for (Long id:ids) {
					pipelined.zrem(key, Long.toString(id));
				}
				pipelined.sync();
			}
		});
	}

	public double getMaxCount(){
		Set<String> zrevRange = redisService.zrevRange(key, 0, 1);
		if (RealtimeCollectionUtils.isEmpty(zrevRange)) {
			return 0.0;
		}
		long id=0;
		for (String str : zrevRange) {
			id=Long.parseLong(str);
		}
		return getCount(id);
	}

	public long getSize(){
		Long size = redisService.zcard(key);
		if (size==null) {
			return 0;
		}
		return size;
	}

	public List<Long> getIdList(int limit){
		return getIdList(limit, 0);
	}

	public List<Long> getIdList(int limit,int offset){
		Set<String> zrevRange = redisService.zrevRange(key, offset, offset+limit-1);
		if (RealtimeCollectionUtils.isEmpty(zrevRange)) {
			return Collections.emptyList();
		}
		List<Long> idList = new ArrayList<Long>(zrevRange.size());
		for (String str : zrevRange) {
			idList.add(Long.parseLong(str));
		}
		return idList;
	}

	public double getCount(long id){
		Double score = redisService.zScore(key,Long.toString(id));
		if (score == null) {
			score = 0.0;
		}
		return score;
	}

	public static class KeyUtils{
		private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		private KeyUtils() {
			throw new AssertionError("不可实例化类被实例化");
		}

		public static String getYesterdayDateFormat(){
			return dateFormat.format(new Date(System.currentTimeMillis() - DateUtils.MILLIS_PER_DAY));
		}

		public static String getYesterdayKey(String keyPrefix){
			return keyPrefix+"_"+dateFormat.format(new Date(System.currentTimeMillis() - DateUtils.MILLIS_PER_DAY));
		}

		public static String getTodayDateFormat(){
			return dateFormat.format(new Date());
		}

		public static String getTodayKey(String keyPrefix){
			return keyPrefix+"_"+dateFormat.format(new Date(System.currentTimeMillis() - DateUtils.MILLIS_PER_DAY));
		}

	}
}
