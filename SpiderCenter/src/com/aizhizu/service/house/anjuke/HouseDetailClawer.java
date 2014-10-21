package com.aizhizu.service.house.anjuke;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class HouseDetailClawer extends BaseClawer {
	private static String identidy = "web_anjuke";
	private String url;
	private static Pattern pattern = Pattern
			.compile("http://.*?#l1=(.*?)&l2=(.*?)&l3=.*");

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
				.select("div.broker_infor > div[class~=broker_icon.*]");
		String phoneNum = "";
		if (phoneNodes.size() > 0) {
			phoneNum = phoneNodes.first().ownText().trim();
		}
		house.setPhone(phoneNum.replaceAll("\\s+", ""));
		String landlord = doc
				.select("div.broker_infor > div[class!=broker_name.*] > span#broker_true_name")
				.text().trim();
		int gender = 1;
		if (!landlord.contains("先生")) {
			gender = 0;
		}
		house.setGender(gender);
		house.setLandlord(landlord);
		String title = doc.select("div.wrapper > div[class=tit cf] > h3")
				.text().trim();
		house.setTitle(title);
		int rentalType = 2;
		String rentalTypeStr = doc
				.select("div[class=phraseobox cf] > div[class=litem fl] > dl> dt:contains(租赁方式) ~ dd")
				.text().trim();
		if (StringUtils.equals("整租", rentalTypeStr))
			rentalType = 1;
		else if (StringUtils.equals("合租", rentalTypeStr)) {
			if (title.contains("次卧"))
				rentalType = 4;
			else if (title.contains("主卧"))
				rentalType = 3;
			else if (title.contains("转租")) {
				rentalType = 2;
			}
		}
		house.setRentalType(rentalType);
		String price = doc
				.select("div[class=phraseobox cf] > div[class=litem fl] > dl> dt:contains(租价) ~ dd.og")
				.text().trim();
		price = price.replaceAll("\\D+", "");
		house.setPrice(price);
		String plot = doc.select("div.pinfo > div.hd > h5.gray").text().trim();
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
			Elements xyDataNodes = doc
					.select("div[class=phraseobox cf] > div[class=litem fl] > dl> dt:contains(地址) ~ dd > a");
			String xyData = "";
			if (xyDataNodes.size() > 0) {
				xyData = xyDataNodes.first().attr("href").trim();
			}
			if (!StringUtils.isBlank(xyData)) {
				Matcher matcher = pattern.matcher(xyData);
				if (matcher.find()) {
					x = matcher.group(1);
					y = matcher.group(2);
				}
			}
			Elements areaNodes = doc
					.select("div[class=phraseobox cf] > div[class=litem fl] > dl> dt:contains(所在版块) ~ dd > a");
			int areaNodesSize = areaNodes.size();
			if (areaNodesSize > 0) {
				area = areaNodes.last().text().trim();
				circle = areaNodes.first().text().trim();
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
		house.setX(x);
		house.setY(y);
		house.setArea(area);
		house.setCircle(circle);
		String format = doc
				.select("div[class=phraseobox cf] > div[class=litem fl] > dl> dt:contains(房型) ~ dd")
				.text().trim();
		format = format.replace("室", "-").replace("厅", "-").replace("卫", "");
		house.setFormat(format);
		String floor = doc
				.select("div[class=phraseobox cf] > div[class=ritem fr] > dl> dt:contains(楼层) ~ dd")
				.text().trim();
		String acreage = doc
				.select("div.pinfo > div.box > div[class=phraseobox cf] > div[class=ritem fr] > dl> dt:contains(面积) ~ dd")
				.text().trim();
		house.setFloor(floor);
		house.setAcreage(acreage);
		String face = doc
				.select("div[class=phraseobox cf] > div[class=ritem fr] > dl> dt:contains(朝向) ~ dd")
				.text().trim();
		house.setFace(face);
		String word = doc
				.select("div.pro_detail > div#propContent > div[class~=pro_con.*]")
				.text().trim();
		word = word.replaceAll("\n", "").replaceAll("\r", "")
				.replace("&nbsp;", "");
		house.setWord("\"" + word + "\"");
		Elements imageNodes = doc.select("div.picCon > ul > li > a > img");
		if (imageNodes.size() != 0) {
			Set<String> imageUrlList = new HashSet<String>();
			for (int imageIndex = 0; imageIndex < imageNodes.size(); imageIndex++) {
				Element imageNode = imageNodes.get(imageIndex);
				String imageNodeUrl = imageNode.attr("data-src").trim();
				if (StringUtils.isBlank(imageNodeUrl)) {
					imageNodeUrl = imageNode.attr("src").trim();
					if (!StringUtils.isBlank(imageNodeUrl)) {
						imageUrlList.add(imageNodeUrl);
					}
				}
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
		v.add("http://bj.zu.anjuke.com/gfangyuan/33749224");
		b.setBox(v);
		b.Implement();
	}
}