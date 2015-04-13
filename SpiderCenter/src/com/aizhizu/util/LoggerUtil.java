package com.aizhizu.util;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerUtil {
	
	private static Logger systemLogger = LoggerFactory.getLogger("SystemLogger");
	private static Logger httpLogger = LoggerFactory.getLogger("HttpLogger");
	private static Logger proxyLogger = LoggerFactory.getLogger("ProxyLogger");
	private static Logger dbLogger = LoggerFactory.getLogger("DataBaseLogger");
	private static Logger pushLogger = LoggerFactory.getLogger("PushLogger");
	
	private static Map<String, Logger> clawerLogger = new HashMap<String, Logger>();
	
	static {
		clawerLogger.put("web_wuba", LoggerFactory.getLogger("WuLogger"));
		clawerLogger.put("web_soufang", LoggerFactory.getLogger("SouLogger"));
		clawerLogger.put("web_ganji", LoggerFactory.getLogger("GanLogger"));
		clawerLogger.put("web_anjuke", LoggerFactory.getLogger("AnLogger"));
	}

	public static void ClawerLog (String identidy, String log) {
		Logger logger = clawerLogger.get(identidy);
		logger.info(log);
	}
	
	public static void PushLog (String log) {
		pushLogger.info(log);
	}
	
	public static void InfoLog (String log) {
		systemLogger.info(log);
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
