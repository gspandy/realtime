package com.netease.beauty.realtime.storm.demo;

import java.util.Map;

import org.apache.storm.Config;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netease.beauty.realtime.constant.StormConstants;
import com.netease.beauty.realtime.helper.RedisHelper;

/**
 * 具体业务bolt的基类，da日志和bi日志的bolt的基类
 * 
 *
 */
public abstract class BaseBolt extends BaseBasicBolt {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8482740018578964338L;
	
	private static final Logger logger = LoggerFactory.getLogger(BaseBolt.class);
	
	private boolean debugEnable;
	
	@Override
	public void prepare(Map conf, TopologyContext context) {
		Map envMap = (Map) conf.get(Config.TOPOLOGY_ENVIRONMENT);
		debugEnable = Boolean.valueOf(envMap.get(StormConstants.STORM_DEBUG).toString());
		//初始化redis
		RedisHelper.init(envMap);
	}
	
	/**
	 * 执行业务逻辑
	 */
	@Override
	public void execute(Tuple input, BasicOutputCollector collector) {
		String rawLog = (String) input.getValueByField(StormConstants.RAWLOG);
		if (debugEnable) {
			logger.info("execute，RAW_LOG:{}", rawLog);
		}
		System.out.println("log: " + rawLog);
		polyExecute(rawLog, collector);
	}

	/**
	 * 清理操作
	 */
	@Override
	public void cleanup() {
		
	}

	/**
	 * 不同的bolt执行不同的逻辑
	 * @param rawLog
	 * @param collector
	 * hzliyong
	 */
	protected abstract void polyExecute(String rawLog, BasicOutputCollector collector);
}
