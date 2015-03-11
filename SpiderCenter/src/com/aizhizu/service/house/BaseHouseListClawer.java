package com.aizhizu.service.house;

import com.aizhizu.bean.BaseHouseEntity;
import com.aizhizu.bean.MornitorEntity;
import com.aizhizu.service.BaseClawer;
import com.aizhizu.util.ConfigUtil;
import com.aizhizu.util.CountDownLatchUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Date;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;

public abstract class BaseHouseListClawer extends BaseClawer {
	protected ConcurrentLinkedQueue<String> taskList = new ConcurrentLinkedQueue<String>();
	protected String filePath;
	protected CountDownLatchUtils listcdl;
	protected static FastDateFormat sim = FastDateFormat.getInstance("yyyyMMdd|HH:mm");
	protected static String tempStr = "序号,source_url,电话,电话来源图片url,房东名,性别,标题,出租类型,价格,城市,小区名,区域,商圈,维度经度,房型格式,楼层,朝向,面积,感言,pic";
	private static String baseUrl;
	private static String devUrl;
	
	static {
		try {
			devUrl = ConfigUtil.getString("data.push.url.base");
			baseUrl = ConfigUtil.getString("data.push.url.dev");
		} catch (Exception e) {
		}
	}
	
	
	public BaseHouseListClawer(String identidy) {
		super(identidy);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void run() {
		try {
			Implement();
		} catch (Exception e) {
			clawerLogger.info("[" + this.identidy + "][got house list fail][" + e.getMessage() + "]");
			this.listcdl.countDown();
			return;
		}
		int taskSize = this.taskList.size();
		clawerLogger.info("[" + this.identidy + "][got house list succ][task size " + taskSize + "]");
		CountDownLatchUtils cdl = new CountDownLatchUtils(taskSize);
		MornitorEntity mornitor = new MornitorEntity(identidy);
		String nowTime = sim.format(new Date());
		String[] timeArr = nowTime.split("\\|");
		String day = timeArr[0];
		String time = timeArr[1];
		mornitor.setDate(day);
		mornitor.setStartTime(time);
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
		File unmatchedCsvFile = new File(this.filePath + "/unmatched_house.csv");
		OutputStreamWriter matchedWriter = null;
		OutputStreamWriter unmatchedWriter = null;
		try {
			matchedWriter = new OutputStreamWriter(new FileOutputStream(matchedCsvFile, true), "GBK");
			matchedWriter.write(tempStr + "\n");
			matchedWriter.flush();
			
			unmatchedWriter = new OutputStreamWriter(new FileOutputStream(unmatchedCsvFile, true), "GBK");
			unmatchedWriter.write(tempStr + "\n");
			unmatchedWriter.flush();
		} catch (IOException e1) {
			e1.printStackTrace();
			if (matchedWriter != null)
				try {
					matchedWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			if (unmatchedWriter != null)
				try {
					unmatchedWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		} finally {
			if (matchedWriter != null) {
				try {
					matchedWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (unmatchedWriter != null) {
				try {
					unmatchedWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		int rowNum = 2;
		while (!this.taskList.isEmpty()) {
			String url = (String) this.taskList.poll();
			Vector<String> box = new Vector<String>();
			box.add(url);
			try {
				Class clazz = loader.loadClass(packageName	+ ".HouseDetailClawer");
				Class[] parameterTypes = { CountDownLatchUtils.class };
				Object[] params = { cdl };
				Constructor con = clazz.getConstructor(parameterTypes);
				Object instance = con.newInstance(params);
				BaseClawer clawer = (BaseClawer) instance;
				clawer.setBox(box);
				clawer.setMornitor(mornitor);
				clawer.Implement();
				Object entity = clawer.getEntity();
				if (entity != null) {
					BaseHouseEntity house = (BaseHouseEntity) entity;
					house.setNum(rowNum);
					boolean dealRes = DealWithChuzuData(house);
					if (dealRes)
						rowNum++;
				}
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
			}
		}
		try {
			cdl.await();
			mornitor.MakeDB();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			this.listcdl.countDown();
		}
	}

	private boolean DealWithChuzuData(BaseHouseEntity house) {
		boolean res = false;
		String url = house.getUrl();
		boolean matched = house.isPush();
		int rowNum = house.getNum();
		File imageFileDir = new File(this.filePath + "/" + rowNum);
		if (!imageFileDir.exists()) {
			imageFileDir.mkdirs();
		}
		OutputStreamWriter matchedwriter = null;
		OutputStreamWriter unmatchedwriter = null;
		String imageFilePath = imageFileDir.getAbsolutePath();
		File imageSourceDataFile = new File(imageFilePath + "/source.html");
		FileWriter sourceDataWriter = null;
		try {
			if (matched) {
				File matchedCsvFile = new File(this.filePath + "/house.csv");
				matchedwriter = new OutputStreamWriter(new FileOutputStream(matchedCsvFile, true), "GBK");
				matchedwriter.write(house.toString() + "\r\n");
				matchedwriter.flush();
			} else {
				File unmatchedCsvFile = new File(this.filePath + "/unmatched_house.csv");
				unmatchedwriter = new OutputStreamWriter(new FileOutputStream(unmatchedCsvFile, true), "GBK");
				unmatchedwriter.write(house.toString() + "\r\n");
				unmatchedwriter.flush();
			}
			sourceDataWriter = new FileWriter(imageSourceDataFile);
			String html = "<html><body onload=\"parent.location='" + house.getUrl() + "'\"></body></html>";
			sourceDataWriter.write(html);  
			sourceDataWriter.flush();
		} catch (IOException e) {
			e.printStackTrace();
			return res;
		} finally {
			if (matchedwriter != null) {
				try {
					matchedwriter.close();
				} catch (IOException e) {
					e.printStackTrace();
					return res;
				}
			}
			if (unmatchedwriter != null) {
				try {
					unmatchedwriter.close();
				} catch (IOException e) {
					e.printStackTrace();
					return res;
				}
			}
			if (sourceDataWriter != null) {
				try {
					sourceDataWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		res = true;
		/** 图片异步抓取 */
		ImageDownLoader downLoader = new ImageDownLoader(this.identidy, imageFilePath, url);
		downLoader.setImageUrlList(house.getImageUrlList());
		new Thread(downLoader).start();
		
		redis.pushNewsUrl(url);
		/** 数据异步推送 */
		boolean push = true;
		if (!StringUtils.isBlank(devUrl) && push) {
			DataPusher pusher = new DataPusher(house, devUrl);
			new Thread(pusher).start();
		}
		if (!StringUtils.isBlank(baseUrl) && push) {
			DataPusher pusher_back = new DataPusher(house, baseUrl);
			new Thread(pusher_back).start();
		}
		return res;
	}

	protected void init() {
	}

	public ConcurrentLinkedQueue<String> GetTaskList() {
		Implement();
		return this.taskList;
	}

	public static void main(String[] args) {
		String t = ConfigUtil.getString("chuzu.csv.first.line.temp");
		System.out.println(t);
	}
}