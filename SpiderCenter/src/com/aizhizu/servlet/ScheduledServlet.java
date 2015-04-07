package com.aizhizu.servlet;

import com.aizhizu.bean.ScheduledEntity;
import com.aizhizu.core.ServiceControlCenter;
import com.aizhizu.dao.DataBaseCenter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.jdiy.core.Ls;
import org.jdiy.core.Rs;

public class ScheduledServlet extends HttpServlet {
	private static final long serialVersionUID = 2297443352058184661L;

	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String type = req.getParameter("type").trim();
		if (StringUtils.equals(type, "0")) {
			String sql = "select conf,identidy,status from tb_scheduled_conf";
			Ls ls = DataBaseCenter.Dao.ls(sql, 0, 0);
			Rs[] list = ls.getItems();
			List<String> sList = new ArrayList<String>();
			for (Rs map : list) {
				ScheduledEntity s = new ScheduledEntity();
				String conf = map.get("conf");
				List<String> confList = new ArrayList<String>();
				String[] confArr = conf.split(";");
				for (String confNode : confArr) {
					confList.add(confNode.trim());
				}
				s.setConf(confList);
				String identidy = (String) map.get("identidy");
				int status = map.getInt("status");
				s.setIdentidy(identidy);
				s.setStatus(status);
				sList.add(s.toString());
			}
			PrintWriter out = resp.getWriter();
			out.print(sList);
			out.flush();
		} else {
			String status = req.getParameter("status").trim();
			String identidy = type;
			if (StringUtils.equals("1", status)) {
				String config = req.getParameter("conf").trim();
				ServiceControlCenter.RestartHandle(identidy, config);
			} else {
				ServiceControlCenter.StopHandle(identidy);
			}
		}
	}

	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doGet(req, resp);
	}
}