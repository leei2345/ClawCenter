package com.aizhizu.dao;

import java.util.ArrayList;
import java.util.List;

import com.aizhizu.util.ConfigUtil;



public abstract class DataWriter {

	private boolean isFirst = true;
	private int batchCount;

	@SuppressWarnings("rawtypes")
	protected abstract void batchWrite(List l);

	protected abstract void singleWrite(Object o);

	protected abstract void open();

	public abstract void close();

	private void before() {
		// 首次打开
		if (isFirst == true) {
			open();

			batchCount = ConfigUtil.getInt("batchReadNumber");// 批量
			isFirst = false;
		}
	}

	private void after() {
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void writeList(List l) {
		before();

		// 无内容
		if (l == null || l.size() == 0) {
		} else {
			// 批量入库
			while (true) {
				List lt = new ArrayList();
				int len = l.size();
				if (len == 0)
					break;

				inner: for (int i = 0; i < len; i++) {
					if (i <= batchCount) {
						lt.add(l.get(0));
						l.remove(0);
					} else {
						break inner;
					}
				}
				batchWrite(lt);
			}

		}

		after();

		return;
	}

	public void writeSingle(Object para) {
		before();

		// 无内容
		if (para == null) {
		} else {
			// 单条
			singleWrite(para);
		}

		after();

		return;
	}

}
