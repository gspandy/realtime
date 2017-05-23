package com.netease.beauty.realtime.business.dalog.dto;

import java.io.Serializable;
import java.util.List;

/**
 * 客户端埋点日志中封装的复合对象
 * 
 *
 */
public class Attributes implements Serializable {


    /**
	 * 
	 */
	private static final long serialVersionUID = 414597129391629190L;

	private String srcType;
	
	private String uid;
	
	private String pvid;
    
    private String osVersion;
    
    private List<Long> items;
    
    private String logTime;
    
    private String src;
    
    private String type;
    
    private String abtest;
    
    private String deviceId;
    
    private String resourceType;

	public String getSrcType() {
		return srcType;
	}

	public void setSrcType(String srcType) {
		this.srcType = srcType;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getPvid() {
		return pvid;
	}

	public void setPvid(String pvid) {
		this.pvid = pvid;
	}

	public String getOsVersion() {
		return osVersion;
	}

	public void setOsVersion(String osVersion) {
		this.osVersion = osVersion;
	}

	public List<Long> getItems() {
		return items;
	}

	public void setItems(List<Long> items) {
		this.items = items;
	}

	public String getLogTime() {
		return logTime;
	}

	public void setLogTime(String logTime) {
		this.logTime = logTime;
	}

	public String getSrc() {
		return src;
	}

	public void setSrc(String src) {
		this.src = src;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getAbtest() {
		return abtest;
	}

	public void setAbtest(String abtest) {
		this.abtest = abtest;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getResourceType() {
		return resourceType;
	}

	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}

}
