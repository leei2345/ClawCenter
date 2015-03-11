package com.aizhizu.service.house.ganji;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aizhizu.dao.DBDataReader;
import com.aizhizu.dao.DBDataWriter;

@SuppressWarnings("unchecked")
public class UserCenter {
	
	public static Map<String, UserStat> userStatusMap = new HashMap<String, UserStat>();
	private static List<UserEntity> userList = new ArrayList<UserEntity>();
	private static AtomicInteger userIndex = new AtomicInteger(0);
	private static UserEntity inUseUser;
	private static final Logger logger = LoggerFactory.getLogger("ClawerLogger");
	
	static {
		Init();
	}

	public static void Init () {
		String updateSql = "update tb_ganji_user set status=:status where status!=2 and status!=1";
		Map<String, Integer> updateMap = new HashMap<String, Integer>();
		updateMap.put("status", 0);
		DBDataWriter updateWriter = new DBDataWriter(updateSql);
		updateWriter.writeSingle(updateMap);
		userList.clear();
		String sql = "select name,passwd,status from tb_ganji_user";
		DBDataReader reader = new DBDataReader(sql);
		List<Map<String, Object>> list = reader.readAll();
		for (Map<String, Object> map : list) {
			String name = (String) map.get("name");
			String passwd = (String) map.get("passwd");
			int status = (int) map.get("status");
			if (status == 0) {
				UserEntity u = new UserEntity(name, passwd);
				userList.add(u);
			}
			switch (status) {
			case 0:
				userStatusMap.put(name, UserStat.Normal);break;
			case 1:
				userStatusMap.put(name, UserStat.UseLess);break;
			case 2:
				userStatusMap.put(name, UserStat.PasswdError);break;
			case 3:
				userStatusMap.put(name, UserStat.Other);break;
			case 4:
				userStatusMap.put(name, UserStat.OnUse);break;
			default:
				break;
			}
		}
		logger.info("[==============UserCenter Init done==============]");
	}

	
	public static int GetUserCount () {
		return userList.size();
	}

	public static UserEntity GetNextUser () {
		int index = userIndex.get();
		if (index >= (userList.size() - 1)) {
			index = 0;
			userIndex = new AtomicInteger(index);
		}
		UserEntity u = userList.get(index);
		inUseUser = u;
		userIndex.addAndGet(1);
		userStatusMap.put(u.getName(), UserStat.OnUse);
		logger.info("[==============UserCenter Update OnUse Done][============" + u.getName() + "============]");
		return u;
	}
	
//	public static void SetUserStatusActive (UserEntity u) {
//		String name = u.getName();
//		userStatusMap.put(name, "使用");
//	}
	
	public static void SetUserStatusInactive (UserStat stat) {
		String name = inUseUser.getName();
		int statusCode = stat.getStatusCode();
		String sql = "update tb_ganji_user  set status=" + statusCode + " where name=:name";
		Map<String, String> map = new HashMap<String, String>();
		map.put("name", name);
		DBDataWriter writer = new DBDataWriter(sql);
		writer.writeSingle(map);
		userStatusMap.put(name, stat);
		logger.info("[============UserCenter Update UserStat " + stat.getStatus() + " Done============][============" + name + "============]");
	}
	
	public Map<String, UserStat> GetUserStatMap () {
		return userStatusMap;
	}
	
	public static void main(String[] args) {
		 String str = "make in \u4e2d\u56fd";  
		 System.out.println(str);
	}
	
}


class UserEntity {
	
	private String name;
	private String passwd;
	
	public UserEntity (String name, String passwd) {
		this.name = name;
		this.passwd = passwd;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPasswd() {
		return passwd;
	}
	public void setPasswd(String passwd) {
		this.passwd = passwd;
	}
	
}