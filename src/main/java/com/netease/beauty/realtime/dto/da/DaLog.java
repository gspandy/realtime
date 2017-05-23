package com.netease.beauty.realtime.dto.da;

import java.io.Serializable;

/**
 * da包含的所有字段
 * 
 * 
 *
 */
public class DaLog implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5943727412633043288L;

	private String deviceUdid;

	private String localeCountry;

	private String deviceModel;

	private String deviceResolution;

	private String devicePlatform;

	private String timeZone;

	private long persistedTime;

	private String sdkVersion;

	private String wifiSsid;

	private long occurTime;

	private long time;

	private String eventId;

	private String userId;

	private String deviceOs;

	private String deviceIMEI;

	private long uploadTime;

	private String deviceOsVersion;

	private String deviceCarrier;

	private String ip;

	private String deviceMacAddr;

	private String dataType;

	private String category;

	private String sessionUuid;

	private String localeLanguage;

	private String deviceNetwork;

	private String appVersion;

	private String appChannel;

	private Attributes attributes;

	private int uploadNum;

	private long costTime;

	private String appKey;

	public String getDeviceUdid() {
		return deviceUdid;
	}

	public void setDeviceUdid(String deviceUdid) {
		this.deviceUdid = deviceUdid;
	}

	public String getLocaleCountry() {
		return localeCountry;
	}

	public void setLocaleCountry(String localeCountry) {
		this.localeCountry = localeCountry;
	}

	public String getDeviceModel() {
		return deviceModel;
	}

	public void setDeviceModel(String deviceModel) {
		this.deviceModel = deviceModel;
	}

	public String getDeviceResolution() {
		return deviceResolution;
	}

	public void setDeviceResolution(String deviceResolution) {
		this.deviceResolution = deviceResolution;
	}

	public String getDevicePlatform() {
		return devicePlatform;
	}

	public void setDevicePlatform(String devicePlatform) {
		this.devicePlatform = devicePlatform;
	}

	public String getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}

	public long getPersistedTime() {
		return persistedTime;
	}

	public void setPersistedTime(long persistedTime) {
		this.persistedTime = persistedTime;
	}

	public String getSdkVersion() {
		return sdkVersion;
	}

	public void setSdkVersion(String sdkVersion) {
		this.sdkVersion = sdkVersion;
	}

	public String getWifiSsid() {
		return wifiSsid;
	}

	public void setWifiSsid(String wifiSsid) {
		this.wifiSsid = wifiSsid;
	}

	public long getOccurTime() {
		return occurTime;
	}

	public void setOccurTime(long occurTime) {
		this.occurTime = occurTime;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public String getEventId() {
		return eventId;
	}

	public void setEventId(String eventId) {
		this.eventId = eventId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getDeviceOs() {
		return deviceOs;
	}

	public void setDeviceOs(String deviceOs) {
		this.deviceOs = deviceOs;
	}

	public String getDeviceIMEI() {
		return deviceIMEI;
	}

	public void setDeviceIMEI(String deviceIMEI) {
		this.deviceIMEI = deviceIMEI;
	}

	public long getUploadTime() {
		return uploadTime;
	}

	public void setUploadTime(long uploadTime) {
		this.uploadTime = uploadTime;
	}

	public String getDeviceOsVersion() {
		return deviceOsVersion;
	}

	public void setDeviceOsVersion(String deviceOsVersion) {
		this.deviceOsVersion = deviceOsVersion;
	}

	public String getDeviceCarrier() {
		return deviceCarrier;
	}

	public void setDeviceCarrier(String deviceCarrier) {
		this.deviceCarrier = deviceCarrier;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getDeviceMacAddr() {
		return deviceMacAddr;
	}

	public void setDeviceMacAddr(String deviceMacAddr) {
		this.deviceMacAddr = deviceMacAddr;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getSessionUuid() {
		return sessionUuid;
	}

	public void setSessionUuid(String sessionUuid) {
		this.sessionUuid = sessionUuid;
	}

	public String getLocaleLanguage() {
		return localeLanguage;
	}

	public void setLocaleLanguage(String localeLanguage) {
		this.localeLanguage = localeLanguage;
	}

	public String getDeviceNetwork() {
		return deviceNetwork;
	}

	public void setDeviceNetwork(String deviceNetwork) {
		this.deviceNetwork = deviceNetwork;
	}

	public String getAppVersion() {
		return appVersion;
	}

	public void setAppVersion(String appVersion) {
		this.appVersion = appVersion;
	}

	public String getAppChannel() {
		return appChannel;
	}

	public void setAppChannel(String appChannel) {
		this.appChannel = appChannel;
	}

	public Attributes getAttributes() {
		return attributes;
	}

	public void setAttributes(Attributes attributes) {
		this.attributes = attributes;
	}

	public int getUploadNum() {
		return uploadNum;
	}

	public void setUploadNum(int uploadNum) {
		this.uploadNum = uploadNum;
	}

	public long getCostTime() {
		return costTime;
	}

	public void setCostTime(long costTime) {
		this.costTime = costTime;
	}

	public String getAppKey() {
		return appKey;
	}

	public void setAppKey(String appKey) {
		this.appKey = appKey;
	}


}
