package com.aizhizu.service;

import java.util.Calendar;

/**
 * 控制中心工具父类
 * @author leei
 *
 */
public class ControlCenter {
	
	protected static synchronized long getDelayTime(String[] runtimeArr) {
		int week = Integer.valueOf(runtimeArr[0]).intValue();
		int hour = Integer.valueOf(runtimeArr[1]).intValue();
		int minute = Integer.valueOf(runtimeArr[2]).intValue();
		int second = Integer.valueOf(runtimeArr[3]).intValue();
		Calendar calendar = Calendar.getInstance();
		long now = System.currentTimeMillis();
		int nowWeek = calendar.get(7);
		long delay = 0L;

		Calendar wanter = Calendar.getInstance();
		wanter.set(7, week);
		wanter.set(11, hour);
		wanter.set(12, minute);
		wanter.set(13, second);

		long interval = wanter.getTimeInMillis();
		if (nowWeek > week) {
			wanter.add(3, 1);
		} else if (now > interval) {
			wanter.add(6, 1);
		}

		interval = wanter.getTimeInMillis();
		delay = interval - now;

		return delay;
	}

	protected static String printRuntime(long delayTime) {
		long day = delayTime / 86400000L;
		long hour = delayTime / 3600000L - day * 24L;
		long min = delayTime / 60000L - day * 24L * 60L - hour * 60L;
		long sec = delayTime / 1000L - day * 24L * 60L * 60L - hour * 60L * 60L
				- min * 60L;
		String print = day + " Day " + hour + " Hour " + min + " Minute " + sec
				+ " Second";
		return print;
	}
}