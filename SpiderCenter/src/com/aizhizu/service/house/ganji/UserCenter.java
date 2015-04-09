package com.aizhizu.service.house.ganji;

import java.net.InetAddress;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.time.FastDateFormat;
import org.jdiy.core.Ls;
import org.jdiy.core.Rs;

import com.aizhizu.dao.DataBaseCenter;
import com.aizhizu.util.LoggerUtil;

public class UserCenter {
	
	private static AtomicInteger userIndex = new AtomicInteger(0);
	private static FastDateFormat sim = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");
	private static Map<Integer, UserEntity> userMap = new HashMap<Integer, UserEntity>();
	private static String host;
	
	static {
		ResetStat(1);
	}

	public static void ResetStat (int code) {
		InetAddress inet = null;
		try {
			inet = InetAddress.getLocalHost();
		} catch (Exception e) {
			host = "10.172.252.245";
		}
		host = inet.getHostAddress();
		
//		host = "10.172.252.245";
		
		if (code == 0) {
			String updateSql = "update tb_ganji_user set status=0 where status!=2 and hostname='" + host + "'";
			DataBaseCenter.Dao.exec(updateSql);
		} else {
			String updateSql = "update tb_ganji_user set status=0 where status!=2 and status!=1 and hostname='" + host + "'";
			DataBaseCenter.Dao.exec(updateSql);
		}
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
		LoggerUtil.ClawerLog("[==============UserCenter Init done==============][UserList Size " + userMap.size() + "]");
	}

	
	public static int GetUserCount () {
		return userMap.size();
	}

	public static synchronized UserEntity GetNextUser () {
		int index = userIndex.get();
		if (index > (userMap.size() - 1)) {
			index = 0;
			userIndex = new AtomicInteger(index);
			ResetStat(0);
		}
		UserEntity u = null;
		for (; index < userMap.size();) {
			u = userMap.get(index);
			int count = u.getCount();
			UserStat s = u.getStat();
			if (count >= 10 || (!s.equals(UserStat.Normal) && !s.equals(UserStat.OnUse))) {
				index++;
				userIndex = new AtomicInteger(index);
				continue;
			}
			u.addCount(1);
			u.setStatOnUse(1);
			userMap.put(index, u);
			break;
		}
		if (u != null) {
			LoggerUtil.ClawerLog("[==============UserCenter Update OnUse Done][============" + u.getName() + "============]");
		}
		return u;
	}
	
	public static void SetUserStatusInactive (UserEntity user, UserStat stat) {
		String name = user.getName();
		user.setStatNotOnUse(stat);
		int index = user.getIndex();
		if (!stat.equals(UserStat.Normal)) {
			user.setCookie(null);
		}
		int statusCode = stat.getStatusCode();
		String sql = "update tb_ganji_user  set status=" + statusCode + ",update_time=now() where name='" + name + "'";
		DataBaseCenter.Dao.exec(sql);
		user.setUpdateTime(sim.format(new Date()));
		userMap.put(index, user);
		LoggerUtil.ClawerLog("[============UserCenter Update UserStat " + stat.getStatus() + " Done============][============" + name + "============]");
	}
	
	public static Map<Integer, UserEntity> getUserMap() {
		return userMap;
	}

	public static void main(String[] args) {
		UserCenter.ResetStat(0);
	}
	
}
