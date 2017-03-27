package com.netease.beauty.realtime.exception;

import java.util.Map;

/**
 * 
 * @author hzliyong
 *
 */
public class RealTimeException extends RuntimeException {


	/**
	 * 
	 */
	private static final long serialVersionUID = -371778878253239216L;

	private int code = -1; // 方便传递错误标识

	private Map<String, Object> data; // 方便传递相关消息

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public Map<String, Object> getData() {
		return data;
	}

	public void setData(Map<String, Object> data) {
		this.data = data;
	}

	public RealTimeException() {
		super();
	}

	public RealTimeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public RealTimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public RealTimeException(int code, String message) {
		super(message);
		this.code = code;
	}

	public RealTimeException(int code, String message, Throwable cause) {
		super(message, cause);
		this.code = code;
	}
	
	public RealTimeException(String message) {
		super(message);
	}

	public RealTimeException(String message, Object... args) {
		super(String.format(message, args));
	}

	public RealTimeException(Throwable cause) {
		super(cause);
	}

}
