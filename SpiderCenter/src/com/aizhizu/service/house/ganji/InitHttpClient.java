//package com.aizhizu.service.house.ganji;
//
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//import org.apache.commons.lang3.StringUtils;
//import org.apache.http.impl.client.BasicCookieStore;
//import org.jsoup.Jsoup;
//import org.jsoup.nodes.Document;
//import org.jsoup.nodes.Element;
//
//import com.aizhizu.http.HttpMethod;
//import com.aizhizu.http.HttpResponseConfig;
//import com.aizhizu.http.Method;
//import com.aizhizu.util.LoggerUtil;
//import com.alibaba.fastjson.JSONObject;
//
///**
// * 初始化登陆状态
// * 
// */
//public class InitHttpClient {
//	
//	private static final String identidy = "web_ganji";
//	private static final String BaseUrl = "https://passport.ganji.com/login.php?next=http%3A%2F%2Fbj.ganji.com%2Ffang1%2Fa1%2F";
//	private static Pattern scriptPattern = Pattern.compile("__hash__ = '(.*?)';");
//	private static BasicCookieStore cookieStore = null;
//	public static volatile boolean InitHttpClientStat = false;
//	private static long timeStemp = 0l;
//	private static final long intervalTime = 1200000;
//	private static UserEntity u;
//
//	public static boolean isInitHttpClientStat() {
//		return InitHttpClientStat;
//	}
//	
//	public static void setInitHttpClientStat(boolean initHttpClientStat) {
//		InitHttpClientStat = initHttpClientStat;
//	}
//	
//	public static long getTimeStemp() {
//		return timeStemp;
//	}
//	
//	public static void setTimeStemp(long timeStemp) {
//		InitHttpClient.timeStemp = timeStemp;
//	}
//		
//	public static void Login () {
//		InitHttpClientStat = false;
//		cookieStore = new BasicCookieStore();
//		u = UserCenter.GetNextUser();
//		String user = u.getName();
//		String passwd = u.getPasswd();
//		
//		HttpMethod method = new HttpMethod(identidy);
//		method.AddHeader(Method.Get, "Host", "passport.ganji.com");
//		method.AddHeader(Method.Get, "User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:35.0) Gecko/20100101 Firefox/35.0");
//		method.AddHeader(Method.Get, "Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
//		method.AddHeader(Method.Get, "Accept-Language", "zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3");
//		method.AddHeader(Method.Get, "Accept-Encoding", "gzip, deflate");
//		method.AddHeader(Method.Get, "Connection", "keep-alive");
//		method.AddHeader(Method.Get, "Cookie", "ganji_xuuid=");
//		method.AddHeader(Method.Get, "Cache-Control", "max-age=0");
//		String html = method.GetHtml(BaseUrl, HttpResponseConfig.ResponseAsStream);
//		if (!StringUtils.isBlank(html)) {
//			Document doc = Jsoup.parse(html);
//			Element scriptNode = null;
//			String hash = "";
//			try {
//				scriptNode = doc.select("ul#footer > li > script").first();
//				String scriptText = scriptNode.html();
//				Matcher scriptMatcher = scriptPattern.matcher(scriptText);
//				if (scriptMatcher.find()) {
//					hash = scriptMatcher.group(1);
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//				ResetHttpClientStat(UserStat.Normal);
//				LoggerUtil.ClawerLog("[" + identidy + "][============STEP ONE FAIL============]");
//				return;
//			}
//			LoggerUtil.ClawerLog("[" + identidy + "][============STEP ONE SUCCESS============]");
//			if (!StringUtils.isBlank(hash)) {
//				try {
//					Thread.sleep(1000);
//				} catch (InterruptedException e1) {
//					e1.printStackTrace();
//				}
//				String firstGetUrl = "https://passport.ganji.com/login.php?callback=jQuery&username=" + user + "&password=" + passwd + "&checkCode=&setcookie=14&second=&parentfunc=&redirect_in_iframe=&next=http%3A%2F%2Fbj.ganji.com%2Ffang1%2F&__hash__=" + hash + "&_=" + System.currentTimeMillis();
//				BasicCookieStore cookies = method.getCookieStore();
//				HttpMethod step2Me = new HttpMethod(identidy, cookies);
//				step2Me.AddHeader(Method.Get, "Referer", BaseUrl);
//				String firstGetHtml = step2Me.GetHtml(firstGetUrl, HttpResponseConfig.ResponseAsString);
//				try {
//					if (!StringUtils.isBlank(firstGetHtml)) {
//						firstGetHtml = firstGetHtml.replace("jQuery(", "").replace("\"})", "\"}");
//					}
//					JSONObject firstHtmlObject = JSONObject.parseObject(firstGetHtml);
//					int user_id = firstHtmlObject.getIntValue("user_id");
//					if (user_id < 1) {
//						LoggerUtil.ClawerLog("[" + identidy + "][============STEP TWO FAIL============]");
//					} else {
//						InitHttpClientStat = true;
//						cookieStore = step2Me.getCookieStore();
//						LoggerUtil.ClawerLog("[" + identidy + "][============STEP TWO  SUCCESS============]");
//					}
//				} catch (Exception e) {
//					LoggerUtil.ClawerLog("[" + identidy + "][============STEP TWO FAIL============]");
//					ResetHttpClientStat(UserStat.Normal);
//					return;
//				}
//			}
//		}
//		timeStemp = System.currentTimeMillis();
//	}
//	
//	public static void ResetHttpClientStat (UserStat stat) {
//		InitHttpClientStat = false;
//		cookieStore = new BasicCookieStore();
//		UserCenter.SetUserStatusInactive(stat);
//	}
//	
//	public static BasicCookieStore GetLoginedHttpClient () {
//		long now_time = System.currentTimeMillis();
//		if (now_time - timeStemp > intervalTime) {
//			InitHttpClientStat = false;
//		}
//		if (!InitHttpClientStat) {
//			synchronized (InitHttpClient.class) {
//				if (!InitHttpClientStat) {
//					timeStemp = 0;
//					Login();
//				}
//			}
//		}
//		return cookieStore;
//	}
//	
//	
//	public static String printRuntime (long delayTime) {
//		long min = delayTime/(60*1000);   
//		long sec = (delayTime%(60*1000))/1000;
//		String print = min + "min " + sec + "sec";
//		return print;
//	}
//	
//	
//	public static void main(String[] args) {
////		String url = "http://bj.ganji.com/fang1/1284641292x.htm";
//		Login();
//		HttpMethod me = new HttpMethod("web_ganji");
//		String html = me.GetHtml("http://bj.ganji.com/fang1/1414037776x.htm", HttpResponseConfig.ResponseAsStream);
//		Document doc = Jsoup.parse(html);
//		
//		Element fphoneNode = null;
//		Element ca_idNode = null;
//		Element puidNode = null;
//		String fphoneStr = "";
//		String ca_id = "";
//		String puid = "";
//		try {
//			fphoneNode = doc.select("input#fphone").first();
//			ca_idNode = doc.select("input#ca_id").first();
//			puidNode = doc.select("input#puid").first();
//			fphoneStr = fphoneNode.attr("value").trim();
//			ca_id = ca_idNode.attr("value").trim();
//			puid = puidNode.attr("value").trim();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		String imageUrl = "http://bj.ganji.com/ajax.php?dir=house&module=get_detail_viewer_login_status&fphone=@@&ca_id=##&puid=$$";
//		String sourceImageUrl = imageUrl.replace("@@", fphoneStr).replace("##",ca_id).replace("$$", puid);
//		HttpMethod sourceImageMe = new HttpMethod(identidy, cookieStore);
//		String sourceImage = sourceImageMe.GetHtml(sourceImageUrl,HttpResponseConfig.ResponseAsStream);
//		
////		String text = doc.select("div#contact-phone").text();
//		System.out.println(sourceImage);
//	}
//	
//
//}
