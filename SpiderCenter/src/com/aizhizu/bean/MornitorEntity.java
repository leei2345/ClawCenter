package com.aizhizu.bean;

import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.time.FastDateFormat;
import org.jdiy.core.Rs;

import com.aizhizu.dao.DataBaseCenter;
import com.aizhizu.util.LoggerUtil;

/**
 * 用于监控的实体类
 * @author leeizhang
 *
 */
public class MornitorEntity {

	private static FastDateFormat sim = FastDateFormat.getInstance("yyyyMMdd;HH:mm");
	private String host;
	/** 调度任务的标识 */
	public String identify;
	/** 起始时间 */
	private String startTime;
	/** 日期 */
	private String date;
	/** 耗时 */
	private AtomicLong usedTime;
	/** 存储成功和失败的数量 true:成功 false:失败*/
	private ConcurrentHashMap<Boolean, Integer> succAndFailCountMap = new ConcurrentHashMap<Boolean, Integer>();
	
		
	public MornitorEntity (String identify) {
		this.identify = identify;
		String time = sim.format(new Date());
		String[] timeArr = time.split(";");
		this.date = timeArr[0];
		this.startTime = timeArr[1];
		try {
			InetAddress add = InetAddress.getLocalHost();
			host = add.getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
		
		/**
		 * 收集成功和失败的数量
		 * @param identity
		 * @param status
		 */
		public synchronized void Count (boolean status, int unit) {
			if (succAndFailCountMap.get(status) == null) {
				succAndFailCountMap.put(status, 0);
			}
			int succCount = succAndFailCountMap.get(status);
			succCount += unit;
			succAndFailCountMap.put(status, succCount);
		}
		
		public void Count (boolean status) {
			Count(status, 1);
		}
		
		/**
		 * 收集抓取并解析的耗时
		 * @param time
		 */
		public synchronized void AddTime (long time) {
			if (usedTime == null) {
				usedTime = new AtomicLong(0l);
			}
			usedTime.addAndGet(time);
		}
		
		//显式调用
		public void MakeDB () {
			int succCount = 0;
			if (succAndFailCountMap.get(true) != null) {
				succCount = succAndFailCountMap.get(true);
			}
			int failCount = 0;
			if (succAndFailCountMap.get(false) != null) {
				failCount = succAndFailCountMap.get(false);
			}
			if (MornitorEntity.this.identify != null) {
				String sql = "select id from tb_mornitor where identidy='" + identify + "' and date='" + date + "' and start_time='" + startTime + "' and hostname='" + host + "'";
				Rs rs = DataBaseCenter.Dao.rs(sql);
				int avgTime = 0;
				int holeCount = (succCount + failCount);
				if (holeCount > 0) {
					avgTime = new BigDecimal(usedTime.get()).divide(new BigDecimal(holeCount), 0, BigDecimal.ROUND_HALF_UP).intValue();
				}
				if (rs.isNull()) {
					sql = "insert into tb_mornitor (hostname,identidy,date,start_time,avg_time,succ_count,fail_count) values ('" + host + "','" + identify + "','" + date + "','" + startTime + "'," + avgTime + "," + succCount + "," + failCount + ")";
					int count = DataBaseCenter.Dao.exec(sql);
					if (count > 0) {
						LoggerUtil.ClawerLog("[Mornitor][" + identify + "][Insert DB][Succ]");
					}
				}
			}
		}
	
	public static void main(String[] args) {
		ConcurrentHashMap<Boolean, Integer> succAndFailCountMap = new ConcurrentHashMap<Boolean, Integer>();
//		succAndFailCountMap.put(false, 1);
		int succ = succAndFailCountMap.get(true);
		System.out.println(succ);
		
	}
}
