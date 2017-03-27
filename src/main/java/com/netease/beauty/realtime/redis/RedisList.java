package com.netease.beauty.realtime.redis;

import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import com.netease.beauty.realtime.util.RealtimeCollectionUtils;

public class RedisList {
	private final RedisService redisService;
	private final String key;
	private volatile boolean expiredKey = false;
	private int expireSeconds; //必须在key存在的情况下才可以给key设置一个过期时间
	private long limitSize;
	private final Semaphore semp = new Semaphore(1);

	public RedisList(RedisService redisService, String key,int expireSeconds, long limitSize) {
		this.redisService = redisService;
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
		if (size > limitSize) {
			try {
				if (semp.tryAcquire(2, TimeUnit.SECONDS)) {
					//double check
					size = getSize();
					if (size > limitSize) {
						redisService.ltrim(key, 0 ,limitSize);
					}
					semp.release();
				}
			} catch (InterruptedException e) {
				//nothing
			}
		}
	}
	
	public Long lpush(String value) {
		Long lpush = redisService.lpush(key, value);
		if (lpush != null && lpush > 0) {
			checkAndExprieKey();
			checkSizeAndRemoveTail();
		}
		return lpush;
	}

	public long lpush(List<String> value) {
		long lpush = redisService.lpush(key, value);
		if (lpush > 0) {
			checkAndExprieKey();
			checkSizeAndRemoveTail();
		}
		return lpush;
	}


	public long getSize() {
		Long size = redisService.llen(key);
		if (size == null) {
			return 0L;
		}
		return size;
	}

	public List<String> lrange(long start, long end) {
		return redisService.lrange(key, start, end);
	}

	public String fetch() {
		List<String> lrange = redisService.lrange(key, 0, 0);
		if(RealtimeCollectionUtils.isEmpty(lrange)){
			return null;
		}else{
			return lrange.get(0);
		}
	}

	public boolean lrem(int count, String value) {
		return redisService.lrem(key, count, value);
	}

	public boolean expire(int seconds) {
		return redisService.expire(key, seconds);
	}

	public String lpop(){
		return redisService.lpop(key);
	}

}
