package com.aizhizu.dao;

import java.util.List;
import java.util.Map;


public class DBDataReader extends DataReader {

	private GeneralizeDao gdao;
	private String sql;
	@SuppressWarnings("rawtypes")
	private Map param;
	private Page page = new Page();
	
	private String dbenv;
	public String getDbenv() {
		return dbenv;
	}

	public void setDbenv(String dbenv) {
		this.dbenv = dbenv;
	}

	@SuppressWarnings("rawtypes")
	public void setParam(Map param) {
		this.param = param;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	@SuppressWarnings("rawtypes")
	public DBDataReader(String sql, Map param) {
		this.sql = sql;
		this.param = param;
	}

	public DBDataReader(String sql) {
		this.sql = sql;
	}

	public DBDataReader() {
	}

	@SuppressWarnings({ "rawtypes" })
	@Override
	protected List batchRead(int batchCount) {
		page.setSize(batchCount);

		List dataList = gdao.queryListMapByPage(sql, page, param);
		page.incFrom(dataList.size());

		return dataList;
	}

	@Override
	protected Object singleRead() {
		return gdao.querySingleMap(sql, param);
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

	@SuppressWarnings("rawtypes")
	@Override
	protected List allRead() {
		List dataList = gdao.queryListMap(sql, param);
		return dataList;
	}
}
