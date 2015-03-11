package com.aizhizu.service.proxy.wrapper;

import org.apache.http.HttpHost;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.aizhizu.service.proxy.BaseProxyChecker;

public class ProxyCheckerOfsoufang extends BaseProxyChecker {
	private static String indentidy = "web_soufang";
	private static String url = "http://zu.fang.com/";

	public ProxyCheckerOfsoufang() {
		super(indentidy, url);
		AddRequestHeader("Accept-Encoding", "deflate");
		AddRequestHeader("Host", "zu.fang.com");
		AddRequestHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		AddRequestHeader("Accept-Language", "zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3");
		AddRequestHeader("Referer", "http://zu.fang.com/house/a21/");
		AddRequestHeader("Connection", "keep-alive");
		AddRequestHeader("Cache-Control", "max-age=0");
	}

	public boolean analyze(Object[] objects) {
		boolean res = false;
		int statusLineCode = ((Integer) objects[0]).intValue();
		String html = (String) objects[1];
		Document doc = Jsoup.parse(html);
		String title = doc.select("title").text().trim();
		boolean htmlstat = false;
		if (!title.contains("百度") && !title.contains("302") && title.contains("搜房网")) {
			htmlstat = true;
		}
		if ((statusLineCode == 200) && (html.length() > 100) && htmlstat) {
			res = true;
		}
		return res;
	}
	
	public static void main(String[] args) {
		//117.184.7.90
		HttpHost proxy = new HttpHost("119.4.115.51", 8090);
		BaseProxyChecker b = new ProxyCheckerOfsoufang();
		b.InstallProxyHost(proxy);
//		b.AddRequestHeader("Accept-Encoding", "deflate");
//		b.AddRequestHeader("Host", "zu.fang.com");
//		b.AddRequestHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
//		b.AddRequestHeader("Accept-Language", "zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3");
//		b.AddRequestHeader("Referer", "http://zu.fang.com/house/a21/");
//		b.AddRequestHeader("Connection", "keep-alive");
//		b.AddRequestHeader("Cache-Control", "max-age=0");
		b.CheckApplicability();
		
	}

	@Override
	protected void init() {
		// TODO Auto-generated method stub
		
	}
}