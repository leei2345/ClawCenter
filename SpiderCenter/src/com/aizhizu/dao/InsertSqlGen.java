package com.aizhizu.dao;

import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;


public class InsertSqlGen {
	
	private static GeneralizeDao dao = null;
	 
	public static void main(String[] args) {
		System.out.print(getInsertSql("tuangou","busi_tuangou"));
	}

	public static String getInsertSql(String database,String table_name) {
		String insert_column = "";
		String insert_value = "";
		String insert = "";

		try {
			if (database == null) {
				dao = DBUtilSingle.getGeneralizeDao();
			} else {
				dao = DBUtilSingle.getGeneralizeDao(database);
			}
			List<String> columnList = dao.queryColumn(table_name);

			for (int i = 0; i < columnList.size(); i++) {
				String columnName = StringUtils.lowerCase(columnList.get(i));

				insert_column += "`" + columnName + "`";
				insert_value += ":" + columnName;

				if (i == columnList.size() - 1)
					break;

				insert_column += ",";
				insert_value += ",";
			}
			insert = "insert into " + table_name + " (" + insert_column
					+ ")values(" + insert_value + ")";
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return insert;
	}
	
	public static void close(){
		dao.close();
	}
}
