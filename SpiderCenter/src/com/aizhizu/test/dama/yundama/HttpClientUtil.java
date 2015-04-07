package com.aizhizu.test.dama.yundama;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

/**
 * http client post 请求工具类 *
 * 
 * @author ryan
 * 
 */
@SuppressWarnings("deprecation")
public class HttpClientUtil {

	/**
	 * 发送HTTP_POST请求
	 * 
	 * @see 该方法会自动关闭连接,释放资源
	 * @see 该方法会自动对<code>params</code>中的[中文][|][ ]等特殊字符进行
	 *      <code>URLEncoder.encode(string,encodeCharset)</code>
	 * @param reqURL
	 *            请求地址
	 * @param params
	 *            请求参数
	 * @param encodeCharset
	 *            编码字符集,解析响应数据时用之,其为null时默认采用UTF-8解码
	 * @param decodeCharset
	 *            解码字符集,解析响应数据时用之,其为null时默认采用UTF-8解码
	 * @return 远程主机响应正文
	 */
	@SuppressWarnings("resource")
	public static String sendPostRequest(String reqURL,
			Map<String, String> params, String encodeCharset,
			String decodeCharset) {
		String responseContent = null;
		HttpClient httpClient = new DefaultHttpClient();
		try {
		HttpPost httpPost = new HttpPost(reqURL);
		List<NameValuePair> formParams = new ArrayList<NameValuePair>(); // 创建参数队列
		for (Map.Entry<String, String> entry : params.entrySet()) {
			formParams.add(new BasicNameValuePair(entry.getKey(), entry
					.getValue()));
		}
		
			httpPost.setEntity(new UrlEncodedFormEntity(formParams,
					encodeCharset == null ? "UTF-8" : encodeCharset));

			HttpResponse response = httpClient.execute(httpPost);
			HttpEntity entity = response.getEntity();
			if (null != entity) {
				responseContent = EntityUtils.toString(entity,
						decodeCharset == null ? "UTF-8" : decodeCharset);
				EntityUtils.consume(entity);
			}
		} catch (Exception e) {
			responseContent="与[" + reqURL + "]通信过程中发生异常";
		} finally {
			httpClient.getConnectionManager().shutdown();
		}
		return responseContent;
	}

	/**
	 * 发送HTTP_POST请求
	 * 
	 * @see 该方法会自动关闭连接,释放资源
	 * @param reqURL
	 *            请求地址
	 * @param params
	 *            请求参数
	 * @param files
	 *            上传文件
	 * @param decodeCharset
	 *            解码字符集,解析响应数据时用之,其为null时默认采用UTF-8解码
	 * @return 远程主机响应正文
	 */
	@SuppressWarnings("resource")
	public static String sendPostRequest(String reqURL,
			Map<String, String> params, Map<String, File> files,
			String decodeCharset) {
		String responseContent = null;
		HttpClient httpClient = new DefaultHttpClient();
		try {
			

			HttpPost httpPost = new HttpPost(reqURL);
			MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder
					.create();

			for (Map.Entry<String, String> entry : params.entrySet()) {
				multipartEntityBuilder.addTextBody(entry.getKey(),
						entry.getValue());
			}

			for (Map.Entry<String, File> entry : files.entrySet()) {
				multipartEntityBuilder.addBinaryBody(entry.getKey(),
						entry.getValue());
			}

			HttpEntity reqentity = multipartEntityBuilder.build();
			httpPost.setEntity(reqentity);

			HttpResponse response = httpClient.execute(httpPost);
			HttpEntity entity = response.getEntity();
			if (null != entity) {
				responseContent = EntityUtils.toString(entity,
						decodeCharset == null ? "UTF-8" : decodeCharset);
				EntityUtils.consume(entity);
			}
		} catch (Exception e) {
			responseContent="与[" + reqURL + "]通信过程中发生异常";
		} finally {
			httpClient.getConnectionManager().shutdown();
		}
		return responseContent;
	}

}
