package com.aizhizu.core.scheduled;

import com.aizhizu.bean.MornitorEntity;
import com.aizhizu.core.BaseHandler;
import com.aizhizu.service.BaseClawer;
import com.aizhizu.service.proxy.ProxyCleanner;

import java.util.Date;

import org.apache.commons.lang3.time.FastDateFormat;

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
		String date = fdf.format(new Date(System.currentTimeMillis()));
		String[] dateArr = date.split("\\|");
		MornitorEntity mornitor = new MornitorEntity(identidy);
		mornitor.setDate(dateArr[0]);
		mornitor.setStartTime(dateArr[1]);
		proxyCleanner.setMornitor(mornitor);
		proxyCleanner.Implement();
		proxyCleanner.WriteMornitorDB();
	}
	
	public static void main(String[] args) {
		BaseHandler b = new ProxyCleanHandle();
		new Thread(b).start();
		
	}
	
}