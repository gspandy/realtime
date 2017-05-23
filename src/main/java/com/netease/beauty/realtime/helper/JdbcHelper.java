package com.netease.beauty.realtime.helper;

import java.sql.Connection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;


/**
 * 处理DB的相关操作
 * 
 *
 */
public class JdbcHelper {

	private static final Logger logger = LoggerFactory.getLogger(JdbcHelper.class);
	
	private static DriverManagerDataSource dataSource;
	
	private static JdbcTemplate jdbcTemplate;
	
	private static Connection connection;
	
	/**
	 * 初始化jdbc
	 * @param envMap
	 * hzliyong
	 */
	public static void init(Map envMap) {
		logger.info("JdbcHelper初始化");
		String url = (String) envMap.get("mysql.url");
		String userName = (String) envMap.get("mysql.user");
		String password = (String) envMap.get("mysql.password");
		try {
			dataSource = new DriverManagerDataSource(url, userName, password);//初始化dataSource
			jdbcTemplate = new JdbcTemplate(dataSource);//初始化模板
			connection = dataSource.getConnection();
			
		} catch (Exception e) {
			logger.error("JdbcHelper初始化失败", e);
		}
	}
	
	/**
	 * 根据sql执行任意db操作
	 * @param sql
	 * @return
	 * hzliyong
	 */
	public static void executeDB(String sql) {
		jdbcTemplate.execute(sql);
	}
	
	/**
	 * 根据sql新增、修改和删除db
	 * @param sql
	 * @return
	 * hzliyong
	 */
	public static boolean updateDB(String sql) {
		int row = jdbcTemplate.update(sql);
		return row > 0;
	}
	
	/**
	 * 批量新增、修改和删除db
	 * @param sql
	 * @return
	 * hzliyong
	 */
	public static int[] batchUpdateDB(String[] sql) {
		int[] rows = jdbcTemplate.batchUpdate(sql);
		return rows;
	}

	public static JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}
	
}
