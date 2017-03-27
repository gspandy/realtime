package com.netease.beauty.realtime.constant;

public class ExceptionConst {

	// MonitoredException 自定义的code从10000开始，为了避免和musicCode冲突
	public static int INIT_ERROR = 10000;
	public static int REDIS_CONN_NULL = 10001;
	public static int REDIS_EXCEPTION = 10002;
	public static int REDIS_POLL_EXCEPTION = 10003;

}
