package com.aizhizu.service.house;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jdiy.core.Ls;
import org.jdiy.core.Rs;

import com.aizhizu.dao.DataBaseCenter;

public class PlotDataMatcher {
	
	private static List<String> plotList = new ArrayList<String>();
	
	static {
		InitPlotList();
	}
	
	public static void InitPlotList () {
		String sql = "select plot from tb_plot group by plot";
		Ls ls = DataBaseCenter.Dao.ls(sql, 0, 0);
		Rs[] items = ls.getItems();
		for (Rs map : items) {
			String plotName = map.get("plot");
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
