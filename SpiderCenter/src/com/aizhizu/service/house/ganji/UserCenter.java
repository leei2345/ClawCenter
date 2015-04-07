package com.aizhizu.service.house.ganji;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.time.FastDateFormat;
import org.jdiy.core.Ls;
import org.jdiy.core.Rs;

import com.aizhizu.bean.UserEntity;
import com.aizhizu.dao.DataBaseCenter;
import com.aizhizu.util.LoggerUtil;

public class UserCenter {
	
	private static AtomicInteger userIndex = new AtomicInteger(0);
	private static FastDateFormat sim = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");
	private static List<UserEntity> userList = new ArrayList<UserEntity>();
	private static String host;
	
	static {
		ResetStat(1);
	}

	public static void ResetStat (int code) {
		InetAddress inet = null;
		try {
			inet = InetAddress.getLocalHost();
		} catch (Exception e) {
			host = "127.0.0.1";
		}
		host = inet.getHostAddress();
		if (code == 0) {
			String updateSql = "update tb_ganji_user set status=0 where status!=2 and hostname='" + host + "'";
			DataBaseCenter.Dao.exec(updateSql);
		} else {
			String updateSql = "update tb_ganji_user set status=0 where status!=2 and status!=1 and hostname='" + host + "'";
			DataBaseCenter.Dao.exec(updateSql);
		}
		userList.clear();
		String sql = "select name,passwd,status,update_time from tb_ganji_user where hostname='" + host + "'";
		Ls ls = DataBaseCenter.Dao.ls(sql, 0, 0);
		Rs[] items = ls.getItems();
		for (Rs map : items) {
			String name = map.get("name");
			String passwd = map.get("passwd");
			Date date = map.getDate("update_time");
			String updateTime = sim.format(date);
			int status = map.getInt("status");
			UserEntity u = new UserEntity(name, passwd);
			u.setUpdateTime(updateTime);
			switch (status) {
			case 0:
				u.setStat(UserStat.Normal);
				break;
			case 1:
				u.setStat(UserStat.UseLess);
				break;
			case 2:
				u.setStat(UserStat.PasswdError);
				break;
			case 3:
				u.setStat(UserStat.Other);
				break;
			case 4:
				u.setStat(UserStat.OnUse);
				break;
			default:
				break;
			}
			userList.add(u);
		}
		
		LoggerUtil.ClawerLog("[==============UserCenter Init done==============]");
	}

	
	public static int GetUserCount () {
		return userList.size();
	}

	public static UserEntity GetNextUser () {
		int index = userIndex.get();
		if (index > (userList.size() - 1)) {
			index = 0;
			userIndex = new AtomicInteger(index);
			ResetStat(0);
		}
		UserEntity u = null;
		for (; index < userList.size();) {
			u = userList.get(index);
			int count = u.getCount();
			UserStat s = u.getStat();
			if (count >= 10 || (!s.equals(UserStat.Normal) && !s.equals(UserStat.OnUse))) {
				index++;
				userIndex = new AtomicInteger(index);
				continue;
			}
			u.addCount(1);
			u.setStat(UserStat.OnUse);
			break;
		}
		LoggerUtil.ClawerLog("[==============UserCenter Update OnUse Done][============" + u.getName() + "============]");
		return u;
	}
	
	public static void SetUserStatusInactive (UserEntity user, UserStat stat) {
		String name = user.getName();
		user.setStat(stat);
		if (!stat.equals(UserStat.Normal)) {
			user.setCookie(null);
		}
		int statusCode = stat.getStatusCode();
		String sql = "update tb_ganji_user  set status=" + statusCode + ",update_time=now() where name='" + name + "'";
		DataBaseCenter.Dao.exec(sql);
		user.setUpdateTime(sim.format(new Date()));
		LoggerUtil.ClawerLog("[============UserCenter Update UserStat " + stat.getStatus() + " Done============][============" + name + "============]");
	}
	
	public static List<UserEntity> GetUserStatMap () {
		return userList;
	}
	
	public static void main(String[] args) {
		UserCenter.ResetStat(0);
	}
	
}
