package com.netease.beauty.realtime.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.storm.shade.org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import com.netease.beauty.realtime.constant.StormConstants;

/**
 * 封装kafka消费者相关操作
 * 
 *
 */
public class KafkaHelper {

	private static final Logger logger = LoggerFactory.getLogger(KafkaHelper.class);

	private static KafkaConsumer<String, String> consumer;

	public static String[] tps = null;//kafka的topics
	
	/**
	 * 创建kafka consumer
	 * 
	 * @param envMap
	 * @return hzliyong
	 */
	public static KafkaConsumer<String, String> getKafkaConsumer(Map envMap) {
		logger.info("创建kafka consumer开始");
		Properties props = getProperties(envMap);
		if (consumer == null) {
			consumer = new KafkaConsumer<String, String>(props);
		}
		try {
			consumer.subscribe(getTopics(envMap));
		} catch (Exception e) {
			logger.error("创建kafka消费者失败，", e);
			consumer = null;
		}
		logger.info("创建kafka consumer结束");
		return consumer;
	}

	/**
	 * 关闭kafka消费者
	 * @return
	 * hzliyong
	 */
	public static boolean close() {
		if (consumer != null) {
			try {
				consumer.close();
			} catch (Exception e) {
				logger.error("关闭kafka消费者失败,", e);
				return false;
			}
		}
		return true;
	}

	/**
	 * 获取topic列表
	 * 
	 * @param envMap
	 * @return hzliyong
	 */
	private static List<String> getTopics(Map envMap) {
		String topics = envMap.get(StormConstants.TOPIC).toString();
		List<String> topicList = new ArrayList<String>();
		if (StringUtils.isNotBlank(topics)) {
			String[] topicArr = topics.split(StormConstants.COMMA);
			if (topicArr != null && topicArr.length > 0) {
				for (String topic : topicArr) {
					topicList.add(topic);
				}
			}
		}
		return topicList;
	}
	
	/**
	 * 
	 * @param envMap
	 * @return
	 * hzliyong
	 */
	public static String[] getKafkaTopicsAsArray(Map envMap) {
		List<String> topics = getTopics(envMap);
		if (!CollectionUtils.isEmpty(topics)) {
			tps = new String[topics.size()]; 
			for (int i = 0; i < topics.size(); i++) {
				tps[i] = topics.get(i);
			}
			return tps;
		}
		return null;
	}

	/**
	 * 获取properties
	 * 
	 * @param envMap
	 * @return hzliyong
	 */
	private static Properties getProperties(Map envMap) {
		Properties props = new Properties();
		props.put(StormConstants.BOOTSTRAP_SERVERS, envMap.get(StormConstants.BOOTSTRAP_SERVERS).toString());
		props.put(StormConstants.ENABLE_AUTO_COMMIT, envMap.get(StormConstants.ENABLE_AUTO_COMMIT).toString());
		props.put(StormConstants.AUTOCOMMIT_INTERVAL_MS, envMap.get(StormConstants.AUTOCOMMIT_INTERVAL_MS).toString());
		props.put(StormConstants.REQUEST_TIMEOUT_MS, envMap.get(StormConstants.REQUEST_TIMEOUT_MS).toString());
		props.put(StormConstants.HEATBEAT_INTERVAL_MS, envMap.get(StormConstants.HEATBEAT_INTERVAL_MS).toString());
		props.put(StormConstants.SESSION_TIMEOUT_MS, envMap.get(StormConstants.SESSION_TIMEOUT_MS).toString());
		props.put(StormConstants.KEY_DESERIALIZER, envMap.get(StormConstants.KEY_DESERIALIZER).toString());
		props.put(StormConstants.VALUE_DESERIALIZER, envMap.get(StormConstants.VALUE_DESERIALIZER).toString());
		props.put(StormConstants.GROUP_ID, envMap.get(StormConstants.GROUP_ID).toString());
		return props;
	}
	
	public static void main(String[] args) {
		Map<String, String> envMap = new HashMap<String, String>();
		envMap.put(StormConstants.TOPIC, "beauty.kafka.biLog.test,beauty.kafka.daLog.test");
		System.out.println(getKafkaTopicsAsArray(envMap).toString());
	}
}
