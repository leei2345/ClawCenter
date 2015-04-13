package com.aizhizu.service;

import com.aizhizu.bean.MornitorEntity;
import com.aizhizu.dao.Redis;
import com.aizhizu.http.HttpMethod;
import com.aizhizu.http.Method;
import com.aizhizu.util.CountDownLatchUtils;
import com.aizhizu.util.LoggerUtil;
import com.alibaba.fastjson.JSONException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.http.message.BasicHeader;

/**
 * 数据抓取的核心抽象工厂
 * @author leei
 *
 */
public abstract class BaseClawer implements Runnable {
	/** 计数器 */
	protected CountDownLatchUtils cdl = new CountDownLatchUtils(1);
	/** 存放抓取请求的参数容器 */
	protected Vector<String> box = null;
	/** 抓取身份证，用于数据库存储标示 */
	public String identidy;
	/** 解析返回的实体数据 */
	protected Object entity = null;
	/** 显式进度 */
	private MornitorEntity mornitor = null;
	/** 解析结果存放容器 */
	protected Map<Analyst, Object> analystResult = new HashMap<Analyst, Object>();
	/** 公用redis */
	protected static Redis redis = null;

	static {
		redis = Redis.getInstance();
		redis.refreshPlotMap();
		LoggerUtil.InfoLog("init plot data from redis!");
	}

	public void setMornitor(MornitorEntity mornitor) {
		this.mornitor = mornitor;
	}

	public Object getEntity() {
		return this.entity;
	}

	public BaseClawer(String mark) {
		this.identidy = mark;
	}

	public void setBox(Vector<String> box) {
		this.box = box;
	}
	
	/** 抓取参数初始化 */
	protected abstract void init();

	/** 发起http请求进行抓取 */
	protected abstract String GetHtml();

	/** 进行数据解析 */
	protected abstract Map<Analyst, Object> Analysis(String paramString);

	/** 完成工作 */
	public boolean Implement() {
		boolean result = false;
		String html = "";
		try {
			StopWatch watch = new StopWatch();
			watch.start();
			init();
			html = GetHtml();
			this.analystResult = Analysis(html);
			watch.split();
			long usedTime = watch.getSplitTime();
			
			watch.stop();
			String info = (String) this.analystResult.get(Analyst.Info);
			if (info.contains("succ")) {
				result = true;
			}
			if (this.analystResult.containsKey(Analyst.Entity)) {
				this.entity = this.analystResult.get(Analyst.Entity);
			}
			if (this.mornitor != null) {
				Object succCountObj = this.analystResult.get(Analyst.SuccCount);
				int succCount = 0;
				if (succCountObj != null)  {
					succCount = ((Integer) succCountObj).intValue();
				}
				Object failCountObj = this.analystResult.get(Analyst.FailCount);
				int failCount = 0;
				if (failCountObj != null) {
					failCount = ((Integer) failCountObj).intValue();
				}
				this.mornitor.Count(true, succCount);
				this.mornitor.Count(false, failCount);
				
				this.mornitor.AddTime(usedTime);
			} else {
				LoggerUtil.ClawerLog(this.identidy , "[" + this.identidy + "]" + "[请注入Mornitor]");
			}
			if ((this.box == null) || (this.box.size() == 0)) {
				this.box = new Vector<String>();
				this.box.add("work");
			}
			LoggerUtil.ClawerLog(this.identidy, "[" + this.identidy + "][" + Progress() + "][" + info + "][" + (String) this.box.get(0) + "]");
		} catch (JSONException je) {
			je.printStackTrace();
			result = false;
			LoggerUtil.ClawerLog(this.identidy, "[" + this.identidy + "][" + Progress() + "][fail][" + (String) this.box.get(0) + "]");
		} catch (Exception e) {
			e.printStackTrace();
			result = false;
			LoggerUtil.ClawerLog(this.identidy, "[" + this.identidy + "][" + Progress() + "][fail][" + (String) this.box.get(0) + "]");
		}
		this.cdl.countDown();
		return result;
	}

	/**
	 * 展示进度
	 * @return
	 */
	public String Progress() {
		int count = this.cdl.getAmount();
		long plan = this.cdl.getCount();
		int schedu = Integer.parseInt(String.valueOf(plan)) - 1;
		return String.valueOf(count - schedu) + " / " + count;
	}

	public BasicHeader[] ToArray(List<BasicHeader> list) {
		int size = list.size();
		BasicHeader[] headers = new BasicHeader[size];
		for (int index = 0; index < size; index++) {
			headers[index] = ((BasicHeader) list.get(index));
		}
		return headers;
	}

	public File GetImageFile (String identidy, String url) {
		String tempName = DigSign.getMD5(url, "UTF-8");
		HttpMethod me = new HttpMethod(identidy);
		me.AddHeader(Method.Get, "User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:32.0) Gecko/20100101 Firefox/32.0");
		me.AddHeader(Method.Get, "Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		me.AddHeader(Method.Get, "Accept-Language", "zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3");
		me.AddHeader(Method.Get, "Accept-Encoding", "gzip, deflate");
		byte[][] imageByteArr = me.GetImageByteArr(url);
		if (imageByteArr == null) {
			return  null;
		}
		String imageType = new String(imageByteArr[1]);
		if (StringUtils.isBlank(imageType)) {
			return  null;
		}
		byte[] imageData = imageByteArr[0];
		File imageFile = new File(tempName + "." + imageType);
		FileOutputStream fileStream = null;
		try {
			imageFile.createNewFile();
			fileStream = new FileOutputStream(imageFile);
			fileStream.write(imageData);
			fileStream.flush();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fileStream != null) {
				try {
					fileStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return imageFile;
	}
	
	
	
	public static void main(String[] args) {
		BigDecimal succBig = new BigDecimal(100);
		BigDecimal holeBig = new BigDecimal(10002);
		float succPercent = succBig.divide(holeBig, 2, 4).floatValue();
		System.out.println(succPercent);
	}
}