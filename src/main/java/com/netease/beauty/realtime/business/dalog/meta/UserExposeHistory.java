package com.netease.beauty.realtime.business.dalog.meta;

import java.io.Serializable;

import com.netease.print.daojar.meta.annotation.AnnonOfClass;
import com.netease.print.daojar.meta.annotation.AnnonOfField;

/**
 * 用户实时曝光日志
 *
 * @version 2017-3-13 hzsujianan 基础代码编写
 */
@AnnonOfClass(desc = "用户实时曝光日志", tableName = "Beauty_Algo_User_Expose_History", dbCreateTimeName = "db_create_time", dbUpdateTimeName = "db_update_time")
public class UserExposeHistory implements Serializable {
    private static final long serialVersionUID = 8201292879862777854L;

    @AnnonOfField(desc = "主键", primary = true, autoAllocateId = true)
    private long id;

    @AnnonOfField(desc = "日志时间", unsigned = true, notNull = true)
    private Long logTime;

    @AnnonOfField(desc = "用户ID", unsigned = true, notNull = false)
    private Long userId;

    @AnnonOfField(desc = "设备ID", notNull = true)
    private String deviceId;

    @AnnonOfField(desc = "相关推荐的源id/搜索的搜索词", notNull = false)
    private String src;

    @AnnonOfField(desc = "相关推荐的源类型/搜索的搜索类型", notNull = false)
    private String srcType;

    @AnnonOfField(desc = "被展示id", unsigned = true, notNull = true)
    private Long item;

    @AnnonOfField(desc = "被展示id类型 repo：合辑，note：心得，product：产品，brand：品牌，user：用户，label：标签", notNull = true)
    private String itemType;

    @AnnonOfField(desc = "展示位", notNull = true)
    private String page;

    @AnnonOfField(desc = "ABtest ID", notNull = true)
    private String abtest;

    @AnnonOfField(desc = "pvid", notNull = false)
    private String pvid;

    @AnnonOfField(desc = "操作系统版本", notNull = false)
    private String osVersion;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Long getLogTime() {
		return logTime;
	}

	public void setLogTime(Long logTime) {
		this.logTime = logTime;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getSrc() {
		return src;
	}

	public void setSrc(String src) {
		this.src = src;
	}

	public String getSrcType() {
		return srcType;
	}

	public void setSrcType(String srcType) {
		this.srcType = srcType;
	}

	public Long getItem() {
		return item;
	}

	public void setItem(Long item) {
		this.item = item;
	}

	public String getItemType() {
		return itemType;
	}

	public void setItemType(String itemType) {
		this.itemType = itemType;
	}

	public String getPage() {
		return page;
	}

	public void setPage(String page) {
		this.page = page;
	}

	public String getAbtest() {
		return abtest;
	}

	public void setAbtest(String abtest) {
		this.abtest = abtest;
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

}
