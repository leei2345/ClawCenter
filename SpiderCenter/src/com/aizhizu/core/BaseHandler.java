package com.aizhizu.core;

import com.aizhizu.dao.DataBaseCenter;
import com.aizhizu.util.LoggerUtil;

/**
 * 
 * 数据处理核心
 * @author leei
 *
 */
public abstract class BaseHandler implements Runnable {
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
			LoggerUtil.ClawerLog("[Handle error][" + e.getMessage() + "]");
		}
		/** 修改数据库状态为休息中 */
		ChangeScheduledStatus(0);
	}

	/**
	 * 修改数据库调度的工作状态
	 * @param status
	 */
	private void ChangeScheduledStatus(int status) {
		String sql = "update tb_scheduled_conf set status=" + status + " where identidy='"+ this.identidy + "'";
		DataBaseCenter.Dao.equals(sql);
	}
}