package com.netease.beauty.realtime.redis;

import redis.clients.jedis.ShardedJedis;

public interface JedisCallback {
	public void useJedis(ShardedJedis jedis);
}
