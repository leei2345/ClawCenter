package com.aizhizu.core.scheduled;

import com.aizhizu.bean.MornitorEntity;
import com.aizhizu.core.BaseHandler;
import com.aizhizu.service.BaseClawer;
import com.aizhizu.service.proxy.ProxyClawer;

import org.apache.commons.lang3.time.FastDateFormat;

/**
 * 代理抓取处理控制中心
 * @author leei
 *
 */
public class ProxyClawHandle extends BaseHandler {
	private static String identidy = "proxy_claw";
	protected static FastDateFormat fdf = FastDateFormat.getInstance("yyyyMMdd|HH:mm");

	public ProxyClawHandle() {
		super(identidy);
	}

	protected void StartHandle() {
		BaseClawer proxyClawer = new ProxyClawer();
		MornitorEntity mornitor = new MornitorEntity(identidy);
		proxyClawer.setMornitor(mornitor);
		proxyClawer.Implement();
		mornitor.MakeDB();
	}
	
	public static void main(String[] args) {
		BaseHandler b = new ProxyClawHandle();
		new Thread(b).start();
	}
	
}