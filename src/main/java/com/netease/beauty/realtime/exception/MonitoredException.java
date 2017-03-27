package com.netease.beauty.realtime.exception;


@SuppressWarnings("serial")
public class MonitoredException extends RealTimeException {
	public MonitoredException(int code, String msg) {
		super(code, msg);
	}

	public MonitoredException(int code, String msg, Exception ex) {
		super(code, msg, ex);
	}
}
