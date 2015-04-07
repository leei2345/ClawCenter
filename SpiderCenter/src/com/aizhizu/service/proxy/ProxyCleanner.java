package com.aizhizu.service.proxy;

import com.aizhizu.dao.DataBaseCenter;
import com.aizhizu.service.Analyst;
import com.aizhizu.service.BaseClawer;
import com.aizhizu.util.ConfigUtil;

import java.util.Map;

public class ProxyCleanner extends BaseClawer {
	private static String identidy = "proxy_delete";
	private static float threshold = 4.0F;

	public ProxyCleanner() {
		super(identidy);
		threshold = ConfigUtil.getInt("proxy.del.threshold");
	}

	public void run() {
	}

	protected void init() {
	}

	protected String GetHtml() {
		return null;
	}

	protected Map<Analyst, Object> Analysis(String html) {
		String sql = "delete FROM tb_proxy WHERE (avail =0  AND unavail-avail > 24) OR (avail!=0 AND unavail!=0 AND  unavail/avail  > " + threshold + ")";
		int count = DataBaseCenter.Dao.exec(sql);
		this.analystResult.put(Analyst.Info, "succ");
		this.analystResult.put(Analyst.SuccCount, count);
		return this.analystResult;
	}

	public static void main(String[] args) {
		Runnable r = new ProxyCleanner();
		new Thread(r).start();
	}
}