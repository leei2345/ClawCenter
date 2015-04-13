package com.aizhizu.service.house.ganji;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.time.FastDateFormat;
import org.jdiy.core.Ls;
import org.jdiy.core.Rs;

import com.aizhizu.dao.DataBaseCenter;
import com.aizhizu.util.LoggerUtil;

public class UserCenter {
	
	private static AtomicInteger loginIndex = new AtomicInteger(0);
	private static AtomicInteger userIndex = new AtomicInteger(0);
	private static FastDateFormat sim = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");
	private static Map<Integer, UserEntity> userMap = new HashMap<Integer, UserEntity>();
	private static List<UserEntity> loginUserList = new ArrayList<UserEntity>();
	private static String host;
	
	static {
		ResetUserMap();
	}
	
	public static void ResetUserMap () {
		InetAddress inet = null;
		try {
			inet = InetAddress.getLocalHost();
		} catch (Exception e) {
			host = "10.172.252.245";
		}
		host = inet.getHostAddress();
		
//		host = "127.0.0.1";
		
		String updateSql = "update tb_ganji_user set status=0 where status!=2 and hostname='" + host + "'";
		DataBaseCenter.Dao.exec(updateSql);
		userMap.clear();
		String sql = "select name,passwd,status,update_time from tb_ganji_user where hostname='" + host + "'";
		Ls ls = DataBaseCenter.Dao.ls(sql, 0, 0);
		Rs[] items = ls.getItems();
		for (int index = 0; index < items.length; index++) {
			Rs map = items[index];
			String name = map.get("name");
			String passwd = map.get("passwd");
			Date date = map.getDate("update_time");
			String updateTime = sim.format(date);
			int status = map.getInt("status");
			UserEntity u = new UserEntity(name, passwd);
			u.setUpdateTime(updateTime);
			u.setIndex(index);
			switch (status) {
			case 0:
				u.setStatNotOnUse(UserStat.Normal);
				break;
			case 1:
				u.setStatNotOnUse(UserStat.UseLess);
				break;
			case 2:
				u.setStatNotOnUse(UserStat.PasswdError);
				break;
			case 3:
				u.setStatNotOnUse(UserStat.Other);
				break;
			case 4:
				u.setStatOnUse(0);
				break;
			default:
				break;
			}
			userMap.put(index, u);
		}
		LoggerUtil.ClawerLog("web_ganji", "[==============UserCenter Init done==============][UserList Size " + userMap.size() + "]");
	
	}

	public static int GetUserCount () {
		return userMap.size();
	}

	public static UserEntity GetNextLoginUser () {
		UserEntity u = null;
		synchronized (loginUserList) {
			loginIndex.addAndGet(1);
			int index = loginIndex.get();
			if (index > (loginUserList.size() - 1)) {
				index = 0;
				loginIndex = new AtomicInteger(index);
			}
			u = loginUserList.get(index);
			if (u != null) {
				LoggerUtil.ClawerLog("web_ganji","[==============UserCenter GetLoginUser Done][LoginList Size " + loginUserList.size() + "][============" + u.getName() + "============]");
			} else {
				LoggerUtil.ClawerLog("web_ganji","[==============UserCenter GetLoginUser Fail][LoginList Size " + loginUserList.size() + "]");
			}
		}
		return u;
	}
	
	public static synchronized UserEntity GetNextUser () {
		userIndex.addAndGet(1);
		int index = userIndex.get();
		if (index > (userMap.size() - 1)) {
			index = 0;
			userIndex = new AtomicInteger(index);
			ResetUserMap();
		}
		UserEntity u = null;
		u = userMap.get(index);
		if (u != null) {
			LoggerUtil.ClawerLog("web_ganji","[==============UserCenter GetUser Done][============" + u.getName() + "============]");
		}
		return u;
	}
	
	
	public static void SetUserStatusInactive (UserEntity user, UserStat stat) {
		String name = user.getName();
		int index = user.getIndex();
		if (!stat.equals(UserStat.Normal)) {
			synchronized (loginUserList) {
				loginUserList.remove(user);
			}
		}
		user.setStatNotOnUse(stat);
		int statusCode = stat.getStatusCode();
		String sql = "update tb_ganji_user  set status=" + statusCode + ",update_time=now() where name='" + name + "'";
		DataBaseCenter.Dao.exec(sql);
		user.setUpdateTime(sim.format(new Date()));
		userMap.put(index, user);
		LoggerUtil.ClawerLog("web_ganji","[============UserCenter Update UserStat " + stat.getStatus() + " Done============][============" + name + "============]");
	}
	
	public static Map<Integer, UserEntity> getUserMap() {
		return userMap;
	}
	
	public static synchronized int GetLoginUserListSize () {
		synchronized (loginUserList) {
			return loginUserList.size();
		}
	}
	
	public static synchronized void ClearLoginUserList () {
		synchronized (loginUserList) {
			loginUserList.clear();
		}
	}
	
	public static synchronized void AddLoginUser (UserEntity u) {
		synchronized (loginUserList) {
			loginUserList.add(u);
		}
	}

	public static void main(String[] args) {}
	
}
