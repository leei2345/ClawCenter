package com.aizhizu.http;

import java.util.TimerTask;

import org.apache.http.client.methods.HttpRequestBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * http客户端实体
 * @author leei
 *
 */
public class AbordConnectTask extends TimerTask {
	
	private static final Logger logger = LoggerFactory.getLogger("HttpLogger");
	private HttpRequestBase method = null;

	public AbordConnectTask (HttpRequestBase method) {
		this.method = method;
	}
	
	@Override
	public void run() {
		try {
			if (!this.method.isAborted()) {
				this.method.abort();
				logger.info("[Http Connection Aborded]");
			}
		} catch (Exception e) {
			logger.info("[Http Connection Aborded Fail][" + e.getCause() + "]");
		}
	}
	
	
	public static void main(String[] args) {
		HttpMethod m = new HttpMethod("test");
		String html = m.GetHtml("http://www.baidu.com", HttpResponseConfig.ResponseAsStream);
		System.out.println(html);
		HttpMethod hm = new HttpMethod("test");
		html = hm.GetHtml("http://www.google.com", HttpResponseConfig.ResponseAsStream);
		System.out.println(html);
	}
	
	
}
