package com.aizhizu.dao;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.jdiy.core.App;
import org.jdiy.core.Dao;
import org.jdiy.core.JDiyContext;
import org.jdiy.util.Fs;

import com.aizhizu.util.LoggerUtil;
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;



public class DataBaseCenter {
	
	public static Dao Dao;
	public static JDiyContext app;
	
	static {
		InitDao();
	}
	
	public static void InitDao () {
		if (Dao == null || !Dao.transExists()) {
			try {
				Dao = CreateNewDao();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static Connection getConnection() {
		Connection conn = null;
		try {
			conn = DataBaseCenter.Dao.getConn();
			if (conn.isClosed()) {
				Dao = null;
				InitDao();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return conn;
	}
	
	public static Dao CreateNewDao () {
		app = App.get();
		//下面获取默认的数据库操作DAO
		Dao dao = null;
		try {
			dao = app.getDao();
			LoggerUtil.DBLog("应用模式：javaWEB");
		} catch (Exception e) {
			LoggerUtil.DBLog("应用模式：javaSE");
		}
		if (dao == null) {
			String path = DataBaseCenter.class.getClassLoader().getResource("").getPath();
			URL xmlLocation;
			try {
				xmlLocation = new URL("file:" + path + "jdiy.xml");
				Fs.getResource("");
				String rootPath = Fs.getResource("../../").getPath();
				app = App.newInstance(xmlLocation, rootPath);
				dao = app.getDao();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		return dao;
	}
	
	public static int InsertAndGetNewId (String insertScoreHistorySql) {
		Connection conn = DataBaseCenter.Dao.getConn();
		PreparedStatement insertStatement = null;
		ResultSet generatedKeys = null;
		int insertId = 0;
		try {
			insertStatement = conn.prepareStatement(insertScoreHistorySql, Statement.RETURN_GENERATED_KEYS);
			insertStatement.execute();
			generatedKeys = insertStatement.getGeneratedKeys();
			if(generatedKeys.next()) {
				insertId = generatedKeys.getInt(1);
			}
		} catch (MySQLIntegrityConstraintViolationException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (generatedKeys != null) 
				try {
					generatedKeys.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
	        if (insertStatement != null)
	        	try {
	        		insertStatement.close();
	        	} catch (SQLException e) {
	        		e.printStackTrace();
	        	}
		}
		return insertId;
	}
	
	public static void close(Connection conn, Statement st, ResultSet rs) {
		if (rs != null) {
			try {
				rs.close();
			}
			catch (SQLException e) {
				LoggerUtil.DBLog("Close Resultset fail.");
			}
		}
		if (st != null) {
			try {
				st.close();
			}
			catch (SQLException e) {
				LoggerUtil.DBLog("Close Statement fail.");
			}
		}
		try {
			conn.close();
		}
		catch (SQLException e) {
			LoggerUtil.DBLog("Close DB Connection fail.");
		}
	}
	

}
