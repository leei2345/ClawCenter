package com.aizhizu.dao;

import java.util.ArrayList;
import java.util.List;

import com.aizhizu.util.ConfigUtil;



public abstract class DataReader {

	private boolean isFirst = true;
	private boolean isFinished = false;

	private int batchCount;

	public boolean isFinished() {
		return isFinished;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List readAll() {
		before();

		// 批量读
		List dataList = allRead();

		List l = new ArrayList();
		l.addAll(dataList);

		after();

		isFinished = true;

		return l;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List readList() {
		// 已经空了
		if (isFinished == true)
			return null;

		before();

		// 批量读
		List dataList = batchRead(batchCount);

		// 无内容
		if (dataList == null || dataList.size() == 0) {
			isFinished = true;
		}

		List l = new ArrayList();
		l.addAll(dataList);

		after();

		return l;
	}

	public Object readSingle() {

		before();

		Object o = singleRead();

		// 无内容
		if (o == null) {
			isFinished = true;
		}

		after();
		return o;
	}

	private void before() {
		// 首次打开
		if (isFirst == true) {
			open();

			batchCount = ConfigUtil.getInt("batchReadNumber");// 批量
			isFirst = false;
		}
	}

	private void after() {
		// 已经空了
		if (isFinished == true) {
			close();
		}
	}

	@SuppressWarnings("rawtypes")
	protected abstract List batchRead(int batchCount);

	@SuppressWarnings("rawtypes")
	protected abstract List allRead();

	protected abstract Object singleRead();

	protected abstract void open();

	protected abstract void close();

}
