package com.aizhizu.service.house.ganji;

import com.aizhizu.bean.HouseChuzuEntity;
import com.aizhizu.http.HttpMethod;
import com.aizhizu.http.HttpResponseConfig;
import com.aizhizu.http.Method;
import com.aizhizu.service.Analyst;
import com.aizhizu.service.BaseClawer;
import com.aizhizu.service.dama.YunDaMa;
import com.aizhizu.service.house.DataPusher;
import com.aizhizu.service.house.UnmatchHouseDataStorer;
import com.aizhizu.util.CountDownLatchUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class HouseDetailClawer extends BaseClawer {
	private static String identidy = "web_ganji";
	private String url;
	private static Pattern pattern = Pattern.compile("window.PAGE_CONFIG\\s+=\\s+(.*?)\\s+\\|\\|\\s+\\{\\};");
	private static Pattern facePattern = Pattern.compile("概况： 朝(.*?)\\s+-.*");
	private static String imageUrl = "http://bj.ganji.com/ajax.php?dir=house&module=get_detail_viewer_login_status&fphone=@@&ca_id=##&puid=$$";
	
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
		me.AddHeader(Method.Get, "Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		me.AddHeader(Method.Get, "Accept-Encoding", "gzip, deflate, sdch");
		me.AddHeader(Method.Get, "Accept-Language", "zh-CN,zh;q=0.8");
		me.AddHeader(Method.Get, "Connection", "keep-alive");
		me.AddHeader(Method.Get, "Host", "bj.ganji.com");
		me.AddHeader(Method.Get, "User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/39.0.2171.65 Chrome/39.0.2171.65 Safari/537.36");
		String html = me.GetHtml(this.url, HttpResponseConfig.ResponseAsStream);
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
		Elements scriptNodes = doc.select("script[type=text/javascript]");
		int size = scriptNodes.size();
		String scriptStr = "";
		if (scriptNodes.size() > 0) {
			for (int scriptIndex = (size -1); scriptIndex >= 0; scriptIndex--) {
				Element script = scriptNodes.get(scriptIndex);
				scriptStr = script.html();
				if (scriptStr.startsWith("window.PAGE_CONFIG")) {
					break;
				}
			}
		} else {
			this.analystResult.put(Analyst.Info, "script null");
			this.analystResult.put(Analyst.FailCount, Integer.valueOf(1));
			return this.analystResult;
		}
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
		JSONObject scriptObject = JSON.parseObject(scriptStr);
		JSONObject logTracker = scriptObject.getJSONObject("logTracker");
		String gjch = logTracker.getString("gjch");
		String[] infoidArr = gjch.split("@");
		Map<String, String> infoMap = new HashMap<String, String>();
		for (String info : infoidArr) {
			if (!info.contains("=")) {
				continue;
			}
			String[] mapSeg = info.split("=");
			infoMap.put(mapSeg[0], mapSeg[1]);
		}
		
		Element fphoneNode = null;
		Element ca_idNode = null;
		Element puidNode = null;
		String fphoneStr = "";
		String ca_id = "";
		String puid = "";
		try {
			fphoneNode = doc.select("input#fphone").first();
			ca_idNode = doc.select("input#ca_id").first();
			puidNode = doc.select("input#puid").first();
			fphoneStr = fphoneNode.attr("value").trim();
			ca_id = ca_idNode.attr("value").trim();
			puid = puidNode.attr("value").trim();
		} catch (Exception e) {
			e.printStackTrace();
		}
		String phonePart = doc.select("div.basic-info-contact > div#contact-phone > span#s_part_phone > em.contact-mobile").text().trim();
		String phoneNum = "";
		boolean phoneStatus = false;
		CloseableHttpClient loginClient = null;
		loginClient = InitHttpClient.GetLoginedHttpClient();
		boolean clawPhoneNum = false;
		if (loginClient != null) {
			clawPhoneNum = true;
		}
		String phoneUrl = "";
		if (!phonePart.contains("*")) {
			phoneNum = phonePart;
		} else  if (clawPhoneNum) {
			phoneNum = phonePart;
			String sourceImageUrl = imageUrl.replace("@@", fphoneStr).replace("##",ca_id).replace("$$", puid);
			HttpMethod sourceImageMe = new HttpMethod(identidy, loginClient);
			if (StringUtils.isBlank(phoneUrl)) {
				String sourceImage = sourceImageMe.GetHtml(sourceImageUrl,HttpResponseConfig.ResponseAsStream);
				if (!StringUtils.isBlank(sourceImage)) {
					Object ret = null;
					JSONObject responseObj = null;
					try {
						sourceImage = sourceImage.replaceFirst("null\\{", "{");
						responseObj = JSONObject.parseObject(sourceImage);
						ret = responseObj.get("ret");
					} catch (JSONException je) {
					}
					if (ret != null) {
						String retStr = ret.toString();
						if (StringUtils.equals("-8", retStr)) {
							InitHttpClient.ResetHttpClientStat(UserStat.PasswdError);
						} else if (StringUtils.equals("-4", retStr)) {
							InitHttpClient.ResetHttpClientStat(UserStat.UseLess);
						} else if (!StringUtils.equals("1", retStr)) {
							InitHttpClient.ResetHttpClientStat(UserStat.Other);
						} else {
							try {
								Object isbind = responseObj.get("isbind");
								if (isbind != null) {
									String isbindStr = isbind.toString();
									if (StringUtils.equals("-1", isbindStr)) {
										InitHttpClient.ResetHttpClientStat(UserStat.UseLess);
									} else {
										String imageUrl = responseObj.getString("phoneshow").trim();
										imageUrl = imageUrl.replaceAll(".*\\?c=", "").replaceAll("\"\\s+/>", "").replaceAll("\n", "").replaceAll("\r", "");
										phoneUrl = "http://bj.ganji.com/tel_img/?c=" + imageUrl;
									}
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
			} 
			if (!StringUtils.isBlank(phoneUrl)) {
				try {
					File imageFile = this.GetImageFile(identidy, phoneUrl);
					if (imageFile != null) {
						YunDaMa y = new YunDaMa(imageFile);
						String phoneNumStr = y.GetPhoneNumber();
						if (!StringUtils.isBlank(phoneNumStr) && !StringUtils.equals("1", phoneNumStr)) {
							phoneNum = phoneNumStr;
							phoneStatus = true;
						} 
					}
					imageFile.delete();
				} catch (Exception e) {
				}
			}
		} else {
			phoneNum = phonePart;
		}
		house.setPhone(phoneNum.replaceAll("\\s+", ""));
		house.setPhoneImageUrl(phoneUrl);
		String landlord = doc.select("div.basic-info-contact > div[class~=contact-person.*] > span.contact-col > i.fc-4b").text().trim();
		int gender = 1;
		if (!landlord.contains("先生")) {
			gender = 0;
		}
		house.setGender(gender);
		house.setLandlord(landlord);
		String title = infoMap.get("title");
		if (!StringUtils.isBlank(title)) {
			try {
				title = URLDecoder.decode(title, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		house.setTitle(title);
		int rentalType = 2;
		String listname = infoMap.get("share_house_type");
		if (listname == null) {
			rentalType = 1;
		} else {
			if (StringUtils.equals("1", listname)) {
				rentalType = 3;
			} else if (StringUtils.equals(listname, "2")) {
				rentalType = 4;
			}
		}
		house.setRentalType(rentalType);
		String price = infoMap.get("price").trim();
		house.setPrice(price);

		String shiType = infoMap.get("huxing_shi")==null?"":infoMap.get("huxing_shi");
		String tingType = infoMap.get("huxing_ting")==null?"":infoMap.get("huxing_ting");
		String weiType = infoMap.get("huxing_wei")==null?"1":infoMap.get("huxing_wei");
		
		StringBuilder format = new StringBuilder();
		if (!StringUtils.isBlank(shiType)) {
			format.append(shiType + "-");
		}
		if (!StringUtils.isBlank(tingType)) {
			format.append(tingType + "-");
		}
		format.append(weiType);
		house.setFormat(format.toString());
		
		String plot = infoMap.get("xq_name");
		try {
			plot = URLDecoder.decode(plot, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		plot = plot.replaceAll("小区.*", "小区");
		String plotData = redis.getPlotData(plot);
		
		String x = "";
		String y = "";
		String area = "";
		String circle = "";
		boolean push = false;
		if (StringUtils.isBlank(plotData)) {
			Element xyNode = doc.select("div#wrapper > div[class=content clearfix] > div.leftBox > div#js-map > div.col-sub > div#map_load").first();
			if (xyNode != null) {
				String xyStr = xyNode.attr("data-ref").trim();
				try {
					JSONObject xy = JSONObject.parseObject(xyStr);
					String lnglat = xy.getString("lnglat");
					String[] xyArr = lnglat.split(",");
					x = xyArr[1];
					y = xyArr[0].replace("b", "");
				} catch (Exception e) {
				}
			}
			Elements areaInfo = doc.select("div.basic-info > ul.basic-info-ul > li[class~=with-area.*] > a");
			if (areaInfo.size() == 3) {
				area = areaInfo.get(1).text().trim();
				circle = areaInfo.get(2).text().trim();
			} else if (areaInfo.size() == 2) {
				circle = areaInfo.get(1).text().trim();
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
			push = true;
			String[] plotDataArr = plotData.split("\\|");
			area = plotDataArr[0];
			circle = plotDataArr[1];
			String[] yxArr = plotDataArr[2].split(",|，");
			y = yxArr[0];
			x = yxArr[1];
		}
		house.setDistrict(plot);
		house.setArea(area);
		house.setCircle(circle);
		house.setX(x);
		house.setY(y);
		if (phoneStatus && push) {
			house.setPush(true);
		} else {
			house.setPush(false);
		}
		String floor = doc.select("div.basic-info > ul.basic-info-ul > li:has(span:contains(楼层))").text().trim();
		floor = floor.replace("楼层", "").replace(":", "").replace("：", "").trim();
		house.setX(x);
		house.setY(y);
		house.setFloor(floor);
		
		String acreage = infoMap.get("area")==null?"":infoMap.get("area");
		acreage = acreage.replaceAll("\\.\\d+", "");
		house.setAcreage(acreage);
		
		String faceHtml = doc.select("div.basic-info > ul.basic-info-ul > li:has(span:contains(概况))").text().trim();
		String face = "";
		Matcher faceMatcher = facePattern.matcher(faceHtml);
		if (faceMatcher.find()) {
			face = faceMatcher.group(1);
			face = face.replace("概况", "").replace(":", "").replace("：", "").trim();
		}
		house.setFace(face);
		String word = doc.select("div[class=summary-cont]").text().trim();
		word = word.replaceAll("\n", "").replaceAll("\r", "").replace("&nbsp;", "");
		word = word.replaceAll("联系我时\\s*(，|,)\\s*请说是在赶集网上看到的\\s*(，|,)\\s*谢谢\\s*(!|！)\\s*", "");
		house.setWord("\"" + word + "\"");

		Elements imageNodes = doc.select("div#js-picture > div[class=cont-box pics] > a > img");
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
		house.setLineName("ganji");
		this.analystResult.put(Analyst.Entity, house);
		this.analystResult.put(Analyst.Info, "succ");
		this.analystResult.put(Analyst.SuccCount, Integer.valueOf(1));
		return this.analystResult;
	}

	public static void main(String[] args) {
//		String test = "{\"ret\":1,\"isbind\":1,\"couldsee\":1,\"phoneshow\":\"<img src=\\\"\\/tel_img\\/?c=kt1K6g3QmMbqFobNIsVMbil4828DQ__PtQyX\\\" \\/>\",\"user_id\":390884014,\"phone\":18500230256}";
//		System.out.println(test);
//		String imageUrl = test.replaceAll(".*\\?c=", "").replaceAll("\\\\\"\\s+\\\\/>.*", "");
//		System.out.println(imageUrl);
		BaseClawer b = new HouseDetailClawer(new CountDownLatchUtils(1));
		Vector<String> v = new Vector<String>();
		v.add("http://bj.ganji.com/fang1/1408087561x.htm");
		b.setBox(v);
		b.Implement();
		Object o = b.getEntity();
		DataPusher d = new DataPusher((HouseChuzuEntity)(o), "http://112.126.65.145:8099/housing!crawlerH.action");
		new Thread(d).start();
		
	}
}