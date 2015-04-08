package com.aizhizu.service.house.wuba;

import java.io.File;
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

import com.aizhizu.bean.HouseChuzuEntity;
import com.aizhizu.http.HttpMethod;
import com.aizhizu.http.HttpResponseConfig;
import com.aizhizu.http.Method;
import com.aizhizu.service.Analyst;
import com.aizhizu.service.BaseClawer;
import com.aizhizu.service.dama.YunDaMa;
import com.aizhizu.service.house.BaseHouseDetailHandler;
import com.aizhizu.service.house.DataPusher;
import com.aizhizu.service.house.PlotDataMatcher;
import com.aizhizu.service.house.UnmatchHouseDataStorer;
import com.aizhizu.util.CountDownLatchUtils;
import com.aizhizu.util.LoggerUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;


public class HouseDetailClawer extends BaseHouseDetailHandler {
	private static String identidy = "web_wuba";
	private String url;
	private static Pattern pattern = Pattern.compile("var\\s+____json4fe\\s+=\\s+(.*?);\r*?\n*?\\s*?____json4fe\\.modules");
	private static Pattern facePattern = Pattern.compile("朝向(.*?)(\\s+|，|,).*");
	private static String imageUrl = "http://yuzufang.house.58.com/common/getinfophonebyauth?infoid=@@&cityId=##";

	public HouseDetailClawer(CountDownLatchUtils cdl) {
		super(identidy);
		this.cdl = cdl;
	}

