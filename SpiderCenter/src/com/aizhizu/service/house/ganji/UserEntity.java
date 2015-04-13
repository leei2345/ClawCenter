package com.aizhizu.service.house.ganji;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.impl.client.BasicCookieStore;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.aizhizu.http.HttpMethod;
import com.aizhizu.http.HttpResponseConfig;
import com.aizhizu.http.Method;
import com.aizhizu.util.LoggerUtil;
import com.alibaba.fastjson.JSONObject;

public class UserEntity {
	
	private int index;
	private String name;
	private String passwd;
	private String updateTime;
	private AtomicInteger useCount = new AtomicInteger(0);
	private UserStat stat = UserStat.Normal;
	private BasicCookieStore cookie = null;
	private static final String BaseUrl = "https://passport.ganji.com/login.php?next=http%3A%2F%2Fbj.ganji.com%2Ffang1%2Fa1%2F";
	private static Pattern scriptPattern = Pattern.compile("__hash__ = '(.*?)';");
	
	public UserEntity (String name, String passwd) {
		this.name = name;
		this.passwd = passwd;
	}
	
	public BasicCookieStore getCookie() {
		if (cookie == null) {
			synchronized (this) {
				if (cookie == null) {
					Login();
				}
			}
		}
		return cookie;
	}
	
	public void setCookie (BasicCookieStore cookie) {
		this.cookie = cookie;
	}

	public String getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPasswd() {
		return passwd;
	}
	public void setPasswd(String passwd) {
		this.passwd = passwd;
	}
	
	public void setStatOnUse (int type) {
		if (!UserStat.OnUse.equals(this.stat)) {
			synchronized (this) {
				if (!UserStat.OnUse.equals(this.stat)) {
					if (type == 0) {
						this.stat = UserStat.OnUse;
					} 
				}
			}
		}
	}
	
	public void setStatNotOnUse (UserStat stat) {
		if (!stat.equals(this.stat)) {
			synchronized (this) {
				if (!stat.equals(this.stat)) {
					this.stat = stat;
				}
			}
		}
	}
	
	public UserStat getStat () {
		return this.stat;
	}
	
	public int getCount () {
		int count = useCount.get();
		return count;
	}
	
	public void addCount (int count) {
		useCount.addAndGet(count);
	}
	
	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public boolean Login () {
		boolean res = false;
		LoggerUtil.ClawerLog("web_ganji","[Login][" + name + "][" + useCount.get() + "][Step 1][Get User][Done]");
		HttpMethod method = new HttpMethod("web_ganji");
		method.AddHeader(Method.Get, "Host", "passport.ganji.com");
		method.AddHeader(Method.Get, "User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:35.0) Gecko/20100101 Firefox/35.0");
		method.AddHeader(Method.Get, "Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		method.AddHeader(Method.Get, "Accept-Language", "zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3");
		method.AddHeader(Method.Get, "Accept-Encoding", "gzip, deflate");
		method.AddHeader(Method.Get, "Connection", "keep-alive");
		method.AddHeader(Method.Get, "Cookie", "ganji_xuuid=");
		method.AddHeader(Method.Get, "Cache-Control", "max-age=0");
		String html = method.GetHtml(BaseUrl, HttpResponseConfig.ResponseAsStream);
		if (!StringUtils.isBlank(html)) {
			Document doc = Jsoup.parse(html);
			Element scriptNode = null;
			String hash = "";
			try {
				scriptNode = doc.select("ul#footer > li > script").first();
				String scriptText = scriptNode.html();
				Matcher scriptMatcher = scriptPattern.matcher(scriptText);
				if (scriptMatcher.find()) {
					hash = scriptMatcher.group(1);
				}
			} catch (Exception e) {
				e.printStackTrace();
				UserCenter.SetUserStatusInactive(this, UserStat.Normal);
				LoggerUtil.ClawerLog("web_ganji","[Login][" + name + "][" + useCount.get() + "][Step 2][Get HashCode][Fail]");
				return false;
			}
			LoggerUtil.ClawerLog("web_ganji","[Login][" + name + "][" + useCount.get() + "][Step 2][Get HashCode][Done]");
			if (!StringUtils.isBlank(hash)) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				String firstGetUrl = "https://passport.ganji.com/login.php?callback=jQuery&username=" + name + "&password=" + passwd + "&checkCode=&setcookie=14&second=&parentfunc=&redirect_in_iframe=&next=http%3A%2F%2Fbj.ganji.com%2Ffang1%2F&__hash__=" + hash + "&_=" + System.currentTimeMillis();
				BasicCookieStore cookies = method.getCookieStore();
				HttpMethod step2Me = new HttpMethod("web_ganji", cookies);
				step2Me.AddHeader(Method.Get, "Referer", BaseUrl);
				String firstGetHtml = step2Me.GetHtml(firstGetUrl, HttpResponseConfig.ResponseAsString);
				try {
					if (!StringUtils.isBlank(firstGetHtml)) {
						firstGetHtml = firstGetHtml.replace("jQuery(", "").replace("\"})", "\"}");
					}
					JSONObject firstHtmlObject = JSONObject.parseObject(firstGetHtml);
					int user_id = firstHtmlObject.getIntValue("user_id");
					if (user_id < 1) {
						LoggerUtil.ClawerLog("web_ganji","[Login][" + name + "][" + useCount.get() + "][Step 3][User Login][Fail]");
					} else {
						cookie = step2Me.getCookieStore();
						res = true;
						LoggerUtil.ClawerLog("web_ganji","[Login][" + name + "][" + useCount.get() + "][Step 3][User Login][Done]");
					}
				} catch (Exception e) {
					LoggerUtil.ClawerLog("web_ganji","[Login][" + name + "][" + useCount.get() + "][Step 3][User Login][Error]");
					UserCenter.SetUserStatusInactive(this, UserStat.Normal);
					return false;
				}
			}
		}
		return res;
	}
	
}
