package com.netease.beauty.realtime.storm.dalog;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.util.StringUtils;

import com.netease.beauty.realtime.business.dalog.meta.UserExposeHistory;
import com.netease.beauty.realtime.constant.DaConstants;
import com.netease.beauty.realtime.constant.enums.ResourceTypeEnums;
import com.netease.beauty.realtime.dto.da.Attributes;
import com.netease.beauty.realtime.dto.da.DaLog;
import com.netease.beauty.realtime.helper.JdbcHelper;
import com.netease.beauty.realtime.storm.common.BaseBolt;
import com.netease.beauty.realtime.util.BaseUtils;
import com.netease.beauty.realtime.util.IdEncryptUtils;
import com.netease.beauty.realtime.util.JsonUtils;

/**
 * 处理da的expose事件
 * 
 *
 */
public class DaLogExposeBolt extends BaseBolt {

	private static final Logger logger = LoggerFactory.getLogger(DaLogExposeBolt.class);
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6455392791564730816L;

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void polyExecute(String rawLog, BasicOutputCollector collector) {
		Map<String, Object> logMap = JsonUtils.fromJson(rawLog, Map.class);
		DaLog daLog = buildDaLog(logMap);
		List<Long> items = daLog.getAttributes() == null ? null : daLog.getAttributes().getItems();
		logger.info("MessageHandler#handleDaExpose开始，items：" + items);
		if (CollectionUtils.isEmpty(items)) {//item不存在则不进行持久化
			return;
		}
		List<UserExposeHistory> list = buildHistory(daLog);
		//保存到DB中
		if (!CollectionUtils.isEmpty(list)) {
			batchInsertToDB(list);
		}
	}

