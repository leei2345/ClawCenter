package com.aizhizu.test.house;

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
	private static String identidy = "web_lm111";
	private static final int pageCount = 5;
	private int pageIndex = 1;

	public HouseListClawer(CountDownLatchUtils cdl, int pageIndex) {
		super(identidy);
		this.pageIndex = pageIndex;
	}

	protected String GetHtml() {
		String url = "http://www.lm111.com/rent/list_0_0_0-0_0_" + pageIndex + "_.html";
		HttpMethod me = new HttpMethod(identidy);
		me.AddHeader(Method.Get, "Host", "www.lm111.com");
		me.AddHeader(Method.Get, "User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:34.0) Gecko/20100101 Firefox/34.0");
		me.AddHeader(Method.Get, "Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		me.AddHeader(Method.Get, "Accept-Language","zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3");
		me.AddHeader(Method.Get, "Accept-Encoding", "gzip, deflate");
		me.AddHeader(Method.Get, "Connection", "keep-alive");
		me.AddHeader(Method.Get, "Cache-Control", "max-age=0");
		String html = me.GetHtml(url, HttpResponseConfig.ResponseAsStream);
		return html;
	}

	protected Map<Analyst, Object> Analysis(String html) {
		if (StringUtils.isBlank(html)) {
			this.analystResult.put(Analyst.Info, "PageNo1 fial");
			return this.analystResult;
		}
		do {
			Document doc = Jsoup.parse(html, "http://www.lm111.com/rent/");
			Elements houseNodes = doc.select("ul.list > li");
			int houseNodesSize = houseNodes.size();
			if (houseNodesSize == 0) {
				html = GetHtml();
				doc = Jsoup.parse(html, "http://www.lm111.com/rent/");
				houseNodes = doc.select("ul.list > li");
			}
			Redis redis = Redis.getInstance();
			for (int nodeIndex = 0; nodeIndex < houseNodes.size(); nodeIndex++) {
				Element houseNode = houseNodes.get(nodeIndex);
				String houseUrl = houseNode.select("div.list01 > a").attr("abs:href").trim();
				if (!redis.hasNewsUrl(houseUrl)) {
					if (!taskList.contains(houseUrl)) {
						this.taskList.offer(houseUrl);
					}
				}
			}
			LoggerUtil.ClawerLog("[" + identidy + "][list][page " + this.pageIndex
					+ "][tasklist " + this.taskList.size() + "]");
			this.pageIndex += 1;
			if (this.pageIndex <= pageCount)
				html = GetHtml();
		} while (this.pageIndex <= pageCount);
		this.analystResult.put(Analyst.Info, "succ");
		return this.analystResult;
	}

	public static void main(String[] args) {
		BaseHouseClawer b = new HouseListClawer(new CountDownLatchUtils(1), 1);
		new Thread(b).start();
	}
}