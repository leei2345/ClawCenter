package com.aizhizu.core.scheduled;

import org.apache.commons.lang3.time.FastDateFormat;

import com.aizhizu.bean.MornitorEntity;
import com.aizhizu.core.BaseHandler;
import com.aizhizu.service.BaseClawer;
import com.aizhizu.service.proxy.ProxyCleanner;

/**
 * 无用代理数据库清理处理控制中心
 * @author leei
 *
 */
public class ProxyCleanHandle extends BaseHandler {
	private static String identidy = "proxy_clean";
	protected static FastDateFormat fdf = FastDateFormat.getInstance("yyyyMMdd|HH:mm");

	public ProxyCleanHandle() {
		super(identidy);
	}

	protected void StartHandle() {
		BaseClawer proxyCleanner = new ProxyCleanner();
		MornitorEntity mornitor = new MornitorEntity(identidy);
		proxyCleanner.setMornitor(mornitor);
		proxyCleanner.Implement();
		mornitor.MakeDB();
	}
	
	public static void main(String[] args) {
		BaseHandler b = new ProxyCleanHandle();
		new Thread(b).start();
		
	}
	
}