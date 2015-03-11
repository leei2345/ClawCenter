package com.aizhizu.service.proxy;

import com.aizhizu.dao.DBDataReader;
import com.aizhizu.dao.DBDataWriter;
import com.aizhizu.http.HttpMethod;
import com.aizhizu.http.HttpResponseConfig;
import com.aizhizu.http.Method;
import com.aizhizu.service.Analyst;
import com.aizhizu.service.BaseClawer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ProxyClawer extends BaseClawer {
	private static String identidy = "proxy_claw";
	private static String baseUrl = "http://erwx.daili666.com/ip/?tid=557057365685582&num=100&operator=1,2,3&area=%E6%B2%B3%E5%8C%97,%E5%8C%97%E4%BA%AC,%E5%A4%A9%E6%B4%A5,%E5%B1%B1%E4%B8%9C,%E6%B1%9F%E8%8B%8F";
	private static Pattern pattern = Pattern.compile("((\\d{1,3}\\.){3}\\d{1,3}:\\d+)");
//	protected static FastDateFormat fdf = FastDateFormat.getInstance("yyyyMMdd|HH:mm");

	public ProxyClawer() {
		super(identidy);
	}

	public void run() {
	}

	protected void init() {
	}

	protected String GetHtml() {
		HttpMethod http = new HttpMethod("proxyclaw");
		http.AddHeader(Method.Get, "Host", "erwx.daili666.com");
		http.AddHeader(Method.Get, "User-Agent",	"Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:35.0) Gecko/20100101 Firefox/35.0");
		http.AddHeader(Method.Get, "Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		http.AddHeader(Method.Get, "Accept-Language", "zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3");
		http.AddHeader(Method.Get, "Accept-Encoding", "gzip, deflate");
		http.AddHeader(Method.Get, "Connection", "keep-alive");
//		http.AddHeader(Method.Get, "Content-Type", "application/x-www-form-urlencoded");
		http.AddHeader(Method.Get, "If-None-Match", "");
		http.AddHeader(Method.Get, "Cache-Control", "");
//		http.AddHeader(Method.Get, "Referer", "http：//miwangip.duapp.com/");
		String html = http.GetHtml(baseUrl,	HttpResponseConfig.ResponseAsString);
		return html;
	}

	@SuppressWarnings("unchecked")
	protected Map<Analyst, Object> Analysis(String html) {
		int succCount = 0;
		int failCount = 0;
		String[] data = html.split("\n");
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
				String selectSql = "select host from tb_proxy where host='" + host + "' and port=" + port;
				DBDataReader reader = new DBDataReader(selectSql);
				List<Object> list = reader.readList();
				if (list.size() == 0) {
					String sql = "insert into tb_proxy (host,port,update_time) values (:host,:port,now())";
					DBDataWriter writer = new DBDataWriter(sql);
					writer.writeSingle(map);
				}
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
		b.Implement();
	}
}