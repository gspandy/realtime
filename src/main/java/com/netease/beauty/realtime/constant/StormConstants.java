package com.netease.beauty.realtime.constant;

/**
 * storm常量
 * @author hzliyong
 *
 */
public class StormConstants {

	/**
	 * 拓扑相关常量
	 */
	public static final String RAWLOG = "rawLog";//原始日志
	
	public static final String TOPOLOGY_TEST_NAME = "beauty_realtime_test";//测试拓扑名
	
	public static final String TOPOLOGY_ONLINE_NAME = "beauty_realtime_online";//线上拓扑名
	
	public static final String MAIN_SPOUT = "MainSpout";

	public static final String LOG_SPLITTING_BOLT = "sLogSplittingBolt";
	
	/**
	 * da拓扑相关常量
	 */
	public static final String DALOG_SPLITTING_BOLT = "DaLogSplittingBolt";
	
	public static final String DALOG_EXPOSE_BOLT = "DaLogExposeBolt";
	/**
	 * bi拓扑相关常量
	 */
	
	public static final String BILOG_SPLITTING_BOLT = "BiLogSplittingBolt";
	
	public static final String READ_BOLT = "ReadBolt";
	
	
	/**
	 * action事件
	 */
	public static final String CLICK = "click";//点击事件
	
	public static final String READ = "read";//阅读事件
	
	public static final String IMPRESS = "impress";//曝光事件
	
	public static final String GLOBAL = "global";//全局事件

	/**
	 * kafka中的keys
	 */
	public static final String BOOTSTRAP_SERVERS = "bootstrap.servers";
	
	public static final String ENABLE_AUTO_COMMIT = "enable.auto.commit";
	
	public static final String AUTOCOMMIT_INTERVAL_MS = "auto.commit.interval.ms";
	
	public static final String REQUEST_TIMEOUT_MS = "request.timeout.ms";
	
	public static final String HEATBEAT_INTERVAL_MS = "heartbeat.interval.ms";
	
	public static final String SESSION_TIMEOUT_MS = "session.timeout.ms";
	
	public static final String KEY_DESERIALIZER = "key.deserializer";
	
	public static final String VALUE_DESERIALIZER = "value.deserializer";
	
	public static final String GROUP_ID = "group.id";
	
	public static final String TOPIC = "topic";
	
	public static final String BI_TOPIC_PREFIX = "beauty.kafka.biLog";
	
	public static final String DA_TOPIC_PREFIX = "beauty.kafka.daLog";
	
	/**
	 * storm配置中的key
	 */
	public static final String STORM_MESSAGE_TIMEOUTSECS = "storm.message.timeoutSecs";
	
	public static final String STORM_DEBUG = "storm.debug";
	
	public static final String STORM_SEEDS = "storm.seeds";
	
	public static final String STORM_THRIFT_PORT = "storm.thrift.port";
	
	public static final String STORM_NUMWORKERS = "storm.numWorkers";
	
	public static final String STORM_MAX_TASK_PARALLELISM = "storm.max.task.parallelism";
	
	/**
	 * 符号
	 */
	public static final String COMMA = ",";//逗号
	
	
	/**
	 * redis的keys
	 */
	
	public static final String REDIS_MASTER = "app.redis.master";
	
	public static final String REDIS_PORT = "app.redis.port";
	
	public static final String REDIS_MAXACTIVE = "app.redis.maxActive";
	
	public static final String REDIS_TIMEOUT = "app.redis.timeout";
	
	public static final String REDIS_PASSWORD = "app.redis.password";
	
	public static final String REDIS_MAX_IDLE = "app.redis.maxIdle";
	
	public static final String REDIS_MIN_IDLE = "app.redis.minIdle";
}
