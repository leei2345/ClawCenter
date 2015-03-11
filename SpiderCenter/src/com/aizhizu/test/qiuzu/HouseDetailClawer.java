package com.aizhizu.test.qiuzu;
/*package com.aizhizu.test;

import com.aizhizu.bean.HouseChuzuEntity;
import com.aizhizu.bean.HouseQiuzuEntity;
import com.aizhizu.http.HttpMethod;
import com.aizhizu.http.HttpResponseConfig;
import com.aizhizu.http.Method;
import com.aizhizu.service.Analyst;
import com.aizhizu.service.BaseClawer;
import com.aizhizu.service.house.ImageOcr;
import com.aizhizu.service.house.PlotDataMatcher;
import com.aizhizu.service.house.UnmatchHouseDataStorer;
import com.aizhizu.util.CountDownLatchUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class HouseDetailClawer extends BaseClawer {
	private static String proxy_identidy = "web_wuba";
	private static String identidy = "qiuzu_web_wuba";
	private String url;
	private static Pattern pattern = Pattern.compile("var\\s+____json4fe\\s+=\\s+(.*?);\r*?\n*?\\s*?____json4fe\\.modules");

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
		HttpMethod me = new HttpMethod(proxy_identidy);
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
		if (doc.select("div.w_990 > search_tips_info:contains(请输入验证码)").size() > 0) {
			html = GetHtml();
			doc = Jsoup.parse(html);
		}
		HouseQiuzuEntity house = new HouseQiuzuEntity();
		house.setUrl(this.url);
		Elements sriptNodes = doc.select("script[type=text/javascript]");
		Element scriptNode = null;
		if (sriptNodes.size() > 0) {
			scriptNode = sriptNodes.first();
		} else {
			this.analystResult.put(Analyst.Info, "script null");
			this.analystResult.put(Analyst.FailCount, Integer.valueOf(1));
			return this.analystResult;
		}
		String scriptStr = scriptNode.html();
		Matcher matcher = pattern.matcher(scriptStr);
		if (matcher.find()) {
			scriptStr = matcher.group(1);
		} else {
			this.analystResult.put(Analyst.Info, "script error");
			this.analystResult.put(Analyst.FailCount, Integer.valueOf(1));
			return this.analystResult;
		}
		if (scriptStr.lastIndexOf("}") != scriptStr.length() - 1) {
			this.analystResult.put(Analyst.Info, "script error");
			this.analystResult.put(Analyst.FailCount, Integer.valueOf(1));
			return this.analystResult;
		}
		scriptStr = scriptStr.replaceAll("start:.*?,", "");
		JSONObject scriptObject = JSON.parseObject(scriptStr);
		String infoid = scriptObject.getString("infoid");
		JSONArray locallistArr = scriptObject.getJSONArray("locallist");
		int locallistSize = locallistArr.size();
		JSONObject areaObject = null;
		JSONObject circleObject = null;
		if (locallistSize > 0) {
			circleObject = locallistArr.getJSONObject(locallistSize - 1);
			if (locallistArr.size() >= 3) {
				areaObject = locallistArr.getJSONObject(1);
			}
		}
		String phoneUrl = doc.select("ul[class=vuser nomargin] > li.call_2 > span.phone > img").attr("src").trim();
		String phoneNum = "";
			int index = 0;
			int count = 5;
			boolean retry = true;
			do
				try {
					String phoneLast4Num = ImageOcr.getImageLast4Num(identidy,phoneUrl);
					if (phoneLast4Num.matches("\\d+")) {
						retry = false;
						phoneNum = phoneLast4Num;
					} else {
						index++;
					}
				} catch (Exception e) {
					index++;
				}
			while ((index < count) && (retry));
			if (StringUtils.isBlank(last4Num)) {
				this.analystResult.put(Analyst.Info, "phone num null");
				this.analystResult.put(Analyst.FailCount, Integer.valueOf(1));
				return this.analystResult;
			}
			phoneNum = phonePart + last4Num;
			
		house.setPhone(phoneNum.replaceAll("\\s+", ""));
		house.setPhoneImageUrl(sourceImageUrl);
		String landlord = scriptObject.getString("linkman");
		int gender = 1;
		if (!landlord.contains("先生")) {
			gender = 0;
		}
		house.setGender(gender);
		house.setLandlord(landlord);
		String title = doc.select("div.bigtitle > h1").text().trim();
		house.setTitle(title);
		int rentalType = 2;
		String listname = scriptObject.getJSONObject("catentry").getString(
				"listname");
		if (StringUtils.equals("zufang", listname)) {
			rentalType = 1;
		}
		if (title.contains("主卧")) {
			rentalType = 3;
		}
		if (title.contains("次卧")) {
			rentalType = 4;
		}
		house.setRentalType(rentalType);
		String paramdata = scriptObject.getJSONObject("supplycount").getString(
				"paramdata");
		JSONObject priceObject = JSONObject.parseObject(paramdata);
		String price = priceObject.getString("MinPrice");
		house.setPrice(price);

		String shiType = priceObject.getString("huxingshi");
		if (StringUtils.isBlank(shiType)) {
			shiType = "0";
		}
		String tingType = priceObject.getString("huxingting");
		if (StringUtils.isBlank(tingType)) {
			tingType = "0";
		}
		String weiType = priceObject.getString("huxingwei");
		if (StringUtils.isBlank(weiType)) {
			weiType = "0";
		}
		String format = shiType + "-" + tingType + "-" + weiType;
		house.setFormat(format);
		JSONArray trackParamsArr = scriptObject.getJSONArray("_trackParams");
		Map<String, String> dataMap = new HashMap<String, String>();
		for (int paramsIndex = 0; paramsIndex < trackParamsArr.size(); paramsIndex++) {
			JSONObject paramsObject = trackParamsArr.getJSONObject(paramsIndex);
			String I = paramsObject.getString("I").trim();
			String V = paramsObject.getString("V").trim();
			dataMap.put(I, V);
		}
		String plot = "";
		String plot1 = scriptObject.getJSONObject("xiaoqu").getString("name").trim();
		plot1 = plot1.replaceAll("小区.*", "小区");
		plot = plot1;
		String plot2 = (String) dataMap.get("1588");
		String plotData = redis.getPlotData(plot1);
		if ((StringUtils.isBlank(plotData)) && (!StringUtils.isBlank(plot2))&& (!StringUtils.equals(plot2, plot1))) {
			plotData = redis.getPlotData(plot2);
			if (!StringUtils.isBlank(plotData)) {
				plot = plot2;
			}
		}
		if (StringUtils.isBlank(plotData)) {
			String matchePlot = PlotDataMatcher.matchPlot(plot);
			if (!StringUtils.isBlank(matchePlot)) {
				plotData = redis.getPlotData(matchePlot);
			}
		}
		String x = "";
		String y = "";
		String area = "";
		String circle = "";
		if (StringUtils.isBlank(plotData)) {
			JSONObject xiaoquObject = scriptObject.getJSONObject("xiaoqu");
			x = xiaoquObject.getString("lat").trim();
			y = xiaoquObject.getString("lon").trim();
			if (areaObject != null) {
				area = areaObject.getString("name").trim();
			}
			if (circleObject != null) {
				circle = circleObject.getString("name").trim();
			}
			if (StringUtils.isBlank(x)) {
				x = dataMap.get("6691") == null ? "" : ((String) dataMap
						.get("6691")).trim();
			}
			if (StringUtils.isBlank(x)) {
				x = dataMap.get("7110") == null ? "" : ((String) dataMap
						.get("7110")).trim();
			}
			if (StringUtils.isBlank(y)) {
				y = dataMap.get("6692") == null ? "" : ((String) dataMap
						.get("6692")).trim();
			}
			if (StringUtils.isBlank(y)) {
				y = dataMap.get("7109") == null ? "" : ((String) dataMap
						.get("7109")).trim();
			}
			Map<String, Object> storerMap = new HashMap<String, Object>();
			storerMap.put("plot", plot);
			storerMap.put("area", area);
			storerMap.put("district", circle);
			storerMap.put("yx", y + "," + x);
			storerMap.put("source_url", this.url);
			UnmatchHouseDataStorer storer = new UnmatchHouseDataStorer(
					storerMap);
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
		house.setArea(area);
		house.setCircle(circle);
		house.setX(x);
		house.setY(y);

		String floor = "";
		String floorNum = "";
		String floorData = dataMap.get("5332") == null ? "" : ((String) dataMap
				.get("5332")).trim();
		String[] floorDataArr = floorData.split("\\|");
		if (floorDataArr.length != 1) {
			String floorStr = floorDataArr[1];
			String[] floorDataInnerArr = floorStr.split(",|，");
			floorStr = floorDataInnerArr[0];
			floorNum = floorStr.replaceAll("\\D+", "");
		} else {
			floorNum = "1";
		}
		String floorCount = dataMap.get("1596") == null ? ""
				: ((String) dataMap.get("1596")).trim();
		String floorCountBackUp = dataMap.get("1608") == null ? ""
				: ((String) dataMap.get("1608")).trim();
		String acreage = dataMap.get("1049") == null ? "" : ((String) dataMap
				.get("1049")).trim();
		if (StringUtils.isBlank(acreage)) {
			acreage = dataMap.get("1025") == null ? "" : ((String) dataMap
					.get("1025")).trim();
		}

		if (!StringUtils.isBlank(floorNum)) {
			if (!StringUtils.isBlank(floorCount))
				floor = floorNum + "/" + floorCount;
			else if (!StringUtils.isBlank(floorCountBackUp)) {
				floor = floorNum + "/" + floorCountBackUp;
			}
		}
		house.setX(x);
		house.setY(y);
		house.setFloor(floor);
		house.setAcreage(acreage);

		Elements faceNodes = doc
				.select("div.ctt-yzfpar > div.yzf-zf-tit > div[class=col detailPrimary] > div[class=col_sub maintop clearfixfix] > div[class~=col_sub.*] > ul.suUl > li > div:contains(朝向)");
		String faceHtml = faceNodes.text().replace("&nbsp;", "");
		String face = "";
		Matcher faceMatcher = facePattern.matcher(faceHtml);
		if (faceMatcher.find()) {
			face = faceMatcher.group(1);
		}
		house.setFace(face);
		String word = doc
				.select("div[class=descriptionBox maincon] > div.description_con > p")
				.text().trim();
		word = word.replaceAll("\n", "").replaceAll("\r", "")
				.replace("&nbsp;", "");
		house.setWord("\"" + word + "\"");

		Elements imageNodes = doc.select("div.descriptionImg > img");
		if (imageNodes.size() != 0) {
			Set<String> imageUrlList = new HashSet<String>();
			for (int imageIndex = 0; imageIndex < imageNodes.size(); imageIndex++) {
				Element imageNode = imageNodes.get(imageIndex);
				String imageNodeUrl = imageNode.attr("src").trim();
				if (!StringUtils.isBlank(imageNodeUrl)) {
					imageUrlList.add(imageNodeUrl);
				}
			}
			house.setImageUrlList(imageUrlList);
		}
		this.analystResult.put(Analyst.Entity, house);
		this.analystResult.put(Analyst.Info, "succ");
		this.analystResult.put(Analyst.SuccCount, Integer.valueOf(1));
		return this.analystResult;
	}

	public static void main(String[] args) {
		BaseClawer b = new HouseDetailClawer(new CountDownLatchUtils(1));
		Vector<String> v = new Vector<String>();
		v.add("http://bj.58.com/hezu/18797768514570x.shtml");
		b.setBox(v);
		b.Implement();
	}
}*/