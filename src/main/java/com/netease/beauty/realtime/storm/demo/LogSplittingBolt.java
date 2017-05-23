package com.netease.beauty.realtime.storm.demo;

import java.util.Map;

import org.apache.storm.Config;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.netease.beauty.realtime.constant.StormConstants;

/**
 * 解析日志的bolt
 * 
 *
 */
public class LogSplittingBolt extends BaseRichBolt {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3517293765471743289L;

	private static final Logger logger = LoggerFactory.getLogger(LogSplittingBolt.class);
	
	private OutputCollector collector;
	
	private boolean debugEnable;
	
	@Override
	public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
		this.collector = collector;
		Map envMap = (Map) stormConf.get(Config.TOPOLOGY_ENVIRONMENT);
		debugEnable = Boolean.valueOf(envMap.get(StormConstants.STORM_DEBUG).toString());
	}

	@Override
	public void execute(Tuple input) {
		Object rawLog = input.getValueByField(StormConstants.RAWLOG);
		if (debugEnable) {
			logger.info("LogSplitting#execute, rawLog:{}", rawLog);
		}
		JSONObject message = null;
		try {
			message = JSON.parseObject((String) rawLog);//转化成JSONObject对象，为了获取action中的值从而发射到不同的stream中
		} catch (Exception e) {
			logger.error("解析rawLog错误，rawLog:{}", rawLog, e);
			return;
		}
		collector.emit(message.getString("action"), new Values(rawLog));//按照action进行分组，此处的action值与下面的declareOutputFields方法中的streamid对相应，如action='read'的对应下列的StormConstants.READ
	}

	/**
	 * 定义多个stream发送
	 */
	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declareStream(StormConstants.CLICK, new Fields(StormConstants.RAWLOG));
		declarer.declareStream(StormConstants.READ, new Fields(StormConstants.RAWLOG));
		declarer.declareStream(StormConstants.IMPRESS, new Fields(StormConstants.RAWLOG));
		declarer.declareStream(StormConstants.GLOBAL, new Fields(StormConstants.RAWLOG));
	}

}
