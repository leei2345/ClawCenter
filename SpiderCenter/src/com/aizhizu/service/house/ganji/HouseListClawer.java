package com.aizhizu.service.house.ganji;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.aizhizu.dao.Redis;
import com.aizhizu.http.HttpMethod;
import com.aizhizu.http.HttpResponseConfig;
import com.aizhizu.http.Method;
import com.aizhizu.service.Analyst;
import com.aizhizu.service.house.BaseHouseClawer;
import com.aizhizu.util.CountDownLatchUtils;
import com.aizhizu.util.LoggerUtil;

public class HouseListClawer extends BaseHouseClawer {
	private static String identidy = "web_ganji";
	private int pageIndex = 1;

	public HouseListClawer(CountDownLatchUtils cdl, int pageIndex) {
		super(identidy);
		this.cdl = cdl;
		this.pageIndex = pageIndex;
	}
	
	public void run () {
		this.Implement();
	}

	protected String GetHtml() {
		String url = "";
		if (this.pageIndex == 1)
			url = "http://bj.ganji.com/fang1/a1/";
		else {
			url = "http://bj.ganji.com/fang1/a1o" + this.pageIndex + "/";
		}
		HttpMethod me = new HttpMethod(identidy);
		me.AddHeader(Method.Get, "Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		me.AddHeader(Method.Get, "Accept-Encoding", "gzip,deflate,sdch");
		me.AddHeader(Method.Get, "Accept-Language", "zh-CN,zh;q=0.8");
		me.AddHeader(Method.Get, "Cache-Control", "no-cache");
		me.AddHeader(Method.Get, "Connection", "keep-alive");
		me.AddHeader(Method.Get, "Host", "bj.ganji.com");
		me.AddHeader(Method.Get, "User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/37.0.2062.120 Chrome/37.0.2062.120 Safari/537.36");
		String html = me.GetHtml(url, HttpResponseConfig.ResponseAsStream);
		return html;
	}

	protected Map<Analyst, Object> Analysis(String html) {
		if (StringUtils.isBlank(html)) {
			this.analystResult.put(Analyst.Info, "PageNo" + pageIndex + " fail");
			return this.analystResult;
		}
		Document doc = Jsoup.parse(html, "http://bj.ganji.com/");
		Elements houseNodes = doc.select("ul.list-style1 > li");
		int houseNodesSize = houseNodes.size();
		if (houseNodesSize == 0) {
			html = GetHtml();
			doc = Jsoup.parse(html);
			houseNodes = doc.select("ul.list-style1 > li");
		}
		Redis redis = Redis.getInstance();
		for (int nodeIndex = 0; nodeIndex < houseNodes.size(); nodeIndex++) {
			Element houseNode = houseNodes.get(nodeIndex);
			String houseUrl = houseNode.select("div[class=list-mod1] > a").attr("abs:href").trim();
			if ((!redis.hasNewsUrl(houseUrl))&& (!StringUtils.isBlank(houseUrl))) {
				this.taskList.offer(houseUrl);
			}
		}
		LoggerUtil.ClawerLog("[" + identidy + "][list][page " + this.pageIndex + "][tasklist " + this.taskList.size() + "]");
		this.analystResult.put(Analyst.Info, "succ");
		return this.analystResult;
	}

	public static void main(String[] args) {
		BaseHouseClawer b = new HouseListClawer(new CountDownLatchUtils(1), 1);
		new Thread(b).start();
	}
}