package com.aizhizu.dao;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class DBUtilSingle {
	private final static String CONFIG = "classpath:applicationContext.xml";
	private static ApplicationContext CONTEXT;
	private final static String DAO_PREFIX = "generalizeDao";
	private static GeneralizeDao gdao = null;
	private static GeneralizeDao envgdao = null;
	static {
		CONTEXT = new ClassPathXmlApplicationContext(CONFIG);
	}

	public synchronized static ApplicationContext getContext() {
		return CONTEXT;
	}

	public synchronized static GeneralizeDao getGeneralizeDao() {

		if (gdao == null) {
			ApplicationContext ctx = new ClassPathXmlApplicationContext(CONFIG);
			gdao = (GeneralizeDao) ctx.getBean(DAO_PREFIX);
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return gdao;
	}

	public synchronized static GeneralizeDao getGeneralizeDao(String env) {

		if (envgdao == null) {
			ApplicationContext ctx = SpringUtil.getSpringContext();
			envgdao = (GeneralizeDao) ctx.getBean(DAO_PREFIX + "_" + env);
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return envgdao;
	}
	
	public void close(){
		gdao.close();
	}
}
