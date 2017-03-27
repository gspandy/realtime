package com.netease.beauty.realtime.storm.bilog;

import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.netease.beauty.realtime.dto.LogDTO;
import com.netease.beauty.realtime.helper.RedisHelper;
import com.netease.beauty.realtime.storm.common.BaseBolt;

/**
 * 阅读相关的bolt
 * @author hzliyong
 *
 */
public class ReadBolt extends BaseBolt {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4549633474043988678L;
	
	private static final Logger logger = LoggerFactory.getLogger(ReadBolt.class);
	
	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void polyExecute(String rawLog, BasicOutputCollector collector) {
		LogDTO logDTO = null;
		try {
			logDTO = JSON.parseObject(rawLog, LogDTO.class);
		} catch (Exception e) {
			logger.error("ReadBolt#polyExecute错误，", e);
			return;
		}
		RedisHelper.setWithExpireTime(logDTO.getLogTime(), logDTO.getIp(), 10);
		System.out.println("初始剩余时间: " + RedisHelper.ttl(logDTO.getLogTime()));
		RedisHelper.expire(logDTO.getLogTime(), 30);
		System.out.println("修改过期时间后剩余时间: " + RedisHelper.ttl(logDTO.getLogTime()));
		logger.info("ip:{}", logDTO.getIp());
	}
}
