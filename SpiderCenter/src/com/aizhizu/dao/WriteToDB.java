package com.aizhizu.dao;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class WriteToDB {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void toDB(String table, List<Map> dataList) {
		// 小写
		List<Map> lm = new ArrayList<Map>();
		for (Map m1 : dataList) {
			// 字段名统�?��小写，并且把中间的引号替换掉，防止不能转成json格式
			Map map = new HashMap();
			for (Object o : m1.keySet()) {
				String s = (String) o;
				String sl = s.toLowerCase();
				map.put(sl, m1.get(o) + "");
			}
			lm.add(map);
		}
		dataList = lm;

		String goodInsertSql = InsertSqlGen.getInsertSql(null, table);
		goodInsertSql = goodInsertSql.replace(",`updatetime`", "").replace(
				",:updatetime", "");
		goodInsertSql = goodInsertSql.replace(",`modify_time`", "").replace(
				",:modify_time", "");
		DBDataWriter ddw = new DBDataWriter(goodInsertSql);
		ddw.writeList(dataList);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void toDBSingle(String table, Map data) {
		// 小写
		// 字段名统�?��小写，并且把中间的引号替换掉，防止不能转成json格式
		Map map = new HashMap();
		for (Object o : data.keySet()) {
			String s = (String) o;
			String sl = s.toLowerCase();
			map.put(sl, data.get(o) + "");
		}

		String goodInsertSql = InsertSqlGen.getInsertSql(null, table);
		goodInsertSql = goodInsertSql.replace(",`updatetime`", "").replace(
				",:updatetime", "");
		goodInsertSql = goodInsertSql.replace(",`modify_time`", "").replace(
				",:modify_time", "");
		DBDataWriter ddw = new DBDataWriter(goodInsertSql);
		ddw.writeSingle(map);
	}
	
	public void close(){
		InsertSqlGen.close();
	}
}
