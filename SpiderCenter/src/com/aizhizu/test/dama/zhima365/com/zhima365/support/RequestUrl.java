package com.aizhizu.test.dama.zhima365.com.zhima365.support;

/**
 * 请求url 枚举类型
 * 
 * @author ryan
 * 
 */
public enum RequestUrl {

	HTTP_API_URL("http://ff.zhima365.com/zmdemo_php/http_api.php");
	private String url;

	private RequestUrl(String url) {
		this.url = url;
	}

	public String getUrl() {
		return url;
	}

}
