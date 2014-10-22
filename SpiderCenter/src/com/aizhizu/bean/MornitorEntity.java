package com.aizhizu.bean;

import com.aizhizu.dao.DBDataWriter;

import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 图形化业务监控实体
 * @author leei
 * 
 */
public class MornitorEntity {
	private String identidy;
	private ProgressEntity pro;
	private String startTime;
	private String date;
	private AtomicLong usedTime;
	private long avgTime;
	private float succPercent;

	public MornitorEntity(String identidy) {
		this.identidy = identidy;
		this.pro = new ProgressEntity();
	}

	public String getIdentidy() {
		return this.identidy;
	}

	public void setIdentidy(String identidy) {
		this.identidy = identidy;
	}

	public long getUsedTime() {
		long usedTime = 0;
		if (this.usedTime != null ) {
			usedTime = this.usedTime.get();
		}
		return usedTime;
	}

	public void setUsedTime(long usedTime) {
		this.usedTime = new AtomicLong(usedTime);
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public long getAvgTime() {
		return this.avgTime;
	}

	public float getSuccPercent() {
		return this.succPercent;
	}

	public String getStartTime() {
		return this.startTime;
	}

	public String getDate() {
		return this.date;
	}

	public ProgressEntity getProgress() {
		return this.pro;
	}

	public void MakeDB() {
		this.pro.MakeDB();
	}

	public class ProgressEntity {
		private ConcurrentHashMap<Boolean, Integer> succAndFailCountMap = null;

		public ProgressEntity() {
			this.succAndFailCountMap = new ConcurrentHashMap<Boolean, Integer>();
		}

		public void setDate(String date) {
			MornitorEntity.this.setDate(date);
		}

		public void setStartTime(String startTime) {
			MornitorEntity.this.setStartTime(startTime);
		}

		public void setUsedTime(long usedTime) {
			MornitorEntity.this.setUsedTime(usedTime);
		}

		public synchronized void Count(boolean status, int unit) {
			if (this.succAndFailCountMap.get(Boolean.valueOf(status)) == null) {
				this.succAndFailCountMap.put(Boolean.valueOf(status),
						Integer.valueOf(0));
			}
			int succCount = ((Integer) this.succAndFailCountMap.get(Boolean
					.valueOf(status))).intValue();
			succCount += unit;
			this.succAndFailCountMap.put(Boolean.valueOf(status),
					Integer.valueOf(succCount));
		}

		public void Count(boolean status) {
			Count(status, 1);
		}

		public synchronized void AddTime(long time) {
			if (MornitorEntity.this.usedTime == null) {
				MornitorEntity.this.usedTime = new AtomicLong(0L);
			}
			MornitorEntity.this.usedTime.addAndGet(time);
		}

		public void MakeDB() {
			int succCount = 0;
			if (this.succAndFailCountMap.get(Boolean.valueOf(true)) != null) {
				succCount = ((Integer) this.succAndFailCountMap.get(Boolean
						.valueOf(true))).intValue();
			}
			int failCount = 0;
			if (this.succAndFailCountMap.get(Boolean.valueOf(false)) != null) {
				failCount = ((Integer) this.succAndFailCountMap.get(Boolean
						.valueOf(false))).intValue();
			}
			int holeCount = succCount + failCount;
			if (holeCount != 0)
				MornitorEntity.this.avgTime = (MornitorEntity.this.usedTime
						.get() / holeCount);
			else {
				MornitorEntity.this.avgTime = 0L;
			}
			BigDecimal succBig = new BigDecimal(succCount);
			BigDecimal holeBig = new BigDecimal(holeCount);
			if (holeCount != 0)
				MornitorEntity.this.succPercent = succBig.divide(holeBig, 2, 4)
						.floatValue();
			else {
				MornitorEntity.this.succPercent = 0.0F;
			}
			String sql = "insert into tb_clawer_mornitor(identidy,date,start_time,avg_time,succ_percent) values (:identidy,:date,:startTime,:avgTime,:succPercent)";
			DBDataWriter writer = new DBDataWriter(sql);
			writer.writeSingle(MornitorEntity.this);
		}
	}
}