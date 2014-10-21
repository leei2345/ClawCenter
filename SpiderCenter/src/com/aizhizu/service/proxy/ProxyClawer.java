package com.aizhizu.service.proxy;

import com.aizhizu.dao.DBDataWriter;
import com.aizhizu.http.HttpMethod;
import com.aizhizu.http.HttpRequestConfig;
import com.aizhizu.http.HttpResponseConfig;
import com.aizhizu.http.Method;
import com.aizhizu.service.Analyst;
import com.aizhizu.service.BaseClawer;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.time.FastDateFormat;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class ProxyClawer extends BaseClawer {
	private static String identidy = "proxy_claw";
	private static String baseUrl = "http://miwangip.duapp.com/qq841175411.php";
	private static Pattern pattern = Pattern.compile("((\\d{1,3}\\.){3}\\d{1,3}:\\d+)");
	protected static FastDateFormat fdf = FastDateFormat.getInstance("yyyyMMdd|HH:mm");

	public ProxyClawer() {
		super(identidy);
	}

	public void run() {
	}

	protected void init() {
	}

	protected String GetHtml() {
		HttpMethod http = new HttpMethod();
		http.AddHeader(Method.Post, "Accept",
				"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		http.AddHeader(Method.Post, "Accept-Encoding", "gzip,deflate,sdch");
		http.AddHeader(Method.Post, "Accept-Language", "zh-CN,zh;q=0.8");
		http.AddHeader(Method.Post, "Cache-Control", "max-age=0");
		http.AddHeader(Method.Post, "Connection", "keep-alive");
		http.AddHeader(Method.Post, "Content-Type",
				"application/x-www-form-urlencoded");
		http.AddHeader(Method.Post, "Host", "miwangip.duapp.com");
		http.AddHeader(Method.Post, "Origin", "http：//miwangip.duapp.com");
		http.AddHeader(Method.Post, "Referer", "http：//miwangip.duapp.com/");
		http.AddHeader(
				Method.Post,
				"User-Agent",
				"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/36.0.1985.125 Safari/537.36");
		String body = "sxb=&tqsl=300&ports%5B%5D2=&ktip=&sxa=&submit=%CC%E1++%C8%A1";
		String html = http.GetHtml(baseUrl, body,
				HttpRequestConfig.RequestBodyAsString,
				HttpResponseConfig.ResponseAsStream);
		return html;
	}

	protected Map<Analyst, Object> Analysis(String html) {
		int succCount = 0;
		int failCount = 0;
		Document doc = Jsoup.parse(html, baseUrl);
		Elements pageNodes = doc.select("div[class=mass]");
		String text = pageNodes.html();
		String[] data = text.split("\n");
		for (String str : data) {
			Matcher matcher = pattern.matcher(str);
			if (matcher.find()) {
				String each = matcher.group(1);
				String[] arr = each.split(":");
				String host = arr[0];
				int port = 0;
				try {
					port = Integer.parseInt(arr[1]);
				} catch (Exception e) {
					failCount++;
					continue;
				}
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("host", host);
				map.put("port", Integer.valueOf(port));
				String sql = "replace into tb_proxy (host,port,update_time) values (:host,:port,now())";
				DBDataWriter writer = new DBDataWriter(sql);
				writer.writeSingle(map);
				succCount++;
			}
		}
		this.analystResult.put(Analyst.SuccCount, Integer.valueOf(succCount));
		this.analystResult.put(Analyst.FailCount, Integer.valueOf(failCount));
		this.analystResult.put(Analyst.Info, "succ");
		return this.analystResult;
	}

	public static void main(String[] args) {
		BaseClawer b = new ProxyClawer();
		new Thread(b).start();
	}
}