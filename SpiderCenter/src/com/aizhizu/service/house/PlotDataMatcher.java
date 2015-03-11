package com.aizhizu.service.house;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.aizhizu.dao.DBDataReader;

public class PlotDataMatcher {
	
	private static List<String> plotList = new ArrayList<String>();
	
	static {
		InitPlotList();
	}
	
	@SuppressWarnings("unchecked")
	public static void InitPlotList () {
		String sql = "select plot from tb_plot group by plot";
		DBDataReader reader = new DBDataReader(sql);
		List<Map<String, Object>> list = reader.readList();
		for (Map<String, Object> map : list) {
			String plotName = (String) map.get("plot");
			if (!StringUtils.isBlank(plotName)) {
				plotList.add(plotName);
			}
		}
	}
	
	public static String matchPlot (String plotName) {
		String matchPlot = "";
		if (!StringUtils.isBlank(plotName)) {
			for (String plot : plotList) {
				if (plot.contains(plotName)) {
					matchPlot = plot;
					break;
				}
			}
		}
		return matchPlot;
	}
	
	public static void main(String[] args) {
		String test = "瑞明路6号院";
		String res = PlotDataMatcher.matchPlot(test);
		System.out.println(res);
		
	}
	
	

}