	/**
	 * 批量插入数据库
	 * @param list
	 * hzliyong
	 */
	private static void batchInsertToDB(final List<UserExposeHistory> list) {
		List<String> deleteList = new ArrayList<String>();//批量删除语句
		for (UserExposeHistory history : list) {
			if ((StringUtils.isEmpty(history.getPvid()) || "null".equals(history.getPvid()))
					|| history.getItem() == null
					|| (StringUtils.isEmpty(history.getItemType()) || "null".equals(history.getItemType()))) {
				continue;//pvid为空或者item或者itemType都为空则不删除
			}
			String deleteSql = "delete from Beauty_Algo_User_Expose_History where item='" + history.getItem() + 
					"' and itemType='" + history.getItemType() + 
					"' and pvid='" + history.getPvid() + "'";
			deleteList.add(deleteSql);
		}
		//批量删除
		String[] batchSqls = deleteList.toArray(new String[0]);
		if (batchSqls != null && batchSqls.length > 0) {
			JdbcHelper.batchUpdateDB(batchSqls);
		}
//		List<String> addList = new ArrayList<String>();//批量新增语句
//		for (UserExposeHistory history : list) {
//			String insertSql = "insert into Beauty_Algo_User_Expose_History values ("
//					+ "seq," + history.getLogTime() + "," + history.getUserId() + ",'" + history.getDeviceId()
//					+ "','" + history.getSrc() + "','" + history.getSrcType() + "'," + history.getItem()
//					+ ",'" + history.getItemType() + "','" + history.getPage() + "','" + history.getAbtest()
//					+ "','" + history.getPvid() + "','" + history.getOsVersion() + "', null, null)";
//			addList.add(insertSql);
//		}
//		batchSqls = addList.toArray(new String[0]);
//		if (batchSqls != null && batchSqls.length > 0) {
//			JdbcHelper.batchUpdateDB(batchSqls);
//		}
		String addSql = "insert into Beauty_Algo_User_Expose_History ('id', 'logTime', 'userId', "
				+ "'deviceId', 'src', 'srcType', 'item', 'itemType', 'page', 'abtest', 'pvid', 'osVersion', "
				+ "'db_create_time', 'db_update_time)' values (null, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		//批量插入数据
		JdbcHelper.getJdbcTemplate().batchUpdate(addSql, new BatchPreparedStatementSetter() {
			
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				UserExposeHistory history = list.get(i);
				ps.setLong(1, history.getLogTime());
				if (history.getUserId() != null) {
					ps.setLong(2, history.getUserId());
				} else {
					ps.setLong(2, 0);
				}
				ps.setString(3, history.getDeviceId());
				ps.setString(4, history.getSrc());
				ps.setString(5, history.getSrcType());
				ps.setLong(6, history.getItem());
				ps.setString(7, history.getItemType());
				ps.setString(8, history.getPage());
				ps.setString(9, history.getAbtest());
				ps.setString(10, history.getPvid());
				ps.setString(11, history.getOsVersion());
				ps.setDate(12, new Date(new java.util.Date().getTime()));
				ps.setDate(13, new Date(new java.util.Date().getTime()));
			}
			
			@Override
			public int getBatchSize() {
				return list.size();
			}
		});
	}

	/**
	 * 组装dalog
	 * @param logMap
	 * @return
	 * hzliyong
	 */
	private DaLog buildDaLog(Map<String, Object> logMap) {
		DaLog daLog = new DaLog();
		daLog.setAppChannel(String.valueOf(logMap.get(DaConstants.APPCHANNEL)));
		daLog.setAppKey(String.valueOf(logMap.get(DaConstants.APPKEY)));
		daLog.setAppVersion(String.valueOf(logMap.get(DaConstants.APPVERSION)));
		daLog.setCategory(String.valueOf(logMap.get(DaConstants.CATEGORY)));
		daLog.setCostTime(BaseUtils.strObject2Long(logMap.get(DaConstants.COSTTIME)));
		daLog.setDataType(String.valueOf(logMap.get(DaConstants.DATATYPE)));
		daLog.setDeviceCarrier(String.valueOf(logMap.get(DaConstants.DEVICECARRIER)));
		daLog.setDeviceIMEI(String.valueOf(logMap.get(DaConstants.DEVICEIMEI)));
		daLog.setDeviceMacAddr(String.valueOf(logMap.get(DaConstants.DEVICEMACADDR)));
		daLog.setDeviceModel(String.valueOf(logMap.get(DaConstants.DEVICEMODEL)));
		daLog.setDeviceNetwork(String.valueOf(logMap.get(DaConstants.DEVICENETWORK)));
		daLog.setDeviceOs(String.valueOf(logMap.get(DaConstants.DEVICEOS)));
		daLog.setDeviceOsVersion(String.valueOf(logMap.get(DaConstants.DEVICEOSVERSION)));
		daLog.setDevicePlatform(String.valueOf(logMap.get(DaConstants.DEVICEPLATFORM)));
		daLog.setDeviceResolution(String.valueOf(logMap.get(DaConstants.DEVICERESOLUTION)));
		daLog.setDeviceUdid(String.valueOf(logMap.get(DaConstants.DEVICEUDID)));
		daLog.setEventId(String.valueOf(logMap.get(DaConstants.EVENTID)));
		daLog.setIp(String.valueOf(logMap.get(DaConstants.IP)));
		daLog.setLocaleCountry(String.valueOf(DaConstants.LOCALECOUNTRY));
		daLog.setLocaleLanguage(String.valueOf(DaConstants.LOCALELANGUAGE));
		daLog.setOccurTime(BaseUtils.strObject2Long(logMap.get(DaConstants.OCCURTIME)));
		daLog.setPersistedTime(BaseUtils.strObject2Long(logMap.get(DaConstants.PERSISTEDTIME)));
		daLog.setSdkVersion(String.valueOf(logMap.get(DaConstants.SDKVERSION)));
		daLog.setSessionUuid(String.valueOf(logMap.get(DaConstants.SESSIONUUID)));
		daLog.setTime(BaseUtils.strObject2Long(logMap.get(DaConstants.TIME)));
		daLog.setTimeZone(String.valueOf(logMap.get(DaConstants.TIMEZONE)));
		daLog.setUploadNum(BaseUtils.strObject2Integer(logMap.get(DaConstants.UPLOADNUM)));
		daLog.setUploadTime(BaseUtils.strObject2Long(logMap.get(DaConstants.UPLOADTIME)));
		daLog.setUserId(String.valueOf(logMap.get(DaConstants.USERID)));
		daLog.setWifiSsid(String.valueOf(logMap.get(DaConstants.WIFISSID)));
		try {
			daLog.setAttributes(buildAttributes((Map<String, Object>)logMap.get(DaConstants.ATTRIBUTES)));
		} catch (Exception e) {
			logger.error("MessageHandler#buildDaLog解析attribute异常", e);
		}
		return daLog;
	}
	
	/**
	 * 组装attributes
	 * @param logMap
	 * @return
	 * hzliyong
	 */
	private Attributes buildAttributes(Map<String, Object> map) {
		Attributes att = new Attributes();
		att.setDeviceId(String.valueOf(map.get(DaConstants.DEVICEID)));
		att.setItems(buildItems(String.valueOf(map.get(DaConstants.ITEMS)), String.valueOf(map.get(DaConstants.RESOURCETYPE))));
		att.setLogTime(handleLogTime(String.valueOf(map.get(DaConstants.LOGTIME))));
		att.setOsVersion(String.valueOf(map.get(DaConstants.OSVERSION)));
		if (!StringUtils.isEmpty(String.valueOf(map.get(DaConstants.RESOURCETYPE)))
				&& !"null".equals(String.valueOf(map.get(DaConstants.RESOURCETYPE)))) {
			att.setResourceType(ResourceTypeEnums.UNKNOWN.getValueByType(Integer.valueOf(String.valueOf(map.get(DaConstants.RESOURCETYPE)))));
		}
		att.setType(String.valueOf(map.get(DaConstants.TYPE)));
		att.setUid(String.valueOf(map.get(DaConstants.UID)));
		att.setAbtest(String.valueOf(map.get(DaConstants.ABTEST)));
		att.setPvid(String.valueOf(map.get(DaConstants.PVID)));
		att.setSrc(IdEncryptUtils.decryptAsString(String.valueOf(map.get(DaConstants.SRC))));
		att.setSrcType(String.valueOf(map.get(DaConstants.SRCTYPE)));
		return att;
	}
	
	/**
	 * 处理logtime字段，有可能是1489475045.926414这种格式
	 * @param valueOf
	 * @return
	 * hzliyong
	 */
	private String handleLogTime(String logTime) {
		if (logTime.indexOf(".") > 0) {//包含小数点
			double time = Double.valueOf(logTime) * 1000;
			logTime = String.valueOf((long) time);
		}
		return logTime;
	}

	/**
	 * 根据不同的资源类型解析items
	 * @param items
	 * @param resourceType
	 * @return
	 * hzliyong
	 */
	private List<Long> buildItems(String items, String resourceType) {
		List<Long> itemList = new ArrayList<Long>();
		if (StringUtils.isEmpty(items)) {
			return itemList;
		}
		//1.4版本只解析合辑和心得
		if (resourceType.equals(String.valueOf(ResourceTypeEnums.NOTE.getType()))
				|| resourceType.equals(String.valueOf(ResourceTypeEnums.REPO.getType()))) {
			items = items.replaceAll("[\\[\\]]", "").replaceAll("[\\(\\)]", "").replaceAll("\"", "").replaceAll("\n", "");
			String[] arr = items.split(",");
			if (arr != null && arr.length > 0) {
				for (String item : arr) {
					itemList.add(IdEncryptUtils.decryptAsLong(item.trim()));
				}
			}
		}
		return itemList;
	}
	
	/**
	 * dalog并将结果转化成List<UserExposeHistory>
	 * @param daLog
	 * @return
	 * hzliyong
	 */
	private static List<UserExposeHistory> buildHistory(DaLog daLog) {
		Attributes att = daLog.getAttributes();
		List<Long> items = null;
		List<UserExposeHistory> list = new ArrayList<UserExposeHistory>();
		if (att != null && (items = att.getItems()) != null) {
			for (Long item : items) {
				if (!StringUtils.isEmpty(att.getResourceType())
						&& !"null".equals(att.getResourceType())) {//itemType不为空并且不为“null”时才保存
					UserExposeHistory history = new UserExposeHistory();
					history.setAbtest(att.getAbtest());
					history.setDeviceId(att.getDeviceId());
					history.setItem(item);
					history.setItemType(att.getResourceType());
					history.setLogTime(Long.valueOf(att.getLogTime()));
					history.setOsVersion(att.getOsVersion());
					history.setPage(att.getType());
					history.setPvid(att.getPvid());
					history.setSrc(att.getSrc());
					history.setSrcType(att.getSrcType());
					if (!StringUtils.isEmpty(att.getUid())) {
						history.setUserId(Long.valueOf(att.getUid()));
					}
					list.add(history);
				}
			}
		}
		return list;
	}
	
	
	public static void main(String[] args) {
		UserExposeHistory history = new UserExposeHistory();
		history.setAbtest("abtest");
		history.setDeviceId(null);
		history.setItem(123L);
		history.setItemType("itemType");
		history.setLogTime(123345L);
		history.setOsVersion("osVersion");
		history.setPage("page");
		history.setPvid("pvid");
		history.setSrc("src");
		history.setSrcType("srcType");
		history.setUserId(54321L);
		List<UserExposeHistory> list = new ArrayList<UserExposeHistory>();
		list.add(history);
		batchInsertToDB(list);
	}
}
