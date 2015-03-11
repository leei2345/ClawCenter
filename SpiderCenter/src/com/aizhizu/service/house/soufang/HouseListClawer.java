package com.aizhizu.service.house.soufang;

import com.aizhizu.dao.Redis;
import com.aizhizu.http.HttpMethod;
import com.aizhizu.http.HttpResponseConfig;
import com.aizhizu.http.Method;
import com.aizhizu.service.Analyst;
import com.aizhizu.service.house.BaseHouseListClawer;
import com.aizhizu.util.CountDownLatchUtils;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class HouseListClawer extends BaseHouseListClawer {
	private static String identidy = "web_soufang";
	private static final int pageCount = 5;
	private int pageIndex = 1;

	public HouseListClawer(String filePath, CountDownLatchUtils cdl) {
		super(identidy);
		this.filePath = filePath;
		this.listcdl = cdl;
	}

	protected String GetHtml() {
		String url = "";
		if (this.pageIndex == 1)
			url = "http://zu.fang.com/house/a21/";
		else {
			url = "http://zu.fang.com/house/a21-i3" + this.pageIndex + "/";
		}
		HttpMethod me = new HttpMethod(identidy);
		me.AddHeader(Method.Get, "Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		me.AddHeader(Method.Get, "Accept-Encoding", "deflate,sdch");
		me.AddHeader(Method.Get, "Accept-Language", "zh-CN,zh;q=0.8");
		me.AddHeader(Method.Get, "Cache-Control", "no-cache");
		me.AddHeader(Method.Get, "Connection", "keep-alive");
		me.AddHeader(Method.Get, "Host", "zu.fang.com");
		me.AddHeader(Method.Get, "Pragma", "no-cache");
		me.AddHeader(Method.Get, "User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/37.0.2062.120 Chrome/37.0.2062.120 Safari/537.36");
		String html = me.GetHtml(url, HttpResponseConfig.ResponseAsString);
		return html;
	}

	protected Map<Analyst, Object> Analysis(String html) {
		if (StringUtils.isBlank(html)) {
			this.analystResult.put(Analyst.Info, "PageNo1 fial");
			return this.analystResult;
		}
		do {
			Document doc = Jsoup.parse(html, "http://zu.fang.com/house/");
			Elements houseNodes = doc.select("div.houseList > dl");
			int houseNodesSize = houseNodes.size();
			if (houseNodesSize == 0) {
				html = GetHtml();
				doc = Jsoup.parse(html);
				houseNodes = doc
						.select("div.houseList > dl");
			}
			Redis redis = Redis.getInstance();
			for (int nodeIndex = 0; nodeIndex < houseNodes.size(); nodeIndex++) {
				Element houseNode = houseNodes.get(nodeIndex);
				String houseUrl = houseNode
						.select("dt[class=img rel floatl] > a")
						.attr("abs:href").trim();
				if ((!redis.hasNewsUrl(houseUrl))
						&& (!StringUtils.isBlank(houseUrl))) {
					this.taskList.offer(houseUrl);
				}
			}
			clawerLogger.info("[" + identidy + "][list][page " + this.pageIndex
					+ "][tasklist " + this.taskList.size() + "]");
			this.pageIndex += 1;
			if (this.pageIndex <= pageCount)
				html = GetHtml();
		} while (this.pageIndex <= pageCount);
		this.analystResult.put(Analyst.Info, "succ");
		return this.analystResult;
	}

	public static void main(String[] args) {
		BaseHouseListClawer b = new HouseListClawer("/home/leei/2014100801/",
				new CountDownLatchUtils(1));
		new Thread(b).start();
	}
}