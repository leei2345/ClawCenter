package com.aizhizu.service.proxy;

import com.aizhizu.dao.DBDataReader;
import com.aizhizu.dao.DBDataWriter;
import com.aizhizu.service.Analyst;
import com.aizhizu.service.BaseClawer;
import com.aizhizu.util.ConfigUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class ProxyCleanner extends BaseClawer {
	private static String identidy = "proxy_delete";
	private static float threshold = 3.0F;
	private int cleanProxyCount = 0;

	public ProxyCleanner() {
		super(identidy);
		threshold = ConfigUtil.getInt("proxy.del.threshold");
	}

	public void run() {
	}

	protected void init() {
	}

	@SuppressWarnings("unchecked")
	protected String GetHtml() {
		String sql = "select id FROM tb_proxy WHERE avail !=0 AND unavail!=0 AND  unavail/avail  > " + threshold;
		DBDataReader reader = new DBDataReader(sql);
		List<Map<String, Object>> list = reader.readAll();
		this.cleanProxyCount = list.size();
		this.box = new Vector<String>();
		this.box.add(0, "unavali proxy count " + this.cleanProxyCount);
		return null;
	}

	protected Map<Analyst, Object> Analysis(String html) {
		String sql = "DELETE FROM tb_proxy WHERE avail !=0 AND unavail!=0 AND  unavail/avail  > :threshold";
		Map<String, Float> map = new HashMap<String, Float> ();
		map.put("threshold", Float.valueOf(threshold));
		DBDataWriter writer = new DBDataWriter(sql);
		writer.writeSingle(map);
		this.analystResult.put(Analyst.Info, "succ");
		this.analystResult.put(Analyst.SuccCount, Integer.valueOf(1));
		return this.analystResult;
	}

	public static void main(String[] args) {
		Runnable r = new ProxyCleanner();
		new Thread(r).start();
	}
}