package com.aizhizu.service.proxy;

import com.aizhizu.bean.MornitorEntity;
import com.aizhizu.dao.DBDataReader;
import com.aizhizu.service.BaseClawer;
import com.aizhizu.util.ConfigUtil;
import com.aizhizu.util.CountDownLatchUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.http.HttpHost;

public class ProxyChecker {
	private static String identidy = "proxy_check";

	private ExecutorService threadPool = null;

	private int threadPoolSize = 3;

	private static ConcurrentHashMap<String, List<HttpHost>> proxyMap = null;
	private static FastDateFormat sim = FastDateFormat
			.getInstance("yyyyMMdd|HH:mm");

	static {
		proxyMap = new ConcurrentHashMap<String, List<HttpHost>>();
	}

	public ProxyChecker() {
		this.threadPoolSize = ConfigUtil.getInt("proxy.check.thread.pool.size");
		this.threadPool = Executors.newFixedThreadPool(this.threadPoolSize);
	}

	public void Implement() {
		ConcurrentLinkedQueue<String> taskList = GetTaskList();
		int size = taskList.size();
		MornitorEntity mornitor = new MornitorEntity(identidy);
		String nowTime = sim.format(new Date());
		String[] timeArr = nowTime.split("\\|");
		String day = timeArr[0];
		String time = timeArr[1];
		mornitor.setDate(day);
		mornitor.setStartTime(time);
		CountDownLatchUtils cdl = new CountDownLatchUtils(size);
		if ((this.threadPool == null) || (this.threadPool.isShutdown())) {
			this.threadPool = Executors.newFixedThreadPool(this.threadPoolSize);
		}
		while (!taskList.isEmpty()) {
			BaseClawer proxyChecker = new WrapperOfProxyChecker(cdl);
			String hostData = (String) taskList.poll();
			Vector<String> box = new Vector<String>();
			box.add(hostData);
			proxyChecker.setBox(box);
			proxyChecker.setMornitor(mornitor);
			this.threadPool.execute(proxyChecker);
		}
		try {
			cdl.await(3000000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		this.threadPool.shutdownNow();
		mornitor.MakeDB();
		this.threadPool = null;
	}

	@SuppressWarnings("unchecked")
	private ConcurrentLinkedQueue<String> GetTaskList() {
		String sql = "select host,port from tb_proxy order by update_time limit 1000";
		DBDataReader reader = new DBDataReader(sql);
		List<Map<String, Object>> dbList = reader.readAll();
		ConcurrentLinkedQueue<String> taskList = new ConcurrentLinkedQueue<String>();
		for (Map<String, Object> map : dbList) {
			String ip = (String) map.get("host");
			int port = ((Integer) map.get("port")).intValue();
			String taskData = ip + ":" + String.valueOf(port);
			taskList.offer(taskData);
		}
		return taskList;
	}

	@SuppressWarnings("unchecked")
	public static ConcurrentHashMap<String, List<HttpHost>> initProxyMap() {
		String sql = "SELECT column_name from information_schema.columns WHERE table_name='tb_proxy' and column_name LIKE 'web%'";
		DBDataReader reader = new DBDataReader(sql);
		List<Map<String, Object>> resList = reader.readList();
		for (Map<String, Object> map : resList) {
			String column_name = (String) map.get("column_name");
			String innerSql = "select host,port from tb_proxy where "
					+ column_name + "=1 limit 200";
			DBDataReader innerReader = new DBDataReader(innerSql);
			List<Map<String, Object>> innerResList = innerReader.readList();
			List<HttpHost> proxyList = proxyMap.get(column_name);
			if (proxyList == null) {
				proxyList = new ArrayList<HttpHost>();
			}
			proxyList.clear();
			for (Map<String, Object> proxyMap : innerResList) {
				String host = (String) proxyMap.get("host");
				int port = ((Integer) proxyMap.get("port")).intValue();
				proxyList.add(new HttpHost(host, port));
			}
			proxyMap.put(column_name, proxyList);
		}
		return proxyMap;
	}

	public static void main(String[] args) {
		ProxyChecker c = new ProxyChecker();
		c.Implement();
		
	}
}