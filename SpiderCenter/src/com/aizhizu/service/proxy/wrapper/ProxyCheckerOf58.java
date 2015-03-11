package com.aizhizu.service.proxy.wrapper;

import org.apache.http.HttpHost;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.aizhizu.service.proxy.BaseProxyChecker;

public class ProxyCheckerOf58 extends BaseProxyChecker {
	private static String indentidy = "web_wuba";
	private static String url = "http://bj.58.com";

	public ProxyCheckerOf58() {
		super(indentidy, url);
	}

	public boolean analyze(Object[] objects) {
		boolean res = false;
		int statusLineCode = ((Integer) objects[0]).intValue();
		String html = (String) objects[1];
		Document doc = Jsoup.parse(html);
		String title = doc.select("title").text().trim();
		boolean htmlstat = false;
		if (!title.contains("百度") && !title.contains("302")
				&& title.contains("58同城")) {
			htmlstat = true;
		}
		if ((statusLineCode == 200) && (html.length() > 100) && htmlstat) {
			res = true;
		}
		return res;
	}

	public static void main(String[] args) {
		HttpHost proxy = new HttpHost("183.207.224.12", 80);
		BaseProxyChecker b = new ProxyCheckerOf58();
		b.InstallProxyHost(proxy);
		b.CheckApplicability();
	}

	@Override
	protected void init() {
		// TODO Auto-generated method stub
		
	}
}