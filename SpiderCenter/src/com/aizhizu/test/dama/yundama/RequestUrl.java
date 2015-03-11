package com.aizhizu.test.dama.yundama;

/**
 * 请求url 枚举类型
 * 
 * @author ryan
 * 
 */
public enum RequestUrl {

	HTTP_API_URL("http://api.yundama.com/api.php");
	private String url;

	private RequestUrl(String url) {
		this.url = url;
	}

	public String getUrl() {
		return url;
	}

}
