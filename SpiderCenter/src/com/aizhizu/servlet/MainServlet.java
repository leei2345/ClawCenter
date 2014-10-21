package com.aizhizu.servlet;

import com.aizhizu.core.ServiceControlCenter;
import com.aizhizu.http.HttpMethod;
import com.aizhizu.util.ConfigUtil;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class MainServlet implements ServletContextListener {
	public void contextDestroyed(ServletContextEvent arg0) {
		System.out.println("房源数据获取服务关闭...");
	}

	public void contextInitialized(ServletContextEvent arg0) {
		System.out.println("房源数据获取服务启动...");
		HttpMethod.initProxyMap();
		ConfigUtil.Init();
		System.out.println("启动配置文件读取成功...");
		ServiceControlCenter.ServiceStartInit();
	}
}