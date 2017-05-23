package com.netease.beauty.realtime.dto;

import java.io.Serializable;

/**
 * 原始日志dto
 * 
 *
 */
public class LogDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3694493104885838888L;

	public String logTime;//操作时间
	
	public String action;//用户动作类型，value可能是read（阅读）、click（点击）、impress（曝光）、global（全局）
	
	public String type;//具体动作
	
	public String uid;//用户id（匿名用户为-1）
	
	public String clientType;//app/web/wap端
	
	public String os;//操作系统，Android、iOS、PC等
	
	public String ip;//ip
	
	public String requestUrl;//请求url
	
	public String longitude;//经度
	
	public String latitude;//纬度
	
	public String deviceModel;//设备
	
	public String carrier;//运营商
	
	public String resolution;//分辨率
	
	public String platform;//平台，如iphone、ipad
	
	public String osVersion;//操作系统版本，如iOs8.0
	
	public String deviceId;//设备Id
	
	public String channel;//app渠道
	
	public String isJail;//iPhone是否越狱（0：否，1：是），安卓为null
	
	public String buildNumber;//构建版本号
	
	public String daId;//da打点的id
	
	public String network;//网络
	
	public String appVersion;//app版本
	
	public String browser;//浏览器
	
	public String cookie;//cookie
	
	public String referer;//web端的上一个页面url
	
	public String resourceType;//点击行为和曝光行为类型
	
	public String source;//页面访问：来源；点击行为：页面；全局事件：来源；曝光事件：位置
	
	public String id;//当前资源的id
	
	public String ids;//当前资源的id列表
	
	public String aid;//作者id
	
	public String cid;//评论/删除评论事件：评论id
	
	public String nid;//评论/删除评论事件：回复评论id
	
	public String oid;//分享事件：分享生成的动态id
	
	public String resId;//长草事件：资源Id
	
	public String accountType;//注册账号类型: 0代表URS、1代表手机号、2代表新浪微博、3代表QQ、4代表微信、5代表豆瓣、8代表易信、1000代表匿名
	
	public String searchType;//搜索类型：1：user是用户搜索、2：experience是心得搜索、3：list是合辑搜索、4：product是产品搜索、5：brand是品牌搜索
	
	public String keyword;//搜索关键词
	
	public String item;//请求结果中包含的内容id，以id的数组形式存储
	
	public String offset;//翻页，每20个内容为1页
	
	public String result;//0：搜索无结果、1：搜索请求成功、2：搜索请求失败
	
	public String actionPosition;//分享：toSina：新浪微博；toTencent：腾讯微博；toQzone：QQ空间；toQQ：QQ；toRenren：人人；toDouban：豆瓣；toNetease：网易微博；toWxsession：微信好友；toWxtimeline：微信朋友圈；toYxsession：易信好友；toYxtimeline：易信朋友圈；toEsthetics：美学
	
	public String isWebView;//分享：0：webView、1：非webView
	
	public String url;//分享：前端传递客户端的参数
	
	public String text;//分享：1：有文字描述、0：无文字描述
	
	public String figure;//分享：0：无图片、1：有图片
	
	public String args;//预留字段
	
	public String pvid;//
	
	public String abtest;//
	
	public String skuId;//
	
	public String skuName;//
	
	public String commentId;//评论id
	
	public String content;//评论内容
	
	public String mediaId;//RepoMedia的id
	
	public String initId;//登录时的initid
	
	public String encrypted;//登录时的encrypted

	public String getLogTime() {
		return logTime;
	}

	public void setLogTime(String logTime) {
		this.logTime = logTime;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getClientType() {
		return clientType;
	}

	public void setClientType(String clientType) {
		this.clientType = clientType;
	}

	public String getOs() {
		return os;
	}

	public void setOs(String os) {
		this.os = os;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getRequestUrl() {
		return requestUrl;
	}

	public void setRequestUrl(String requestUrl) {
		this.requestUrl = requestUrl;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getDeviceModel() {
		return deviceModel;
	}

	public void setDeviceModel(String deviceModel) {
		this.deviceModel = deviceModel;
	}

	public String getCarrier() {
		return carrier;
	}

	public void setCarrier(String carrier) {
		this.carrier = carrier;
	}

	public String getResolution() {
		return resolution;
	}

	public void setResolution(String resolution) {
		this.resolution = resolution;
	}

	public String getPlatform() {
		return platform;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}

	public String getOsVersion() {
		return osVersion;
	}

	public void setOsVersion(String osVersion) {
		this.osVersion = osVersion;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getIsJail() {
		return isJail;
	}

	public void setIsJail(String isJail) {
		this.isJail = isJail;
	}

	public String getBuildNumber() {
		return buildNumber;
	}

	public void setBuildNumber(String buildNumber) {
		this.buildNumber = buildNumber;
	}

	public String getDaId() {
		return daId;
	}

	public void setDaId(String daId) {
		this.daId = daId;
	}

	public String getNetwork() {
		return network;
	}

	public void setNetwork(String network) {
		this.network = network;
	}

	public String getAppVersion() {
		return appVersion;
	}

	public void setAppVersion(String appVersion) {
		this.appVersion = appVersion;
	}

	public String getBrowser() {
		return browser;
	}

	public void setBrowser(String browser) {
		this.browser = browser;
	}

	public String getCookie() {
		return cookie;
	}

	public void setCookie(String cookie) {
		this.cookie = cookie;
	}

	public String getReferer() {
		return referer;
	}

	public void setReferer(String referer) {
		this.referer = referer;
	}

	public String getResourceType() {
		return resourceType;
	}

	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getIds() {
		return ids;
	}

	public void setIds(String ids) {
		this.ids = ids;
	}

	public String getAid() {
		return aid;
	}

	public void setAid(String aid) {
		this.aid = aid;
	}

	public String getCid() {
		return cid;
	}

	public void setCid(String cid) {
		this.cid = cid;
	}

	public String getNid() {
		return nid;
	}

	public void setNid(String nid) {
		this.nid = nid;
	}

	public String getOid() {
		return oid;
	}

	public void setOid(String oid) {
		this.oid = oid;
	}

	public String getResId() {
		return resId;
	}

	public void setResId(String resId) {
		this.resId = resId;
	}

	public String getAccountType() {
		return accountType;
	}

	public void setAccountType(String accountType) {
		this.accountType = accountType;
	}

	public String getSearchType() {
		return searchType;
	}

	public void setSearchType(String searchType) {
		this.searchType = searchType;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public String getItem() {
		return item;
	}

	public void setItem(String item) {
		this.item = item;
	}

	public String getOffset() {
		return offset;
	}

	public void setOffset(String offset) {
		this.offset = offset;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getActionPosition() {
		return actionPosition;
	}

	public void setActionPosition(String actionPosition) {
		this.actionPosition = actionPosition;
	}

	public String getIsWebView() {
		return isWebView;
	}

	public void setIsWebView(String isWebView) {
		this.isWebView = isWebView;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getFigure() {
		return figure;
	}

	public void setFigure(String figure) {
		this.figure = figure;
	}

	public String getArgs() {
		return args;
	}

	public void setArgs(String args) {
		this.args = args;
	}

	public String getPvid() {
		return pvid;
	}

	public void setPvid(String pvid) {
		this.pvid = pvid;
	}

	public String getAbtest() {
		return abtest;
	}

	public void setAbtest(String abtest) {
		this.abtest = abtest;
	}

	public String getSkuId() {
		return skuId;
	}

	public void setSkuId(String skuId) {
		this.skuId = skuId;
	}

	public String getSkuName() {
		return skuName;
	}

	public void setSkuName(String skuName) {
		this.skuName = skuName;
	}

	public String getCommentId() {
		return commentId;
	}

	public void setCommentId(String commentId) {
		this.commentId = commentId;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getMediaId() {
		return mediaId;
	}

	public void setMediaId(String mediaId) {
		this.mediaId = mediaId;
	}

	public String getInitId() {
		return initId;
	}

	public void setInitId(String initId) {
		this.initId = initId;
	}

	public String getEncrypted() {
		return encrypted;
	}

	public void setEncrypted(String encrypted) {
		this.encrypted = encrypted;
	}
	
}

