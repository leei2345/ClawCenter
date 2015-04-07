package com.aizhizu.service.proxy;

import com.aizhizu.bean.MornitorEntity;
import com.aizhizu.dao.DataBaseCenter;
import com.aizhizu.util.ConfigUtil;
import com.aizhizu.util.CountDownLatchUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.HttpHost;
import org.jdiy.core.Ls;
import org.jdiy.core.Rs;

public class ProxyChecker {
	private static String identidy = "proxy_check";

	private ExecutorService threadPool = null;

	private int threadPoolSize = 3;

	private static ConcurrentHashMap<String, List<HttpHost>> proxyMap = null;

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
		CountDownLatchUtils cdl = new CountDownLatchUtils(size);
		if ((this.threadPool == null) || (this.threadPool.isShutdown())) {
			this.threadPool = Executors.newFixedThreadPool(this.threadPoolSize);
		}
		while (!taskList.isEmpty()) {
			BaseProxyClawer proxyChecker = new WrapperOfProxyChecker(cdl);
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

	private ConcurrentLinkedQueue<String> GetTaskList() {
		String sql = "select host,port from tb_proxy order by update_time limit 1000";
		Ls ls = DataBaseCenter.Dao.ls(sql, 0, 0);
		Rs[] items = ls.getItems();
		ConcurrentLinkedQueue<String> taskList = new ConcurrentLinkedQueue<String>();
		for (Rs map : items) {
			String ip = map.get("host");
			int port = map.getInt("port");
			String taskData = ip + ":" + String.valueOf(port);
			taskList.offer(taskData);
		}
		return taskList;
	}

	public static ConcurrentHashMap<String, List<HttpHost>> initProxyMap() {
		String sql = "SELECT column_name from information_schema.columns WHERE table_name='tb_proxy' and column_name LIKE 'web%'";
		Ls ls = DataBaseCenter.Dao.ls(sql, 0, 0);
		Rs[] items = ls.getItems();
		for (Rs map : items) {
			String column_name = map.get("column_name");
			String innerSql = "select host,port from tb_proxy where " + column_name + "=1 limit 200";
			Ls innerLs = DataBaseCenter.Dao.ls(innerSql, 0, 0);
			Rs[] innerRsItems = innerLs.getItems();
			List<HttpHost> proxyList = proxyMap.get(column_name);
			if (proxyList == null) {
				proxyList = new ArrayList<HttpHost>();
			}
			proxyList.clear();
			for (Rs proxyMap : innerRsItems) {
				String host = proxyMap.get("host");
				int port = proxyMap.getInt("port");
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