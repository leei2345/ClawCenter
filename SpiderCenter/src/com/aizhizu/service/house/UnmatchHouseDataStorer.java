package com.aizhizu.service.house;

import com.aizhizu.dao.DBDataWriter;
import java.util.Map;

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
		String sql = "replace into tb_plot_unmatch (plot,area,district,yx,source_url,status) values (:plot,:area,:district,:yx,:source_url,0)";
		DBDataWriter writer = new DBDataWriter(sql);
		writer.writeSingle(this.map);
	}
}