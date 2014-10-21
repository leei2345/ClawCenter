package com.aizhizu.service.house.soufang;

import com.aizhizu.bean.HouseChuzuEntity;
import com.aizhizu.http.HttpMethod;
import com.aizhizu.http.HttpResponseConfig;
import com.aizhizu.service.Analyst;
import com.aizhizu.service.BaseClawer;
import com.aizhizu.service.house.UnmatchHouseDataStorer;
import com.aizhizu.util.CountDownLatchUtils;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class HouseDetailClawer extends BaseClawer {
	private static String identidy = "web_soufang";
	private String url;

	public HouseDetailClawer(CountDownLatchUtils cdl) {
		super(identidy);
		this.cdl = cdl;
	}

	public void run() {
	}

	protected void init() {
		this.url = ((String) this.box.get(0));
	}

	protected String GetHtml() {
		HttpMethod me = new HttpMethod(identidy);
		String html = me.GetHtml(this.url, HttpResponseConfig.ResponseAsString);
		return html;
	}

	protected Map<Analyst, Object> Analysis(String html) {
		if (StringUtils.isBlank(html)) {
			this.analystResult.put(Analyst.Info, "html null");
			this.analystResult.put(Analyst.FailCount, Integer.valueOf(1));
			return this.analystResult;
		}
		Document doc = Jsoup.parse(html);
		HouseChuzuEntity house = new HouseChuzuEntity();
		house.setUrl(this.url);
		Elements phoneNodes = doc
				.select("p[class=phoneBg  mt10] > span#agtphone > span.telno0");
		String phoneNum = "";
		if (phoneNodes.size() > 0) {
			phoneNum = phoneNodes.first().ownText().trim();
		}
		house.setPhone(phoneNum.replaceAll("\\s+", ""));
		String landlord = doc.select("p[class=phoneBg  mt10] > span#Span2")
				.text().trim();
		int gender = 1;
		if (!landlord.contains("先生")) {
			gender = 0;
		}
		house.setGender(gender);
		house.setLandlord(landlord);
		Element titleNode = null;
		String title = "";
		try {
			titleNode = doc.select("dl.title > dt[class=name floatl] > p")
					.first();
		} catch (Exception localException) {
		}
		if (titleNode != null) {
			title = titleNode.ownText().trim();
		}
		house.setTitle(title);
		int rentalType = 2;
		String rentalTypeStr = doc
				.select("ul[class=Huxing floatl] > li > p:contains(出租间) ~ p.info")
				.text().trim();
		if (StringUtils.equals("整租", rentalTypeStr))
			rentalType = 1;
		else if (StringUtils.equals("主卧", rentalTypeStr))
			rentalType = 3;
		else if (StringUtils.equals("次卧", rentalTypeStr)) {
			rentalType = 4;
		}
		house.setRentalType(rentalType);
		String price = doc
				.select("div[class=info floatr] > ul > li > span[class=num green]")
				.text().trim();
		price = price.replaceAll("\\D+", "");
		house.setPrice(price);
		String plot = doc
				.select("ul[class=Huxing floatl] > li > p:contains(小 区) ~ p.info")
				.text().trim();
		String plotData = "";
		if (!StringUtils.isBlank(plot)) {
			plotData = redis.getPlotData(plot);
		} else {
			this.analystResult.put(Analyst.Info, "plot null");
			this.analystResult.put(Analyst.FailCount, Integer.valueOf(1));
			return this.analystResult;
		}
		String x = "";
		String y = "";
		String area = "";
		String circle = "";
		if (StringUtils.isBlank(plotData)) {
			Elements areaNodes = doc
					.select("div[class=info floatr] > ul > li > span:contains(小区)");
			int areaNodesSize = areaNodes.size();
			if (areaNodesSize > 0) {
				circle = areaNodes.select("a[id=gerenzfxq_B04_04]").text()
						.trim();
				area = areaNodes.select("a[id=gerenzfxq_B04_03]").text().trim();
			}
			Map<String, Object> storerMap = new HashMap<String, Object>();
			storerMap.put("plot", plot);
			storerMap.put("area", area);
			storerMap.put("district", circle);
			storerMap.put("yx", y + "," + x);
			storerMap.put("source_url", this.url);
			UnmatchHouseDataStorer storer = new UnmatchHouseDataStorer(storerMap);
			new Thread(storer).start();
		} else {
			String[] plotDataArr = plotData.split("\\|");
			area = plotDataArr[0];
			circle = plotDataArr[1];
			String[] yxArr = plotDataArr[2].split(",");
			y = yxArr[0];
			x = yxArr[1];
		}
		house.setDistrict(plot);
		house.setX(x);
		house.setY(y);
		house.setArea(area);
		house.setCircle(circle);
		String format = doc
				.select("ul[class=Huxing floatl] > li > p:contains(户 型) ~ p.info")
				.text().trim();
		format = format.replace("室", "-").replace("厅", "-").replace("卫", "");
		house.setFormat(format);
		String floor = doc
				.select("ul[class=Huxing floatl] > li > p:contains(楼 层) ~ p.info")
				.text().trim();
		floor = floor.replace("层", "");
		String acreage = doc
				.select("ul[class=Huxing floatl] > li > p:contains(面 积) ~ p.info")
				.text().trim();
		house.setFloor(floor);
		house.setAcreage(acreage.replaceAll("\\D+", ""));
		String face = doc
				.select("ul[class=Huxing floatl] > li > p:contains(朝 向) ~ p.info")
				.text().trim();
		house.setFace(face);
		String word = doc.select("div[class=Introduce floatr] > p").text()
				.trim();
		word = word.replaceAll("\n", "").replaceAll("\r", "")
				.replace("&nbsp;", "");
		house.setWord("\"" + word + "\"");
		Elements imageNodes = doc
				.select("div[class=zoombox floatl] > div.sliderbox > div.slider > ul > li > img");
		if (imageNodes.size() != 0) {
			Set<String> imageUrlList = new HashSet<String>();
			for (int imageIndex = 0; imageIndex < imageNodes.size(); imageIndex++) {
				Element imageNode = imageNodes.get(imageIndex);
				String imageNodeUrl = imageNode.attr("src").trim();
				if (StringUtils.isBlank(imageNodeUrl)) {
					continue;
				}
				imageUrlList.add(imageNodeUrl);
			}
			house.setImageUrlList(imageUrlList);
		}
		this.analystResult.put(Analyst.Info, "succ");
		this.analystResult.put(Analyst.SuccCount, Integer.valueOf(1));
		this.analystResult.put(Analyst.Entity, house);
		return this.analystResult;
	}

	public static void main(String[] args) {
		BaseClawer b = new HouseDetailClawer(new CountDownLatchUtils(1));
		Vector<String> v = new Vector<String>();
		v.add("http://zu.fang.com/chuzu/1_58736337_-1.htm");
		b.setBox(v);
		b.Implement();
	}
}