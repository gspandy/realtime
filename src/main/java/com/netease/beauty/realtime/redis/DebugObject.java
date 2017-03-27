package com.netease.beauty.realtime.redis;

public class DebugObject {
	 public int refCount;
	 public String encoding;
	 public int serializedlength;
	 public int lru;
	 public int lru_seconds_idle;
	public int getRefCount() {
		return refCount;
	}
	public void setRefCount(int refCount) {
		this.refCount = refCount;
	}
	public int getSerializedlength() {
		return serializedlength;
	}
	public void setSerializedlength(int serializedlength) {
		this.serializedlength = serializedlength;
	}
	public int getLru() {
		return lru;
	}
	public void setLru(int lru) {
		this.lru = lru;
	}
	public int getLru_seconds_idle() {
		return lru_seconds_idle;
	}
	public void setLru_seconds_idle(int lru_seconds_idle) {
		this.lru_seconds_idle = lru_seconds_idle;
	}
	public String getEncoding() {
		return encoding;
	}
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

}
