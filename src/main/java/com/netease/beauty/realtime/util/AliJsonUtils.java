package com.netease.beauty.realtime.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class AliJsonUtils {

	/**
	 * 从列表中的json中提取id列表，value的格式是{
			  "msg" : "",
			  "result" : {
			    "list" : [ {
			      "id" : "11024167"
			      }
				]
			    }
			}
	 * @param value
	 * @return
	 * hzliyong
	 */
	public static List<Long> getIdListFromResult(String value) {
		List<Long> idList = new ArrayList<Long>();//返回的id列表
		JSONArray list = getListFromJsonValue(value);
		if (list != null && list.size() > 0) {
			for (Object jo : list) {
				JSONObject jsonObject = (JSONObject) jo;
				if (jsonObject != null) {
					String strId = jsonObject.getString("id");
					Long id = IdEncryptUtils.decryptAsLong(strId);
					if (id != null) {
						idList.add(id);
					}
				}
			}
		}
		return idList;
	}
	
	
	/**
	 * 从json串中获取list
	 * @return
	 * hzliyong
	 */
	private static JSONArray getListFromJsonValue(String value) {
		JSONObject json = JSONObject.parseObject(value);
		JSONObject result = json.getJSONObject("result");
		if (result == null) {
			return null;
		}
		return result.getJSONArray("list");
	}
	
	/**
	 * 从列表中的json中提取id列表，value的格式是{
			  "msg" : "",
			  "result" : [ {
			      "id" : "11024167"
			      }
				]
			}
	 * @param returnValue
	 * @return
	 * hzliyong
	 */
	public static List<Long> getIdListFromResult(Object returnValue) {
		List<Long> idList = new ArrayList<Long>();//返回的id列表
		if (returnValue == null) {
			return idList;
		}
		JSONObject jsonObject = JSONObject.parseObject((String) returnValue);
		JSONArray result = jsonObject.getJSONArray("result");
		if (result != null && result.size() > 0) {
			for (Object o : result) {
				JSONObject obj = (JSONObject) o;
				if (obj != null) {
					String strid = obj.getString("id");
					Long id = IdEncryptUtils.decryptAsLong(strid);
					if (id != null) {
						idList.add(id);
					}
				}
			}
		}
		return idList;
	}
	
	/**
	 * 从列表中的json中提取id列表，value的格式是{
		  "msg" : "",
		  "result" : {
		    "hasNext" : false,
		    "list" : [ {
		      "user" : {
		        "id" : "7275"
		      },
		      "texts" : [ "修容小课堂｜圆脸如何告别络腮胡一样的修容？" ],
		      "imgUrls" : null
		    }]
		  },
		  "code" : 200
		}
	 * @param returnValue
	 * @return
	 * hzliyong
	 */
	public static List<Long> getIdListFromUser(Object returnValue) {
		List<Long> idList = new ArrayList<Long>();//返回的id列表
		if (returnValue == null) {
			return idList;
		}
		JSONObject jsonObject = JSONObject.parseObject((String) returnValue);
		JSONObject result = jsonObject.getJSONObject("result");
		if (result != null) {
			JSONArray list = result.getJSONArray("list");
			if (list != null && list.size() > 0) {
				for (Object object : list) {
					JSONObject jo = (JSONObject) object;
					if (jo != null) {
						JSONObject user = (JSONObject) jo.get("user");
						if (user != null) {
							Long id = user.getLong("id");
							if (id != null) {
								idList.add(id);
							}
						}
					}
				}
			}
		}
		return idList;
		
	}
	
	public static void main(String[] args) {
		List<Long> idList = new ArrayList<Long>();//返回的id列表
		idList.add(22L);
		idList.add(22L);
		idList.add(22L);
	}
}
