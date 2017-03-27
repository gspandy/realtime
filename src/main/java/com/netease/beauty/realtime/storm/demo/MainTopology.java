package com.netease.beauty.realtime.storm.demo;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.topology.TopologyBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netease.beauty.realtime.constant.StormConstants;

/**
 * 构造拓扑
 * @author hzliyong
 *
 */
public class MainTopology {

	private static final Logger logger = LoggerFactory.getLogger(MainTopology.class);
	
	public static void main(String[] args) throws Exception {
		if (args == null || args.length < 2) {
			logger.error("args必须大于等于2，args—— env:[test|online],localMode:[true|false] ");
		}
		String env = args[0];
		boolean online = isOnline(env);//是否为线上环境
		boolean localMode = Boolean.valueOf(args[1]);//是否本地调试
		logger.info("runtime.env.confg = {}, localMode = {}", env, localMode);
		
		//加载配置信息
		Map<String, String> envMap = loadPropertiesConfig("/config/" + env + "/application.properties");
		//加载storm配置
		Config stormConf = buildStormConfig(envMap, env);
		//创建拓扑
		TopologyBuilder topologyBuilder = buildTopologyBuilder();
		
		//提交拓扑
		if (!localMode) {
			try {
				StormSubmitter.submitTopology(online ? StormConstants.TOPOLOGY_ONLINE_NAME : StormConstants.TOPOLOGY_TEST_NAME,
						stormConf, topologyBuilder.createTopology());
			} catch (Exception e) {
				logger.error("提交Topology失败，", e);
				return;
			}
		} else {
			runLocalInMode(stormConf, topologyBuilder);
			return;
		}
	}
	
	/**
	 * 本地运行模式
	 * @param stormConf
	 * @param topologyBuilder
	 * hzliyong
	 */
	private static void runLocalInMode(Config stormConf, TopologyBuilder topologyBuilder) {
		final LocalCluster cluster = new LocalCluster();
		cluster.submitTopology(StormConstants.TOPOLOGY_TEST_NAME, stormConf, topologyBuilder.createTopology());
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				cluster.killTopology(StormConstants.TOPOLOGY_TEST_NAME);
				cluster.shutdown();
			}
		});
		try {
			Thread.sleep(1000000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 创建Topology
	 * @return
	 * hzliyong
	 */
	private static TopologyBuilder buildTopologyBuilder() {
		TopologyBuilder tb = new TopologyBuilder();
		tb.setSpout(StormConstants.MAIN_SPOUT, new MainSpout());//创建主spout
		
		tb.setBolt(StormConstants.LOG_SPLITTING_BOLT, new LogSplittingBolt()).shuffleGrouping(StormConstants.MAIN_SPOUT);
		tb.setBolt(StormConstants.READ_BOLT, new ReadBolt()).shuffleGrouping(StormConstants.LOG_SPLITTING_BOLT, StormConstants.READ);
		return tb;
	}

	/**
	 * 构建storm配置
	 * @param envMap
	 * @param env
	 * @return
	 * hzliyong
	 */
	private static Config buildStormConfig(Map<String, String> envMap, String env) {
		Config config = new Config();
		config.setMessageTimeoutSecs(Integer.valueOf(envMap.get(StormConstants.STORM_MESSAGE_TIMEOUTSECS)));
		config.setDebug(Boolean.valueOf(envMap.get(StormConstants.STORM_DEBUG)));
		config.setNumWorkers(Integer.valueOf(envMap.get(StormConstants.STORM_NUMWORKERS)));
		config.setMaxTaskParallelism(Integer.valueOf(envMap.get(StormConstants.STORM_MAX_TASK_PARALLELISM)));
		config.setEnvironment(envMap);
		if (!"local".equals(env)) {
			config.put(Config.NIMBUS_SEEDS, envMap.get(StormConstants.STORM_SEEDS));
			config.put(Config.NIMBUS_THRIFT_PORT, Integer.valueOf(envMap.get(StormConstants.STORM_THRIFT_PORT)));
		}
		return config;
	}

	/**
	 * 判断是否为online环境
	 * @param env
	 * @return
	 * hzliyong
	 */
	private static boolean isOnline(String env) {
		return "online".equals(env);
	}


	/**
	 * 加载配置信息
	 * @param configPath
	 * @return
	 * @throws IOException
	 * hzliyong
	 */
	private static Map<String, String> loadPropertiesConfig(String configPath) throws IOException {
		Map<String, String> config = new HashMap<String, String>();
		Properties properties = new Properties();
		properties.load(MainTopology.class.getResourceAsStream(configPath));
		for (Object key : properties.keySet()) {
			config.put(key.toString(), properties.get(key).toString());
		}
		return config;
	}
}
