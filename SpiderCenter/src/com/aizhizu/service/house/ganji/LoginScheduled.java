package com.aizhizu.service.house.ganji;

import java.util.Date;

import org.apache.commons.lang3.time.FastDateFormat;

import com.aizhizu.util.LoggerUtil;

public class LoginScheduled  implements Runnable{

	private static int NeedCount = 10;
	private static FastDateFormat sim = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");
	
	@Override
	public void run() {
		int size = UserCenter.GetLoginUserListSize();
		LoggerUtil.ClawerLog("web_ganji","[LoginScheduled Start][LoginUserList Size " + size + "]");
		int diff = NeedCount - size;
		if (diff > 0) {
			for (int index = 0; index < diff; index++) {
				UserEntity user = UserCenter.GetNextUser();
				boolean res = user.Login();
				if (res) {
					user.setStatOnUse(0);
					user.setUpdateTime(sim.format(new Date()));
					UserCenter.AddLoginUser(user);
				}
			}
			LoggerUtil.ClawerLog("web_ganji","[LoginScheduled Done][LoginUserList Size " + size + "]");
		}
	}

}
