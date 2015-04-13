package com.aizhizu.service.house;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.commons.lang3.time.StopWatch;

import com.aizhizu.bean.FileWriterEntity;
import com.aizhizu.bean.MornitorEntity;
import com.aizhizu.service.BaseClawer;
import com.aizhizu.service.house.ganji.LoginScheduled;
import com.aizhizu.service.house.ganji.UserCenter;
import com.aizhizu.util.ConfigUtil;
import com.aizhizu.util.CountDownLatchUtils;
import com.aizhizu.util.LoggerUtil;

public abstract class BaseHouseClawer extends BaseClawer {
	
	protected static ConcurrentHashMap<String, ConcurrentLinkedQueue<String>> taskMap = new ConcurrentHashMap<String, ConcurrentLinkedQueue<String>>();
	protected String filePath;
	protected CountDownLatchUtils wrapperCdl;
	protected static FastDateFormat sim = FastDateFormat.getInstance("yyyyMMdd|HH:mm");
	protected static String tempStr = "序号,source_url,电话,电话来源图片url,房东名,性别,标题,出租类型,价格,城市,小区名,区域,商圈,维度经度,房型格式,楼层,朝向,面积,感言,pic";
	protected static String baseUrl;
	protected static String devUrl;
	private static final long TimeOut = 7000000;
	private int threadCount = 5;
	protected static Map<String, int[]> threadPoolConf = new HashMap<String, int[]>();
	
	static {
		try {
			devUrl = ConfigUtil.getString("data.push.url.base");
			baseUrl = ConfigUtil.getString("data.push.url.dev");
			String clawerKey = ConfigUtil.getString("clawer_key");
			String[] clawerArr = clawerKey.split(";");
			for (String key : clawerArr) {
				String keyName = key + "_thread";
				String value = ConfigUtil.getString(keyName);
				String[] valueArr = value.split(";");
				int listPool = Integer.parseInt(valueArr[0]);
				int detailPool = Integer.parseInt(valueArr[1]);
				int pageCount = Integer.parseInt(valueArr[2]);
				threadPoolConf.put(key, new int[]{listPool, detailPool, pageCount});
			}
		} catch (Exception e) {
		}
	}
	
	public BaseHouseClawer(String identidy) {
		super(identidy);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void run() {
		/** 赶集登陆定时器 */
		ScheduledExecutorService service = null;
		if (StringUtils.equals(this.identidy, "web_ganji")) {
			service = Executors.newSingleThreadScheduledExecutor();
			LoginScheduled login = new LoginScheduled();
			service.scheduleAtFixedRate(login, 10000, 60000, TimeUnit.MILLISECONDS);
		}
		try {
			Implement();
		} catch (Exception e) {
			LoggerUtil.ClawerLog(identidy, "[" + this.identidy + "][got house list fail][" + e.getMessage() + "]");
			return;
		}
		ConcurrentLinkedQueue<String> taskList = taskMap.get(this.identidy);
		int taskSize = taskList.size();
		threadCount = threadPoolConf.get(this.identidy)[1];
		ExecutorService threadPool = Executors.newFixedThreadPool(threadCount);
		LoggerUtil.ClawerLog(this.identidy, "[" + this.identidy + "][got house list succ][task size " + taskSize + "]");
		CountDownLatchUtils cdl = new CountDownLatchUtils(taskSize);
		MornitorEntity mornitor = new MornitorEntity(identidy);
		String classLoaderPath = getClass().getClassLoader().getResource("").getPath();
		ClassLoader Floader = Thread.currentThread().getContextClassLoader();
		Package p = getClass().getPackage();
		String packageName = p.getName();
		ClassLoader loader = null;
		try {
			loader = new URLClassLoader(new URL[] { new URL("file:" + classLoaderPath) }, Floader);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		this.filePath = (this.filePath + "/" + this.identidy);
		File detailDataClawDir = new File(this.filePath);
		if (!detailDataClawDir.exists()) {
			detailDataClawDir.mkdirs();
		}
		File matchedCsvFile = new File(this.filePath + "/house.csv");
		OutputStreamWriter matchedWriter = null;
		try {
			matchedWriter = new OutputStreamWriter(new FileOutputStream(matchedCsvFile, true), "GBK");
			matchedWriter.write(tempStr + "\n");
			matchedWriter.flush();
		} catch (IOException e1) {
			e1.printStackTrace();
			if (matchedWriter != null)
				try {
					matchedWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		FileWriterEntity fileWriter = new FileWriterEntity(this.filePath, matchedWriter);
		AtomicInteger rowNum = new AtomicInteger(2);
		String sourceName = this.identidy.replace("web_", "");
		while (!taskList.isEmpty()) {
			String url = (String) taskList.poll();
			Vector<String> box = new Vector<String>();
			box.add(url);
			try {
				Class clazz = loader.loadClass(packageName + "." + sourceName	+ ".HouseDetailClawer");
				Class[] parameterTypes = { CountDownLatchUtils.class };
				Object[] params = { cdl };
				Constructor con = clazz.getConstructor(parameterTypes);
				Object instance = con.newInstance(params);
				BaseHouseDetailHandler clawer = (BaseHouseDetailHandler) instance;
				clawer.setBox(box);
				clawer.setMornitor(mornitor);
				int lineNum = rowNum.get();
				clawer.setLineNum(lineNum);
				clawer.setWriter(fileWriter);
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
			} finally {
				rowNum.getAndAdd(1);
			}
		}
		try {
			cdl.await(TimeOut);
			Thread.sleep(10000);
			threadPool.shutdownNow();
			threadPool = null;
			mornitor.MakeDB();
			if (StringUtils.equals(this.identidy, "web_ganji")) {
				service.shutdownNow();
				service = null;
				UserCenter.ClearLoginUserList();
			}
		}  catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				matchedWriter.close();
				wrapperCdl.countDown();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	protected void init() {
	}

	public static void ClearTaskList() {
		taskMap.clear();
	}

	public static void main(String[] args) {
		StopWatch watch = new StopWatch();
//		watch.start();
		for (int i = 0; i < 10; i++) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println(watch.getTime());
		}
	}
}