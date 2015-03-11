package com.aizhizu.util;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class CountDownLatchUtils {
	private int amount;
	private CountDownLatch cdl;

	public CountDownLatchUtils(int count) {
		this.amount = count;
		this.cdl = new CountDownLatch(count);
	}

	public void countDown() {
		this.cdl.countDown();
	}

	public void await() throws InterruptedException {
		this.cdl.await();
	}
	
	public void await(long time) throws InterruptedException {
		this.cdl.await(time, TimeUnit.MILLISECONDS);
	}

	public long getCount() {
		return this.cdl.getCount();
	}

	public int getAmount() {
		return this.amount;
	}
}