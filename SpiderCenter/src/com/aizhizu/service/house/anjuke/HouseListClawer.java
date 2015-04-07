package com.aizhizu.service.house.anjuke;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.aizhizu.dao.Redis;
import com.aizhizu.http.HttpMethod;
import com.aizhizu.http.HttpResponseConfig;
import com.aizhizu.service.Analyst;
import com.aizhizu.service.house.BaseHouseClawer;
import com.aizhizu.util.CountDownLatchUtils;
import com.aizhizu.util.LoggerUtil;

public class HouseListClawer extends BaseHouseClawer {
	private static String identidy = "web_anjuke";
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
			url = "http://bj.zu.anjuke.com/fangyuan/l2/#filtersort";
		else {
			url = "http://bj.zu.anjuke.com/fangyuan/l2-p" + this.pageIndex
					+ "/#filtersort";
		}
		HttpMethod me = new HttpMethod(identidy);
		String html = me.GetHtml(url, HttpResponseConfig.ResponseAsString);
		return html;
	}

	protected Map<Analyst, Object> Analysis(String html) {
		if (StringUtils.isBlank(html)) {
			this.analystResult.put(Analyst.Info, "PageNo1 fial");
			return this.analystResult;
		}
		Document doc = Jsoup.parse(html);
		Elements houseNodes = doc
				.select("div.main_content > div.content_l > div.plate > dl");
		int houseNodesSize = houseNodes.size();
		if (houseNodesSize == 0) {
			html = GetHtml();
			doc = Jsoup.parse(html);
			houseNodes = doc.select("div.main_content > div.content_l > div.plate > dl");
		}
		Redis redis = Redis.getInstance();
		for (int nodeIndex = 0; nodeIndex < houseNodes.size(); nodeIndex++) {
			Element houseNode = houseNodes.get(nodeIndex);
			String houseUrl = houseNode.attr("link").trim();
			houseUrl = houseUrl.replaceAll("\\?from=.*", "");
			if ((!redis.hasNewsUrl(houseUrl))
					&& (!StringUtils.isBlank(houseUrl))) {
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