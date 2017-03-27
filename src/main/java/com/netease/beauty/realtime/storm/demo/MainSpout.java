package com.netease.beauty.realtime.storm.demo;

import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.storm.Config;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netease.beauty.realtime.constant.StormConstants;
import com.netease.beauty.realtime.helper.KafkaHelper;

public class MainSpout extends BaseRichSpout {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7702247597306224652L;

	private static final Logger logger = LoggerFactory.getLogger(MainSpout.class);

	private static final Long TIMEOUT = 2000L;//拉取kafka消息的超时时间
	
	private SpoutOutputCollector collector;
	
	private boolean debugEnable = false;//是否允许debug
	
	private KafkaConsumer<String, String> consumer;//kafka消费者
	
	@Override
	public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
		logger.info("MainSpout#open begin");
		this.collector = collector;
		Map envMap = (Map) conf.get(Config.TOPOLOGY_ENVIRONMENT);//获取环境名称
		debugEnable = Boolean.valueOf(envMap.get(StormConstants.STORM_DEBUG).toString());
		consumer = KafkaHelper.getKafkaConsumer(envMap);
		logger.info("MainSpout#open end, topic: {}", envMap.get(StormConstants.TOPIC));
	}

	@Override
	public void nextTuple() {
		try {
			ConsumerRecords<String, String> records = consumer.poll(TIMEOUT);
			if (!records.isEmpty()) {
				for (ConsumerRecord<String, String> record : records) {
					if (record.topic().startsWith(StormConstants.BI_TOPIC_PREFIX)) {
						if (debugEnable) {
							logger.info("获取下一个Tuple，topic:{}, $RAW_LOG: {}", record.topic(), record.value());
						}
//						message = StringEscapeUtils.escapeJava(record.value());
						collector.emit(new Values(record.value()));//发射消息
					}
				}
			}
		} catch (Exception e) {
			logger.error("kafka读取消息失败，e", e);
		}
	}

	/**
	 * Declare the output schema for all the streams of this topology.
	 */
	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields(StormConstants.RAWLOG));
	}
	

	public static void main(String[] args) {
		String topic = "beauty.kafka.biLog.test";
		System.out.println(topic.startsWith("beauty.kafka.biLog"));
	}
}