	public void run() {
		try {
			Implement();
		} catch (Exception e) {
			LoggerUtil.ClawerLog("[" + identidy + "][got house detail fail][" + e.getMessage() + "]");
			return;
		}
		HouseChuzuEntity entity = (HouseChuzuEntity) this.getEntity();
		if (entity != null) {
			this.DealWithChuzuData(entity);
		}
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
		if (doc.select("div.w_990 > search_tips_info:contains(请输入验证码)").size() > 0) {
			html = GetHtml();
			doc = Jsoup.parse(html);
		}
		HouseChuzuEntity house = new HouseChuzuEntity();
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
		String dispid = "";
		JSONObject areaObject = null;
		JSONObject circleObject = null;
		if (locallistSize > 0) {
			dispid = locallistArr.getJSONObject(0).getString("dispid");
			circleObject = locallistArr.getJSONObject(locallistSize - 1);
			if (locallistArr.size() >= 3) {
				areaObject = locallistArr.getJSONObject(1);
			}
		}
		String phonePart = doc.select("div.su_phone > div > span.phone-num").text().trim();
		if (StringUtils.isBlank(phonePart)) {
			phonePart = doc.select("div[class=yzf-zf-tit] > div > div > div[class~=col_sub\\s+sumary.*] > ul > li > div[class=tel_phone_rz] > span").text().trim();
		}
		String phoneNum = "";
		String phoneImageUrl = "";
		boolean fullPhoneNum = false;
		if (!phonePart.contains("*")) {
			phoneNum = phonePart;
		} else {
			phoneNum = phonePart;
			String sourceImageUrl = imageUrl.replace("@@", infoid).replace("##",dispid);
			HttpMethod sourceImageMe = new HttpMethod(identidy);
			sourceImageMe.AddHeader(Method.Get, "Host", "yuzufang.house.58.com");
			sourceImageMe.AddHeader(Method.Get, "User-Agent","Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:32.0) Gecko/20100101 Firefox/32.0");
			sourceImageMe.AddHeader(Method.Get, "Accept", "*/*");
			sourceImageMe.AddHeader(Method.Get, "Accept-Language","zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3");
			sourceImageMe.AddHeader(Method.Get, "Accept-Encoding","gzip, deflate");
			sourceImageMe.AddHeader(Method.Get, "Referer", this.url);
			String sourceImage = sourceImageMe.GetHtml(sourceImageUrl,HttpResponseConfig.ResponseAsString);
			phoneImageUrl = sourceImage.replaceAll(".*<img\\s+src='", "").replaceAll("'\\s+/>\",.*", "");
			if (phoneImageUrl.contains("<HTML>")) {
				phoneImageUrl = "";
			}
			if (!StringUtils.isBlank(phoneImageUrl)) {
				try {
					File imageFile = this.GetImageFile(identidy, phoneImageUrl);
					if (imageFile != null) {
						YunDaMa y = new YunDaMa(imageFile);
						String phoneNumStr = y.GetPhoneNumber();
						if (!StringUtils.isBlank(phoneNumStr) && !StringUtils.equals("1", phoneNumStr)) {
							phoneNum = phoneNumStr;
							fullPhoneNum = true;
						} 
						imageFile.delete();
						
					}
				} catch (Exception e) {
				}
			}
		}
		house.setPhone(phoneNum.replaceAll("\\s+", ""));
		house.setPhoneImageUrl(phoneImageUrl);
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
			String jushi = priceObject.getString("jushishuru");
			if (StringUtils.isBlank(jushi)) {
				shiType = "1";
			} else {
				shiType = jushi;
			}
		}
		String tingType = priceObject.getString("huxingting");
		if (StringUtils.isBlank(tingType)) {
			tingType = "1";
		}
		String weiType = priceObject.getString("huxingwei");
		if (StringUtils.isBlank(weiType)) {
			weiType = "1";
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
		boolean push = false;
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
			push = true;
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
		if (push && fullPhoneNum) {
			house.setPush(true);
		} else {
			house.setPush(false);
		}
		String floor = "";
		String floorNum = "";
		String floorData = dataMap.get("5332") == null ? "" : ((String) dataMap.get("5332")).trim();
		String[] floorDataArr = floorData.split("\\|");
		if (floorDataArr.length != 1) {
			String floorStr = floorDataArr[1];
			String[] floorDataInnerArr = floorStr.split(",|，");
			floorStr = floorDataInnerArr[0];
			floorNum = floorStr.replaceAll("\\D+", "");
		} else {
			String floorNumBackUp = dataMap.get("1062") == null ? "" : ((String) dataMap.get("1062")).trim();
			if (StringUtils.isBlank(floorNumBackUp)) {
				floorNum = "1";
			} else {
				floorNum = floorNumBackUp;
			}
		}
		String floorCount = dataMap.get("1596") == null ? "": ((String) dataMap.get("1596")).trim();
		String floorCountBackUp = dataMap.get("1608") == null ? "": ((String) dataMap.get("1608")).trim();
		String acreage = dataMap.get("1049") == null ? "" : ((String) dataMap.get("1049")).trim();
		if (StringUtils.isBlank(acreage)) {
			acreage = dataMap.get("1025") == null ? "" : ((String) dataMap.get("1025")).trim();
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

		Elements faceNodes = doc.select("div.ctt-yzfpar > div.yzf-zf-tit > div[class=col detailPrimary] > div[class=col_sub maintop clearfixfix] > div[class~=col_sub.*] > ul.suUl > li > div:contains(朝向)");
		String faceHtml = faceNodes.text().replace("&nbsp;", "");
		String face = "";
		Matcher faceMatcher = facePattern.matcher(faceHtml);
		if (faceMatcher.find()) {
			face = faceMatcher.group(1);
		}
		house.setFace(face);
		String word = doc
				.select("div[class=descriptionBox maincon] > div.description_con > p:not(.mb20)")
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
		house.setLineName("58");
		this.analystResult.put(Analyst.Entity, house);
		this.analystResult.put(Analyst.Info, "succ");
		this.analystResult.put(Analyst.SuccCount, Integer.valueOf(1));
		return this.analystResult;
	}

	public static void main(String[] args) {
		BaseClawer b = new HouseDetailClawer(new CountDownLatchUtils(1));
		Vector<String> v = new Vector<String>();
		v.add("http://bj.58.com/zufang/21189724417418x.shtml");
		b.setBox(v);
		b.Implement();
		Object o = b.getEntity();
		DataPusher d = new DataPusher((HouseChuzuEntity)(o), "http://112.126.65.145:8099/housing!crawlerH.action");
		new Thread(d).start();
		
	}
}