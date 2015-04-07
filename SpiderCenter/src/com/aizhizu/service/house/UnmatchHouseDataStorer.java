package com.aizhizu.service.house;

import java.util.Map;

import com.aizhizu.dao.DataBaseCenter;

/**
 * 无法match数据异步写入数据库
 * @author leei
 *
 */
public class UnmatchHouseDataStorer implements Runnable {
	private Map<String, Object> map;

	public UnmatchHouseDataStorer(Map<String, Object> map) {
		this.map = map;
	}

	public void run() {
		String plot = (String) map.get("plot");
		String area = (String) map.get("area");
		String district = (String) map.get("district");
		String yx = (String) map.get("yx");
		String sourceUrl = (String) map.get("source_url");
		String sql = "replace into tb_plot_unmatch (plot,area,district,yx,source_url,status) values ('" + plot + "','" + area + "','" + district + "','" + yx + "','" + sourceUrl + "',0)";
		DataBaseCenter.Dao.equals(sql);
	}
}