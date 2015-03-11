package com.aizhizu.service.house.wuba;

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
	private static String identidy = "web_wuba";
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
			url = "http://bj.58.com/chuzu/0/";
		else {
			url = "http://bj.58.com/chuzu/0/pn" + this.pageIndex + "/";
		}
		HttpMethod me = new HttpMethod("web_wuba");
		me.AddHeader(Method.Get, "Host", "bj.58.com");
		me.AddHeader(Method.Get, "User-Agent",
				"Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:33.0) Gecko/20100101 Firefox/33.0");
		me.AddHeader(Method.Get, "Accept",
				"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		me.AddHeader(Method.Get, "Accept-Language",
				"zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3");
		me.AddHeader(Method.Get, "Accept-Encoding", "gzip, deflate");
		me.AddHeader(Method.Get, "Connection", "keep-alive");
		String html = me.GetHtml(url, HttpResponseConfig.ResponseAsStream);
		return html;
	}

	protected Map<Analyst, Object> Analysis(String html) {
		if (StringUtils.isBlank(html)) {
			this.analystResult.put(Analyst.Info, "PageNo1 fial");
			return this.analystResult;
		}
		do {
			Document doc = Jsoup.parse(html);
			Elements houseNodes = doc.select("table.tbimg > tbody > tr");
			int houseNodesSize = houseNodes.size();
			if (houseNodesSize == 0) {
				html = GetHtml();
				doc = Jsoup.parse(html);
				houseNodes = doc.select("table.tbimg > tbody > tr");
			}
			Redis redis = Redis.getInstance();
			for (int nodeIndex = 0; nodeIndex < houseNodes.size(); nodeIndex++) {
				Element houseNode = houseNodes.get(nodeIndex);
				Elements zhidingNode = houseNode
						.select("td[class=t qj-rentd] > h1 > a[rel=nofollow]");
				String houseUrl = houseNode
						.select("td[class=t qj-rentd] > h1 > a.t").attr("href")
						.trim();
				String checkNodeStr = houseNode.select("td[class=t qj-rentd] > p.qj-rendp > span:eq(1)").text();
				if (checkNodeStr.contains("认证师")) {
					continue;
				}
				if (zhidingNode.size() != 0) {
					HttpMethod innerMe = new HttpMethod("web_wuba");
					String localUrl = innerMe.GetLocationUrl(houseUrl);
					if (StringUtils.isBlank(localUrl)) {
						localUrl = innerMe.GetLocationUrl(houseUrl);
					}
					if (StringUtils.isBlank(localUrl)) {
						continue;
					}
					localUrl = localUrl.replaceAll("shtml\\?ecspm=.*", "shtml");
					if (!StringUtils.isBlank(localUrl)) {
						houseUrl = localUrl;
					}

				} else if (!redis.hasNewsUrl(houseUrl)) {
					if (!taskList.contains(houseUrl)) {
						this.taskList.offer(houseUrl);
					}
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