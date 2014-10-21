package com.aizhizu.dao;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;


public class DBDataWriter extends DataWriter {

	private String sql;
	private GeneralizeDao gdao = null;
	
	private String dbenv;
	public String getDbenv() {
		return dbenv;
	}

	public void setDbenv(String dbenv) {
		this.dbenv = dbenv;
	}
	
	public DBDataWriter(String sql) {
		this.sql = sql;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected void batchWrite(List l) {
		List<Map> lm = (List<Map>) l;
		gdao.batchInsert(sql, lm);
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected void singleWrite(Object para) {
		Map m = new HashMap();
		try {
			m = (Map) para;
		} catch (Exception e) {
			try {
				m = BeanUtils.describe(para);
			} catch (IllegalAccessException e1) {
				e1.printStackTrace();
			} catch (InvocationTargetException e1) {
				e1.printStackTrace();
			} catch (NoSuchMethodException e1) {
				e1.printStackTrace();
			}
		}
		gdao.insert(sql, m);
	}

	@Override
	protected void open() {
		if (dbenv == null)
			gdao = DBUtilSingle.getGeneralizeDao();
		else{
			gdao = DBUtilSingle.getGeneralizeDao(dbenv);
		}
	}

	@Override
	public void close() {
		gdao.close();
	}
}
