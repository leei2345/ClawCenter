package com.aizhizu.servlet;

import com.aizhizu.bean.HighChartsEntity;
import com.aizhizu.dao.DataBaseCenter;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.jdiy.core.Ls;
import org.jdiy.core.Rs;

public class MornitorServlet extends HttpServlet {
	private static final long serialVersionUID = 1364250816859877382L;
	private static FastDateFormat sim = FastDateFormat.getInstance("yyyyMMdd");

	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String type = req.getParameter("type").trim();
		if (StringUtils.isBlank(type)) {
			type = "0";
		}
		String grads = req.getParameter("grads");
		if (StringUtils.isBlank(grads)) {
			grads = "0";
		}
		if (StringUtils.equals("0", type)) {
			String sql = "select identidy from tb_mornitor group by identidy";
			List<String> typeList = new ArrayList<String>();
			Ls ls = DataBaseCenter.Dao.ls(sql, 0, 0);
			Rs[] redList = ls.getItems();
			for (Rs map : redList) {
				String value = (String) map.get("identidy");
				if ((!StringUtils.isBlank(value))
						&& (!StringUtils.equals("null", value.toLowerCase()))) {
					typeList.add("\"" + value + "\"");
				}
			}
			PrintWriter out = resp.getWriter();
			out.print(typeList);
			out.flush();
		} else {
			int fromToToday = Integer.parseInt(grads);
			Calendar cal = Calendar.getInstance();
			int todayHour = cal.get(11);
			int hour = 23;
			if (fromToToday == 0) {
				hour = todayHour;
			}
			cal.add(6, fromToToday);
			String selectDate = sim.format(cal);
			HighChartsEntity highChartsEntity = new HighChartsEntity();
			List<String> categories = new ArrayList<String>();
			highChartsEntity.setCategories(categories);
			for (int hourIndex = 0; hourIndex <= hour; hourIndex++) {
				categories.add(String.valueOf(hourIndex));
			}
			highChartsEntity.setCategories(categories);
			List<Map<String, Object>> series = new ArrayList<Map<String, Object>>();

			Map<Integer, Float> avgTimeDataMap = new HashMap<Integer, Float>();
			Map<Integer, Integer> succPercentDataMap = new HashMap<Integer, Integer>();
			for (int hourIndex = 0; hourIndex <= hour; hourIndex++) {
				avgTimeDataMap.put(Integer.valueOf(hourIndex), Float.valueOf(0.0F));
				succPercentDataMap.put(Integer.valueOf(hourIndex),	Integer.valueOf(0));
			}
			String sql = "select avg_time,start_time,succ_percent from tb_clawer_mornitor where identidy='"
					+ type + "' and date='" + selectDate + "'";
			Map<String, Object> avgTimeEntity = new HashMap<String, Object>();
			Map<String, Object> succPercentEntity = new HashMap<String, Object>();
			avgTimeEntity.put("name", "time-consuming");
			avgTimeEntity.put("color", "#4572A7");
			avgTimeEntity.put("type", "column");
			avgTimeEntity.put("yAxis", Integer.valueOf(1));
			avgTimeEntity.put("tooltip", "{valueSuffix: ' min'}");
			succPercentEntity.put("name", "success-ratio");
			succPercentEntity.put("color", "#89A54E");
			succPercentEntity.put("type", "spline");
			succPercentEntity.put("tooltip", "{valueSuffix: ' %'}");
			Ls ls = DataBaseCenter.Dao.ls(sql, 0, 0);
			Rs[] res = ls.getItems();
			for (Rs map : res) {
				String startTime = map.get("start_time");
				startTime = startTime.split(":")[0];
				int startTimeHour = Integer.parseInt(startTime);
				if ((avgTimeDataMap.get(Integer.valueOf(startTimeHour)) != null) && (((Float) avgTimeDataMap.get(Integer.valueOf(startTimeHour))).floatValue() > 0.0F)) {
					continue;
				}
				long usedTime = map.getLong("avg_time");
				BigDecimal usedTimeBig = new BigDecimal(usedTime);
				float usedMinute = usedTimeBig.divide(new BigDecimal(60000), 2, 4).floatValue();
				avgTimeDataMap.put(Integer.valueOf(startTimeHour), Float.valueOf(usedMinute));

				Float succPercentF = Float.valueOf(map .getFloat("succ_percent") * 100.0F);
				int succPercent = succPercentF.intValue();
				succPercentDataMap.put(Integer.valueOf(startTimeHour), Integer.valueOf(succPercent));
			}
			List<Float> avgTimeData = new ArrayList<Float>();
			List<Integer> succPercentData = new ArrayList<Integer>();
			for (int hourIndex = 0; hourIndex <= hour; hourIndex++) {
				avgTimeData.add(avgTimeDataMap.get(Integer.valueOf(hourIndex)));
				succPercentData.add(succPercentDataMap.get(Integer.valueOf(hourIndex)));
			}
			avgTimeEntity.put("data", avgTimeData);
			succPercentEntity.put("data", succPercentData);
			series.add(succPercentEntity);
			series.add(avgTimeEntity);

			highChartsEntity.setSeries(series);
			PrintWriter out = resp.getWriter();
			out.print(highChartsEntity);
			out.flush();
		}
	}

	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doGet(req, resp);
	}
}