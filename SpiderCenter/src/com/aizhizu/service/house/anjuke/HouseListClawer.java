package com.aizhizu.service.house.anjuke;

import com.aizhizu.dao.Redis;
import com.aizhizu.http.HttpMethod;
import com.aizhizu.http.HttpResponseConfig;
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
	private static String identidy = "web_anjuke";
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
		do {
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
			clawerLogger.info("[" + identidy + "][list][page " + this.pageIndex + "][tasklist " + this.taskList.size() + "]");
			this.pageIndex += 1;
			if (this.pageIndex <= pageCount) {
				html = GetHtml();
			}
		} while (this.pageIndex <= pageCount);
			this.analystResult.put(Analyst.Info, "succ");
		return this.analystResult;
	}

	public static void main(String[] args) {
		BaseHouseListClawer b = new HouseListClawer("/home/leei/2014100801/", new CountDownLatchUtils(1));
		new Thread(b).start();
	}
}