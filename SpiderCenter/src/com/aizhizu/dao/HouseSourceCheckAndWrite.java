package com.aizhizu.dao;

import org.jdiy.core.Rs;
import org.jdiy.core.ex.JDiyActionException;

public class HouseSourceCheckAndWrite {

	public static boolean CheckHouseExsist (String url) {
		boolean res = false;
		String sql = "select id from tb_claw_history where url='" + url + "'";
		Rs rs = DataBaseCenter.Dao.rs(sql);
		if (!rs.isNull()) {
			res = true;
		}
		return res;
	}
	
	public static void InsertClawHistory (String url, String source, String path) {
		String sql = "insert tb_claw_history into (url, source, path, update_time) values ('" + url + "','" + source + "','" + path + "',now())";
		try {
			DataBaseCenter.Dao.exec(sql);
		} catch (Exception e) {
			throw new JDiyActionException("Insert Exsist");
		}
	}
	
}
