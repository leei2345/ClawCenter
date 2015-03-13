package com.aizhizu.service.house;

import com.aizhizu.bean.BaseHouseEntity;
import com.aizhizu.bean.HouseChuzuEntity;
import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("deprecation")
public class DataPusher implements Runnable {
	private HouseChuzuEntity house;
	private static CloseableHttpClient client = new DefaultHttpClient();
	private static RequestConfig.Builder config = RequestConfig.custom();
	private static HttpClientBuilder clientBuilder = HttpClientBuilder.create();
	private static final Logger logger = LoggerFactory.getLogger("ClawerLogger");
	private String url;

	static {
		config.setAuthenticationEnabled(true);
		config.setConnectTimeout(30000);
		config.setSocketTimeout(30000);
		clientBuilder = HttpClientBuilder.create();
		clientBuilder.setMaxConnTotal(100);
		clientBuilder.setMaxConnPerRoute(500);
		client = clientBuilder.setDefaultRequestConfig(config.build()).build();
	}
	
	public DataPusher (BaseHouseEntity house, String url) {
		this.house = ((HouseChuzuEntity) house);
		this.url = url;
	}

	public void run() {
		StringBuilder bodyBuilder = new StringBuilder();
		String phoneNum = this.house.getPhone();
		if (phoneNum.contains("**")) {
			return;
		}
		bodyBuilder.append("phone=" + this.house.getPhone());
		bodyBuilder.append("&nickName=" + this.house.getLandlord());
		bodyBuilder.append("&gender=" + this.house.getGender());
		bodyBuilder.append("&htype=" + this.house.getRentalType());
		bodyBuilder.append("&title=" + this.house.getTitle());
		bodyBuilder.append("&rent=" + this.house.getPrice());
		bodyBuilder.append("&city=" + this.house.getCity());
		bodyBuilder.append("&town=" + this.house.getArea());
		bodyBuilder.append("&district=" + this.house.getCircle());
		bodyBuilder.append("&area=" + this.house.getDistrict());
		bodyBuilder.append("&location=" + this.house.getY() + "," + this.house.getX());
		bodyBuilder.append("&roomcount=" + this.house.getFormat());
		bodyBuilder.append("&floor=" + this.house.getFloor());
		bodyBuilder.append("&orientation=" + this.house.getFace());
		bodyBuilder.append("&size=" + this.house.getAcreage());
		bodyBuilder.append("&describe=" + this.house.getWord());
		bodyBuilder.append("&imagePhone=" + this.house.getEncodePhoneImageUrl());
		String imageUrl = "";
		Set<String> imageUrlList = this.house.getImageUrlList();
		for (String url : imageUrlList) {
			imageUrl += url + ";";
		}
		bodyBuilder.append("&pic=" + imageUrl);
		bodyBuilder.append("&video=");
		bodyBuilder.append("&titledeel=");
		bodyBuilder.append("&linkurl=" + this.house.getUrl());
		bodyBuilder.append("&linkname=" + this.house.getLineName());
		String body = bodyBuilder.toString();
		String	response = HttpThings(body);
		try {
			JSONObject respObject = JSONObject.parseObject(response);
			JSONObject statusObject = respObject.getJSONObject("status");
			int code = statusObject.getIntValue("code");
			String mes = statusObject.getString("msg");
			if (code == 200)
				logger.info("[数据推送成功][source_url=" + this.house.getUrl() + "][target=" + url + "]");
			else
				logger.info("[数据推送失败][source_url=" + this.house.getUrl()+ "][mes=" + mes + "][target=" + url + "]");
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("[数据推送错误][source_url=" + this.house.getUrl() + "][target=" + url + "][response=" + response + "]");
		}
	}
	
	public String HttpThings(String body) {
		int postStatus = 400;
		String html = "";
		String exception = "";
		HttpPost post = new HttpPost();
		try {
			URI uri = new URI(url);
			post.setURI(uri);
			List<NameValuePair> nameValueList = new ArrayList<NameValuePair>();
			String[] params = body.split("&");
			for (String param : params) {
				String[] nameValue = param.split("\\=");
				nameValueList.add(new BasicNameValuePair(nameValue[0],
						nameValue.length == 1 ? "" : nameValue[1]));
			}
			UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(
					nameValueList, Consts.UTF_8);
			post.setEntity(urlEncodedFormEntity);
			Object response = client.execute(post);
			postStatus = ((HttpResponse) response).getStatusLine()
					.getStatusCode();
			HttpEntity entity = ((HttpResponse) response).getEntity();
			html = EntityUtils.toString(entity);
		} catch (SocketTimeoutException e) {
			exception = "SocketTimeoutException";
		} catch (ConnectTimeoutException e) {
			exception = "ConnectTimeoutException";
		} catch (UnknownHostException e) {
			exception = "UnknownHostException";
		} catch (IOException e) {
			exception = "IOException";
		} catch (URISyntaxException e) {
			exception = "URISyntaxException";
		} catch (Exception e) {
			exception = e.getMessage();
		} finally {
			post.abort();
			post.releaseConnection();
		}
		if (postStatus != 200) {
			logger.info("[接口数据推送失败][source_url=" + this.house.getUrl()	+ "][excption=" + exception + "]");
		}
		return (String) html;
	}
}