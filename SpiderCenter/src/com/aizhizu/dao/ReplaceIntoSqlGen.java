package com.aizhizu.dao;

import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;


public class ReplaceIntoSqlGen {
	
	private static GeneralizeDao dao = null;
	 
	public static String getReplaceIntoSql(String database,String table_name) {
		String replace_column = "";
		String replace_value = "";
		String replaceinto = "";

		try {
			if (database == null) {
				dao = DBUtilSingle.getGeneralizeDao();
			} else {
				dao = DBUtilSingle.getGeneralizeDao(database);
			}
			List<String> columnList = dao.queryColumn(table_name);

			for (int i = 0; i < columnList.size(); i++) {
				String columnName = StringUtils.lowerCase(columnList.get(i));

				replace_column += "`" + columnName + "`";
				replace_value += ":" + columnName;

				if (i == columnList.size() - 1)
					break;

				replace_column += ",";
				replace_value += ",";
			}
			replaceinto = "replace into " + table_name + " (" + replace_column
					+ ")values(" + replace_value + ")";
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return replaceinto;
	}
	
	public static void close(){
		dao.close();
	}
}
