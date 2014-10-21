package com.aizhizu.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;


public class GeneralizeDao {

	private JdbcTemplate jdbcTemplate;
	private NamedParameterJdbcTemplate namedJdbcTemplate;

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public NamedParameterJdbcTemplate getNamedJdbcTemplate() {
		return namedJdbcTemplate;
	}

	public void setNamedJdbcTemplate(
			NamedParameterJdbcTemplate namedJdbcTemplate) {
		this.namedJdbcTemplate = namedJdbcTemplate;
	}

	@SuppressWarnings({ "rawtypes" })
	public List queryMapByPage(String sql, Page p) {
		SqlParameterSource param = new BeanPropertySqlParameterSource(p);

		return namedJdbcTemplate.queryForList(sql, param);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List queryListMapByPage(String sql, Page p, Map param) {
		if (param == null)
			param = new HashMap();

		param.put("from", p.getFrom());
		param.put("size", p.getSize());
		return namedJdbcTemplate.queryForList(sql, param);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List queryListMap(String sql, Map param) {
		return namedJdbcTemplate.queryForList(sql, param);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map querySingleMap(String sql, Map param) {
		return namedJdbcTemplate.queryForMap(sql, param);
	}

	@SuppressWarnings({ "rawtypes" })
	public List queryMapByPageByTableName(String table, Page p) {
		SqlParameterSource param = new BeanPropertySqlParameterSource(p);
		String sql = "select * from " + table + " limit :from,:size";
		return namedJdbcTemplate.queryForList(sql, param);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public int[] batchInsert(String sql, List<Map> para) {
		Map[] t = new HashMap[para.size()];
		for (int i = 0; i < para.size(); i++) {
			t[i] = para.get(i);
		}
		return namedJdbcTemplate.batchUpdate(sql, t);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public int insert(String sql, Map para) {
		return namedJdbcTemplate.update(sql, para);
	}

	public List<String> queryColumn(String table) throws SQLException {

		String sql = "select * f" +
				"rom " + table + " where 0=1";
		SqlRowSetMetaData srsmd = jdbcTemplate.queryForRowSet(sql)
				.getMetaData();

		int columnCount = srsmd.getColumnCount();
		List<String> queryColumnNames = new ArrayList<String>(columnCount);
		for (int i = 1; i <= columnCount; i++) {
			queryColumnNames.add(srsmd.getColumnName(i).toLowerCase());
		}
		return queryColumnNames;
	}

	public void beginTransaction() {
		try {
			jdbcTemplate.getDataSource().getConnection().setAutoCommit(false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void commit() {
		try {
			jdbcTemplate.getDataSource().getConnection().commit();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void rollback() {
		try {
			jdbcTemplate.getDataSource().getConnection().rollback();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void close(){
		try {
			jdbcTemplate.getDataSource().getConnection().close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
