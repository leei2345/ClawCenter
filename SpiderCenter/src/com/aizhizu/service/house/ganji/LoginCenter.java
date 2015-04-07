//package com.aizhizu.service.house.ganji;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import org.apache.http.impl.client.BasicCookieStore;
//
//import com.aizhizu.tools.util.LoggerUtil;
//
//public class LoginCenter {
//	
//	private static Map<String, LoginHandle> loginMap = new HashMap<String, LoginHandle>();
//	
//	public static BasicCookieStore GetLoginCookie (String userName) {
//		LoginBase loginBase = loginMap.get(host);
//		BasicCookieStore cookie = null;
//		if (loginBase == null) {
//			synchronized (LoginCenter.class) {
//				loginBase = loginMap.get(host);
//				if (loginBase == null) {
//					loginBase = new LoginBase(host);
//					loginMap.put(host, loginBase);
//				}
//			}
//		}
//		LoggerUtil.GanInfoLog("[Init Login Center][" + host + "][" + loginBase.hashCode() + "]");
//		cookie = loginBase.getCookie();
//		return cookie;
//	}
//	
//	public static void ResetUserStat (String host, UserStat stat) {
//		LoginBase loginBase = loginMap.get(host);
//		if (loginBase != null) {
//			synchronized (LoginCenter.class) {
//				if (loginBase != null) {
//					loginBase = loginMap.get(host);
//					loginBase.CookieSetNull(stat);
//					loginMap.put(host, null);
//				}
//			}
//		}
//		LoggerUtil.GanInfoLog("[Init Login Center][" + host + "][" + loginBase.hashCode() + "]");
//	}
//
//}
