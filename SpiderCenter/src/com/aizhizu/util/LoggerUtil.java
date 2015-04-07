package com.aizhizu.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerUtil {
	
	private static Logger clawerLogger = LoggerFactory.getLogger("ClawerLogger");
	private static Logger httpLogger = LoggerFactory.getLogger("HttpLogger");
	private static Logger proxyLogger = LoggerFactory.getLogger("ProxyLogger");
	private static Logger dbLogger = LoggerFactory.getLogger("DataBaseLogger");
	private static Logger pushLogger = LoggerFactory.getLogger("PushLogger");

	public static void ClawerLog (String log) {
		clawerLogger.info(log);
	}
	
	public static void PushLog (String log) {
		pushLogger.info(log);
	}
	
	public static void DBLog (String log) {
		dbLogger.info(log);
	}
	
	public static void ProxyLog (String log) {
		proxyLogger.info(log);
	}
	
	public static void HttpInfoLog (String log) {
		httpLogger.info(log);
	}
	
	public static void HttpDebugLog (String log) {
		httpLogger.debug(log);
	}
	
}
