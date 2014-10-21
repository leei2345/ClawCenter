package com.aizhizu.core;

import com.aizhizu.dao.DBDataWriter;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 * 数据处理核心
 * @author leei
 *
 */
public abstract class BaseHandler implements Runnable {
	protected static Logger logger = LoggerFactory.getLogger("ClawerLogger");
	protected String identidy;

	public BaseHandler(String identidy) {
		this.identidy = identidy;
	}
	
	/**
	 * 开始工作
	 */
	protected abstract void StartHandle() throws Exception;

	public void run() {
		/** 修改数据库状态为工作中 */
		ChangeScheduledStatus(1);
		try {
			StartHandle();
		} catch (Exception e) {
			e.printStackTrace();
		}
		/** 修改数据库状态为休息中 */
		ChangeScheduledStatus(0);
	}

	/**
	 * 修改数据库调度的工作状态
	 * @param status
	 */
	private void ChangeScheduledStatus(int status) {
		String sql = "update tb_scheduled_conf set status=:status where identidy='"
				+ this.identidy + "'";
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("status", Integer.valueOf(status));
		DBDataWriter writer = new DBDataWriter(sql);
		writer.writeSingle(map);
	}
}