package com.aizhizu.service.proxy;

import com.aizhizu.dao.DBDataWriter;
import com.aizhizu.service.Analyst;
import com.aizhizu.service.BaseClawer;
import com.aizhizu.util.CountDownLatchUtils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;

public class WrapperOfProxyChecker extends BaseClawer {
	private static String mark = "proxy check";
	private static String loaderPath = null;
	private static ClassLoader loader = null;
	private HttpHost host = null;
	private Map<String, Object> proxyCheckResult = new HashMap<String, Object>();

	public WrapperOfProxyChecker(CountDownLatchUtils cdl) {
		super(mark);
		this.cdl = cdl;
		if (loaderPath == null) {
			loaderPath = getClass().getClassLoader().getResource("").getPath();
			ClassLoader f = Thread.currentThread().getContextClassLoader();
			try {
				loader = new URLClassLoader(new URL[] { new URL("file:"	+ loaderPath) }, f);
			} catch (MalformedURLException e1) {
				e1.printStackTrace();
			}
		}
	}

	public void run() {
		Implement();
	}

	protected void init() {
		String[] ipData = ((String) this.box.get(0)).split(":");
		String ip = ipData[0];
		int port = Integer.parseInt(ipData[1]);
		this.proxyCheckResult.put("host", ip);
		this.proxyCheckResult.put("port", Integer.valueOf(port));
		this.host = new HttpHost(ip, port);
	}

	protected String GetHtml() {
		return null;
	}

	@SuppressWarnings("rawtypes")
	protected Map<Analyst, Object> Analysis(String html) {
		Package p = getClass().getPackage();
		String packageName = p.getName();
		File wrappers = new File(loaderPath + packageName.replace(".", "/")
				+ "/wrapper/");
		File[] wrapperArr = wrappers.listFiles();
		int succCount = 0;
		for (File file : wrapperArr) {
			String wrapperName = file.getName().replace(".class", "");
			try {
				Class clazz = loader.loadClass(packageName + ".wrapper."
						+ wrapperName);
				BaseProxyChecker proxyChecker = (BaseProxyChecker) clazz
						.newInstance();
				proxyChecker.InstallProxyHost(this.host);
				String identidy = proxyChecker.getIdentidy();
				int checkProxyStatus = proxyChecker.CheckApplicability();
				if (checkProxyStatus == 1) {
					succCount++;
				}
				this.proxyCheckResult.put(identidy,
						Integer.valueOf(checkProxyStatus));
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		try {
			ChangeDBProxyStatus();
		} catch (Exception e) {
			this.analystResult.put(Analyst.FailCount, Integer.valueOf(1));
		}
		if (succCount > 0) {
			this.analystResult.put(Analyst.SuccCount, Integer.valueOf(1));
			this.analystResult.put(Analyst.Info, "succ");
		} else {
			this.analystResult.put(Analyst.FailCount, Integer.valueOf(1));
			this.analystResult.put(Analyst.Info, "fail");
		}
		return this.analystResult;
	}

	public void ChangeDBProxyStatus() {
		String innerSql = "";
		if (this.proxyCheckResult.size() != 0) {
			boolean update = false;
			for (Entry<String, Object> entry : this.proxyCheckResult.entrySet()) {
				String key = (String) entry.getKey();
				Object value = entry.getValue();
				if ((!update) && (StringUtils.equals("1", value.toString()))) {
					update = true;
				}

				if ((!StringUtils.equals("host", key))
						&& (!StringUtils.equals("port", key))) {
					String limSql = key + "=" + value + ", ";
					innerSql = innerSql + limSql;
				}
			}
			innerSql = innerSql.substring(0, innerSql.length() - 2);
			String[] hostStr = this.host.toHostString().split(":");
			String sql = "";
			if (update)
				sql = "update tb_proxy set " + innerSql
						+ ", avail=avail+1, update_time=now() where host='"
						+ hostStr[0] + "' and port=" + hostStr[1];
			else {
				sql = "update tb_proxy set " + innerSql
						+ ", unavail=unavail+1, update_time=now() where host='"
						+ hostStr[0] + "' and port=" + hostStr[1];
			}
			DBDataWriter writer = new DBDataWriter(sql);
			writer.writeSingle(this.proxyCheckResult);
		}
	}

	public static void main(String[] args) {
		BaseClawer b = new WrapperOfProxyChecker(new CountDownLatchUtils(1));
		Vector<String> v = new Vector<String>();
		v.add("112.17.0.205:80");
		b.setBox(v);
		new Thread(b).start();
	}
}