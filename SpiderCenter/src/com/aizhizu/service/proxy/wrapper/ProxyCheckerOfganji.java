package com.aizhizu.service.proxy.wrapper;

import org.apache.http.HttpHost;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.aizhizu.service.proxy.BaseProxyChecker;

public class ProxyCheckerOfganji extends BaseProxyChecker {
	private static String indentidy = "web_ganji";
	private static String url = "https://passport.ganji.com/login.php?next=http%3A%2F%2Fbj.ganji.com%2Ffang1%2Fa1%2F";

	public ProxyCheckerOfganji() {
		super(indentidy, url);
	}
	
	@Override
	protected void init() {
		this.AddRequestHeader("Host", "passport.ganji.com");
		this.AddRequestHeader("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:35.0) Gecko/20100101 Firefox/35.0");
		this.AddRequestHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		this.AddRequestHeader("Accept-Language", "zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3");
		this.AddRequestHeader("Accept-Encoding", "gzip, deflate");
		this.AddRequestHeader("Connection", "keep-alive");
		this.AddRequestHeader("Cookie", "ganji_xuuid=");
		this.AddRequestHeader("Cache-Control", "max-age=0");
	}

	public boolean analyze(Object[] objects) {
		boolean res = false;
		int statusLineCode = ((Integer) objects[0]).intValue();
		String html = (String) objects[1];
		Document doc = Jsoup.parse(html);
		String title = doc.select("title").text().trim();
		boolean htmlstat = false;
		if (!title.contains("百度") && !title.contains("302") && title.contains("赶集")) {
			htmlstat = true;
		}
		if ((statusLineCode == 200) && (html.length() > 100) && htmlstat) {
			res = true;
		}
		return res;
	}
	
	public static void main(String[] args) {
		HttpHost proxy = new HttpHost("111.161.126.100", 80);
		BaseProxyChecker b = new ProxyCheckerOfganji();
		b.InstallProxyHost(proxy);
		b.CheckApplicability();
		
	}

}