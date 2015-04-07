package com.aizhizu.service.house;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.aizhizu.service.Analyst;
import com.aizhizu.service.house.BaseHouseClawer;
import com.aizhizu.util.CountDownLatchUtils;


public class BaseHouseListHandler extends BaseHouseClawer {
	
	private String fileName;
	private int listThreadPoolCount = 30;
	private int pageCount = 50;
	private ExecutorService threadPool;
	private static ClassLoader loader = null;
	private static String packageName;
	
	static {
		packageName = BaseHouseListHandler.class.getPackage().getName();
		String classLoaderPath = BaseHouseListHandler.class.getClassLoader().getResource("").getPath();
		ClassLoader Floader = Thread.currentThread().getContextClassLoader();
		try {
			loader = new URLClassLoader(new URL[] { new URL("file:"+ classLoaderPath) }, Floader);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
	
	public BaseHouseListHandler(String identidy, CountDownLatchUtils cdl, String dataFilepath) {
		super(identidy);
		this.fileName = identidy.replaceFirst("web_", "");
		listThreadPoolCount = threadPoolConf.get(identidy)[0];
		pageCount = threadPoolConf.get(identidy)[2];
		threadPool = Executors.newFixedThreadPool(listThreadPoolCount);
		this.wrapperCdl = cdl;
		this.filePath = dataFilepath;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public boolean Implement() {
		ConcurrentLinkedQueue<String> taskList = new ConcurrentLinkedQueue<String>();
		taskMap.put(identidy, taskList);
		CountDownLatchUtils listCdl = new CountDownLatchUtils(pageCount);
		for (int pageIndex = 1; pageIndex <= pageCount; pageIndex++) {
			try {
				Class clazz = loader.loadClass(packageName + "." + fileName + ".HouseListClawer");
				Class[] parameterTypes = {CountDownLatchUtils.class, int.class};
				Object[] params = { listCdl, pageIndex };
				Constructor con = clazz.getConstructor(parameterTypes);
				Object instance = con.newInstance(params);
				BaseHouseClawer clawer = (BaseHouseClawer) instance;
				threadPool.execute(clawer);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		try {
			listCdl.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return true;
	}

	@Override
	protected String GetHtml() {
		return null;
	}

	@Override
	protected Map<Analyst, Object> Analysis(String paramString) {
		return null;
	}

}
