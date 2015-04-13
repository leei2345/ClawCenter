package com.aizhizu.core;

import com.aizhizu.dao.DataBaseCenter;
import com.aizhizu.util.LoggerUtil;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Calendar;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.jdiy.core.Ls;
import org.jdiy.core.Rs;

/**
 * 控制中心
 * @author leei
 *
 */
public class ServiceControlCenter {
	/** 调度定时器容器 */
	private static ConcurrentHashMap<String, ScheduledExecutorService> scheduledMap = new ConcurrentHashMap<String, ScheduledExecutorService>();
	/** 反射公用ClassLoader */
	private static ClassLoader loader = null;
	/** 反射所在路径 */
	private static String packageName;

	static {
		String classLoaderPath = ServiceControlCenter.class.getClassLoader().getResource("").getPath();
		ClassLoader Floader = Thread.currentThread().getContextClassLoader();
		Package p = ServiceControlCenter.class.getPackage();
		packageName = p.getName();
		try {
			loader = new URLClassLoader(new URL[] { new URL("file:"+ classLoaderPath) }, Floader);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 调度控制中心初始化
	 */
	public static void ServiceStartInit() {
		String sql = "select identidy from tb_scheduled_conf";
		Ls ls = DataBaseCenter.Dao.ls(sql, 0, 0);
		Rs[] items = ls.getItems();
		for (Rs map : items) {
			String identidy = map.get("identidy");
			try {
				StartHandle(identidy, null, true);
			} catch (Exception localException) {
				continue;
			}
		}
	}

	/**
	 * 针对不同调度的重启控制
	 * @param handleName
	 * @param conf
	 */
	public static void RestartHandle(String handleName, String conf) {
		StartHandle(handleName, conf, false);
		ChangeScheduledConf(handleName, conf);
	}

	/**
	 * 调度任务启动
	 * @param handleName
	 * @param conf
	 * @param init 是否是初始化启动
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	private static boolean StartHandle(String handleName, String conf, boolean init) {
		boolean res = false;
		String sql = "select clazz,conf,status from tb_scheduled_conf where identidy='" + handleName + "'";
		Rs rs = DataBaseCenter.Dao.rs(sql);
		if (conf == null) {
			conf = rs.get("conf");
		}
		if (init) {
			int status = rs.getInt("status");
			if (status == 2) {
				res = true;
				return res;
			}
		}
		String clazzName = rs.get("clazz");
		String[] configDataArr = conf.split(";");
		BigDecimal intervalHoursBig = new BigDecimal(configDataArr[4]);
		BigDecimal hourTime = new BigDecimal(3600000);
		BigDecimal intervalTimeBig = intervalHoursBig.multiply(hourTime);
		String intervalTimeStr = String.valueOf(intervalTimeBig);
		long intervalTime = Long.parseLong(intervalTimeStr.replaceAll("\\..*",	""));
		String[] runTimeArr = new String[4];
		System.arraycopy(configDataArr, 0, runTimeArr, 0, 4);
		long delayTime = getDelayTime(runTimeArr);
		ScheduledExecutorService service =  scheduledMap.get(handleName);
		if ((service != null) && (!service.isShutdown())) {
			service.shutdownNow();
			service = null;
		}
		service = Executors.newSingleThreadScheduledExecutor();
		try {
			Class clazz = loader.loadClass(packageName + ".scheduled."+ clazzName);
			Object instance = clazz.newInstance();
			BaseHandler clawer = (BaseHandler) instance;
			service.scheduleAtFixedRate(clawer, delayTime, intervalTime,TimeUnit.MILLISECONDS);
			scheduledMap.put(handleName, service);
			LoggerUtil.InfoLog("[" + handleName + "][" + printRuntime(delayTime) + "后开始运行][间隔 " + printRuntime(intervalTime) + "][scheduledBoxSize " + scheduledMap.size() + "]");
			ChangeScheduledStatus(handleName, 0);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		res = true;
		return res;
	}

	/**
	 * 调度任务停止
	 * @param handleName
	 */
	public static void StopHandle(String handleName) {
		ScheduledExecutorService service = (ScheduledExecutorService) scheduledMap.get(handleName);
		if (service != null) {
			service.shutdownNow();
		}
		scheduledMap.put(handleName, service);
		String sql = "update tb_scheduled_conf set status=2 where identidy='"	+ handleName + "'";
		DataBaseCenter.Dao.exec(sql);
		LoggerUtil.InfoLog("[" + handleName + "][停止运行]");
	}

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

	protected static void ChangeScheduledStatus(String identidy, int status) {
		String sql = "update tb_scheduled_conf set status=" + status + " where identidy='" + identidy + "'";
		DataBaseCenter.Dao.exec(sql);
	}
	
	protected static void ChangeScheduledConf (String identidy, String conf) {
		String sql = "update tb_scheduled_conf set conf='" + conf + "' where identidy='" + identidy + "'";
		DataBaseCenter.Dao.exec(sql);
	}

	public static void main(String[] args) {
		ServiceStartInit();
	}
}