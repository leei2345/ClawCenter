package com.aizhizu.dao;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DBExec {

	private static final Logger log = LoggerFactory.getLogger(DBExec.class);

	protected String sql;
	protected List<String> sqlList;
	protected GeneralizeDao gdao = null;

	private String dbenv;

	public String getDbenv() {
		return dbenv;
	}

	public void setDbenv(String dbenv) {
		this.dbenv = dbenv;
	}

	public void setSqlList(List<String> sqlList) {
		this.sqlList = sqlList;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public void dbExec() {
		if (dbenv == null)
			gdao = DBUtilSingle.getGeneralizeDao();
		else {
			gdao = DBUtilSingle.getGeneralizeDao(dbenv);
		}

		if (sql != null) {
			log.debug(sql);
			gdao.getJdbcTemplate().execute(sql);
		}

		if (sqlList != null) {
			for (String t : sqlList) {
				log.debug(t);
				gdao.getJdbcTemplate().execute(t);
			}
		}
	}

	public GeneralizeDao getGDao() {
		if (gdao == null) {
			if (dbenv == null)
				gdao = DBUtilSingle.getGeneralizeDao();
			else {
				gdao = DBUtilSingle.getGeneralizeDao(dbenv);
			}
		}
		return gdao;
	}
	
	public void close() {
		gdao.close();
	}
}
