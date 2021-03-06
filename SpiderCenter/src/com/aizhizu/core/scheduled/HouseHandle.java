package com.aizhizu.core.scheduled;

import com.aizhizu.core.BaseHandler;
import com.aizhizu.service.house.BaseHouseClawer;
import com.aizhizu.service.house.BaseHouseListHandler;
import com.aizhizu.util.ConfigUtil;
import com.aizhizu.util.CountDownLatchUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.time.FastDateFormat;

/**
 * 房源数据抓取并解析处理控制中心
 * @author leei
 *
 */
public class HouseHandle extends BaseHandler {
	/** 线程池子 */
	private ExecutorService threadPool = null;
	/** 文件写入路径 */
	private static String dataFilePath = null;
	private static FastDateFormat datePattern = FastDateFormat.getInstance("yyyyMMddHH");
	private static String identidy = "house_claw";

	static {
		dataFilePath = ConfigUtil.getString("data.file.path");
	}

	public HouseHandle() {
		super(identidy);
	}

	protected void StartHandle() {
		if (this.threadPool == null) {
			this.threadPool = Executors.newCachedThreadPool();
		}
		Calendar cal = Calendar.getInstance();
		String dateFileName = datePattern.format(cal);
		File dataFileDir = new File(dataFilePath + dateFileName);
		if (!dataFileDir.exists()) {
			dataFileDir.mkdirs();
		}
		String dataFilePath = dataFileDir.getAbsolutePath();
//		String classLoaderPath = getClass().getClassLoader().getResource("").getPath();
//		ClassLoader Floader = Thread.currentThread().getContextClassLoader();
//		ClassLoader loader = null;
//		try {
//			loader = new URLClassLoader(new URL[] { new URL("file:"+ classLoaderPath) }, Floader);
//		} catch (MalformedURLException e) {
//			e.printStackTrace();
//		}
//		Package p = BaseHouseClawer.class.getPackage();
//		String packageName = p.getName();
		String classPath = BaseHouseClawer.class.getResource("").getPath();
		File classPathDir = new File(classPath);
		File[] files = classPathDir.listFiles();
		List<File> houseWrapperFileList = new ArrayList<File>();
		for (File file : files) {
			if (file.isDirectory()) {
				houseWrapperFileList.add(file);
			}
		}
		int houseWrapperFileListSize = houseWrapperFileList.size();
		CountDownLatchUtils wrapperCdl = new CountDownLatchUtils(houseWrapperFileListSize);
		List<String> list = new ArrayList<String>();
//		list.add("soufang");
//		list.add("wuba");
//		list.add("anjuke");
		for (File file : houseWrapperFileList) {
			String dirName = file.getName();
			String identidy = "web_" + dirName;
			if (list.contains(dirName)) {
				System.out.println(dirName);
				continue;
			}
//			try {
//				Class clazz = loader.loadClass(packageName + ".BaseHouseListHandler");
//				Class[] parameterTypes = { String.class, String.class, CountDownLatchUtils.class};
//				Object[] params = { identidy, dataFilePath, listCdl };
//				Constructor con = clazz.getConstructor(parameterTypes);
//				Object instance = con.newInstance(params);
//				BaseHouseClawer clawer = (BaseHouseClawer) instance;
				BaseHouseClawer clawer = new BaseHouseListHandler(identidy, wrapperCdl, dataFilePath);
				this.threadPool.execute(clawer);
//			} catch (ClassNotFoundException e) {
//				e.printStackTrace();
//			} catch (NoSuchMethodException e) {
//				e.printStackTrace();
//			} catch (SecurityException e) {
//				e.printStackTrace();
//			} catch (InstantiationException e) {
//				e.printStackTrace();
//			} catch (IllegalAccessException e) {
//				e.printStackTrace();
//			} catch (IllegalArgumentException e) {
//				e.printStackTrace();
//			} catch (InvocationTargetException e) {
//				e.printStackTrace();
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
		}
		try {
			wrapperCdl.await(7160000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		this.threadPool.shutdownNow();
		this.threadPool = null;
	}

	public static void main(String[] args) {
		BaseHandler r = new HouseHandle();
		new Thread(r).start();
	}
}