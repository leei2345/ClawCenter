package com.aizhizu.http;

import com.aizhizu.service.proxy.ProxyChecker;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("deprecation")
/**
 * 数据抓取核心，http请求发起端
 * @author leei
 */
public class HttpMethod {
	public CloseableHttpClient client = new DefaultHttpClient();
	private static final Logger logger = LoggerFactory.getLogger("ClawerLogger");
	private HttpGet get = null;
	private HttpPost post = null;
	private static int retryCount = 3;
	private static final String DEFAULTCHARACTER = "UTF-8";
	private RequestConfig.Builder config = RequestConfig.custom();
	private HttpClientBuilder clientBuilder = HttpClientBuilder.create();
	private String identidy;
	private static ConcurrentHashMap<String, List<HttpHost>> proxyMap = null;
	private static long timeStemp = 0L;
	private static final long INTERVALTIME = 43200000L;
	private String getHtml = null;
	private String getException = null;
	private int getStatus = 0;
	private String postHtml = "";
	private String postException = "";
	private int postStatus = 0;

	public HttpMethod() {
		this(null);
	}

	public HttpMethod(String identidy) {
		this.identidy = identidy;
		this.config.setAuthenticationEnabled(true);
		this.config.setConnectTimeout(30000);
		this.config.setSocketTimeout(30000);
		this.clientBuilder = HttpClientBuilder.create();
		this.clientBuilder.setMaxConnTotal(100);
		this.clientBuilder.setMaxConnPerRoute(500);
		this.client = this.clientBuilder.setDefaultRequestConfig(
				this.config.build()).build();

		if (identidy != null)
			initProxyMap();
	}

	public static String initProxyMap() {
		long nowTimeStemp = System.currentTimeMillis();
		if (nowTimeStemp - timeStemp > INTERVALTIME) {
			proxyMap = ProxyChecker.initProxyMap();
			if (proxyMap == null) {
				proxyMap = new ConcurrentHashMap<String, List<HttpHost>>();
			}
			logger.info("[HttpThings Proxy Boxs Init Complment]");
			timeStemp = nowTimeStemp;
		}
		return "[HttpThings Proxy Boxs Init Complment]";
	}

	public void SetConnectionTimeOutThreshold(int timeOut) {
		this.config.setConnectTimeout(timeOut);
		this.config.setSocketTimeout(timeOut);
		this.client = this.clientBuilder.setDefaultRequestConfig(
				this.config.build()).build();
	}

	public void AddHeader(Method method, String name, String value) {
		if (method.equals(Method.Get)) {
			if (this.get == null) {
				this.get = new HttpGet();
			}
			this.get.addHeader(name, value);
		} else {
			if (this.post == null) {
				this.post = new HttpPost();
			}
			this.post.addHeader(name, value);
		}
	}

	private String GetHtml(String url, HttpResponseConfig httpResponseConfig,
			int retryIndex) {
		boolean responseAsStream = false;
		boolean getLocation = false;
		if (httpResponseConfig == null) {
			getLocation = true;
			RequestConfig.Builder builder = this.config;
			builder.setRelativeRedirectsAllowed(false);
			builder.setCircularRedirectsAllowed(false);
			builder.setRedirectsEnabled(false);
			this.client = this.clientBuilder.setDefaultRequestConfig(
					builder.build()).build();
		} else {
			responseAsStream = httpResponseConfig.isYesOrNo();
		}
		HttpHost proxy = null;
		if (!StringUtils.isBlank(this.identidy)) {
			List<HttpHost> proxySet =  proxyMap.get(this.identidy);
			if (proxySet != null) {
				int size = proxySet.size();
				int index = (int) (Math.random() * size);
				proxy = (HttpHost) proxySet.get(index);
				if (proxy != null) {
					RequestConfig.Builder builder = this.config;
					builder.setProxy(proxy);
					this.client = this.clientBuilder.setDefaultRequestConfig(
							builder.build()).build();
				}
			}
		}
		logger.debug("[" + url + "][第" + (retryCount - retryIndex + 1) + "次抓取尝试][proxy " + proxy.toHostString() + "]");
		String locationHeader = "";
		if (retryIndex < 1) {
			this.get.abort();
			this.get.releaseConnection();
			logger.info("[数据获取][url=" + url + "][status=" + this.getStatus + "][exception=" + this.getException + "]");
			return this.getHtml;
		}
		try {
			URI uri = new URI(url);
			if (this.get == null) {
				this.get = new HttpGet();
			}
			this.get.setURI(uri);
			HttpContext context = new BasicHttpContext();
			CloseableHttpResponse response = this.client.execute(this.get,
					context);
			if (getLocation) {
				try {
					locationHeader = response.getFirstHeader("Location").getValue();
					String str1 = locationHeader;
					this.get.abort();
					return str1;
				} catch (Exception e) {
					this.get.abort();
					return "";
				}
			}
			this.getStatus = response.getStatusLine().getStatusCode();
			HttpEntity entity = response.getEntity();
			ContentType contentType = ContentType.getOrDefault(entity);
			entity = new BufferedHttpEntity(entity);
			Charset charset = contentType.getCharset() != null ? contentType.getCharset() : getCharsetFromByte(EntityUtils.toByteArray(entity));
			String responseCharset = "";
			if (charset != null) {
				responseCharset = charset.toString();
			}
			if (StringUtils.isBlank(responseCharset))
				responseCharset = DEFAULTCHARACTER;
			else if (StringUtils
					.equals(responseCharset.toLowerCase(), "gb2312")) {
				responseCharset = "GBK";
			}
			if (responseAsStream) {
				Header[] headers = response.getAllHeaders();
				boolean isGzip = false;
				for (Header header : headers) {
					String isGzipStr = header.getValue().toLowerCase();
					if (isGzipStr.contains("gzip")) {
						isGzip = true;
					}
				}
				InputStream is = entity.getContent();
				BufferedReader reader = null;
				if (isGzip) {
					GZIPInputStream gzipIs = new GZIPInputStream(is);
					reader = new BufferedReader(new InputStreamReader(gzipIs,
							responseCharset));
				} else {
					reader = new BufferedReader(new InputStreamReader(is,
							responseCharset));
				}
				String line = "";
				while ((line = reader.readLine()) != null)
					this.getHtml += line;
			} else {
				this.getHtml = EntityUtils.toString(entity);
			}
		} catch (SocketTimeoutException e) {
			this.getException = "SocketTimeoutException";
			this.get.abort();
			this.get.releaseConnection();
			retryIndex--;
			GetHtml(url, httpResponseConfig, retryIndex);
		} catch (ConnectTimeoutException e) {
			this.getException = "ConnectTimeoutException";
			this.get.abort();
			this.get.releaseConnection();
			retryIndex--;
			GetHtml(url, httpResponseConfig, retryIndex);
		} catch (UnknownHostException e) {
			this.getException = "UnknownHostException";
			this.get.abort();
			this.get.releaseConnection();
			retryIndex--;
			GetHtml(url, httpResponseConfig, retryIndex);
		} catch (IOException e) {
			this.getException = "IOException";
			this.get.abort();
			this.get.releaseConnection();
			retryIndex--;
			GetHtml(url, httpResponseConfig, retryIndex);
		} catch (URISyntaxException e) {
			this.getException = "URISyntaxException";
			this.get.abort();
			this.get.releaseConnection();
			retryIndex--;
			GetHtml(url, httpResponseConfig, retryIndex);
		} catch (Exception e) {
			this.getException = e.getMessage();
			e.printStackTrace();
			this.get.abort();
			this.get.releaseConnection();
			retryIndex--;
			GetHtml(url, httpResponseConfig, retryIndex);
		} finally {
			this.get.abort();
		}
		this.get.releaseConnection();
		if ((this.getStatus == 200) && (!StringUtils.isBlank(this.getHtml))) {
			logger.debug("[数据获取][url=" + url + "][html=" + this.getHtml + "]");
		} else {
			this.getException = "response_null";
			retryIndex--;
			GetHtml(url, httpResponseConfig, retryIndex);
		}
		if ((this.getStatus == 302) && (StringUtils.isBlank(this.getHtml))) {
			this.getHtml = locationHeader;
		}
		return this.getHtml;
	}

	public String GetHtml(String url, HttpResponseConfig httpResponseConfig) {
		return GetHtml(url, httpResponseConfig, retryCount);
	}

	public String GetHtml(String url, String body,
			HttpRequestConfig httpRequestConfig,
			HttpResponseConfig httpResponseConfig) {
		return GetHtml(url, body, httpRequestConfig, httpResponseConfig,
				retryCount);
	}

	public String GetLocationUrl(String url) {
		return GetHtml(url, null, retryCount);
	}

	public String GetLocationUrl(String url, String body,
			HttpRequestConfig httpRequsetConfig) {
		return GetHtml(url, body, httpRequsetConfig, null, retryCount);
	}

	private String GetHtml(String url, String body,
			HttpRequestConfig httpRequsetConfig,
			HttpResponseConfig httpResponseConfig, int retryIndex) {
		if (retryIndex < 1) {
			this.post.abort();
			this.post.releaseConnection();
			logger.debug("[数据获取][url=" + url + "][body=" + body + "][status=" + this.postStatus + "][exception=" + this.postException + "]");
			return this.postHtml;
		}
		boolean getLocation = false;
		boolean useStringEntity = httpRequsetConfig.isYesOrNo();
		boolean responseStream = false;
		if (httpResponseConfig == null)
			getLocation = true;
		else {
			responseStream = httpResponseConfig.isYesOrNo();
		}
		if (!StringUtils.isBlank(this.identidy)) {
			List<HttpHost> proxySet = proxyMap.get(this.identidy);
			if (proxySet != null) {
				int size = proxySet.size();
				int index = (int) (Math.random() * size);
				HttpHost proxy = (HttpHost) proxySet.get(index);
				if (proxy != null) {
					this.config.setProxy(proxy);
					this.client = this.clientBuilder.setDefaultRequestConfig(
							this.config.build()).build();
				}
			}
		}
		try {
			URI uri = new URI(url);
			if (this.post == null) {
				this.post = new HttpPost();
			}
			this.post.setURI(uri);
			if (useStringEntity) {
				StringEntity strEntity = new StringEntity(body);
				this.post.setEntity(strEntity);
			} else {
				InputStream is = new ByteArrayInputStream(
						body.getBytes("UTF-8"));
				HttpEntity inputStreamEntity = new InputStreamEntity(is);
				this.post.setEntity(inputStreamEntity);
			}
			HttpResponse response = this.client.execute(this.post);
			if (getLocation) {
				String locationheader = response.getFirstHeader("Location") == null ? ""
						: response.getFirstHeader("Location").getValue();
				return locationheader;
			}
			this.postStatus = response.getStatusLine().getStatusCode();
			HttpEntity entity = response.getEntity();
			ContentType contentType = ContentType.getOrDefault(entity);
			entity = new BufferedHttpEntity(entity);
			Charset charset = contentType.getCharset() != null ? contentType
					.getCharset() : getCharsetFromByte(EntityUtils
					.toByteArray(entity));
			String responseCharset = "";
			if (charset != null) {
				responseCharset = charset.toString();
			}
			if (StringUtils.isBlank(responseCharset)) {
				responseCharset = "UTF-8";
			}
			if (responseStream) {
				Header[] headers = response.getAllHeaders();
				boolean isGzip = false;
				for (Header header : headers) {
					String isGzipStr = header.getValue().toLowerCase();
					if (isGzipStr.contains("gzip")) {
						isGzip = true;
						break;
					}
				}
				InputStream is = entity.getContent();
				BufferedReader reader = null;
				if (isGzip) {
					GZIPInputStream gzipIs = new GZIPInputStream(is);
					reader = new BufferedReader(new InputStreamReader(gzipIs,
							responseCharset));
				} else {
					reader = new BufferedReader(new InputStreamReader(is,
							responseCharset));
				}
				String line = "";
				while ((line = reader.readLine()) != null)
					this.postHtml += line;
			} else {
				this.postHtml = EntityUtils.toString(entity);
			}
		} catch (SocketTimeoutException e) {
			this.postException = "SocketTimeoutException";
			this.post.abort();
			this.post.releaseConnection();
			retryIndex--;
			GetHtml(url, body, httpRequsetConfig, httpResponseConfig,
					retryIndex);
		} catch (ConnectTimeoutException e) {
			this.postException = "ConnectTimeoutException";
			this.post.abort();
			this.post.releaseConnection();
			retryIndex--;
			GetHtml(url, body, httpRequsetConfig, httpResponseConfig,
					retryIndex);
		} catch (UnknownHostException e) {
			this.postException = "UnknownHostException";
			this.post.abort();
			this.post.releaseConnection();
			retryIndex--;
			GetHtml(url, body, httpRequsetConfig, httpResponseConfig,
					retryIndex);
		} catch (IOException e) {
			this.postException = "IOException";
			this.post.abort();
			this.post.releaseConnection();
			retryIndex--;
			GetHtml(url, body, httpRequsetConfig, httpResponseConfig,
					retryIndex);
		} catch (URISyntaxException e) {
			this.postException = "URISyntaxException";
			this.post.abort();
			this.post.releaseConnection();
			retryIndex--;
			GetHtml(url, body, httpRequsetConfig, httpResponseConfig,
					retryIndex);
		} catch (Exception e) {
			this.postException = e.getMessage();
			this.post.abort();
			this.post.releaseConnection();
			retryIndex--;
			GetHtml(url, body, httpRequsetConfig, httpResponseConfig,
					retryIndex);
		} finally {
			this.post.abort();
		}
		this.post.releaseConnection();
		if ((this.postStatus == 200) && (!StringUtils.isBlank(this.postHtml))) {
			logger.debug("[数据获取][url=" + url + "][body=" + body + "][html=" + this.postHtml + "]");
		} else {
			this.postException = "response null";
			this.post.abort();
			this.post.releaseConnection();
			retryIndex--;
			GetHtml(url, body, httpRequsetConfig, httpResponseConfig,
					retryIndex);
		}
		return this.postHtml;
	}

	public byte[][] GetImageByteArr(String url) {
		byte[][] fileData = null;
		try {
			URI uri = new URI(url);
			if (this.get == null) {
				this.get = new HttpGet();
			}
			List<HttpHost> proxySet = proxyMap.get(this.identidy);
			HttpHost proxy = null;
			if (proxySet != null) {
				int size = proxySet.size();
				int index = (int) (Math.random() * size);
				proxy = (HttpHost) proxySet.get(index);
				if (proxy != null) {
					this.config.setProxy(proxy);
					this.client = this.clientBuilder.setDefaultRequestConfig(
							this.config.build()).build();
				}
			}
			logger.info("[" + url + "][第1次抓取尝试][proxy " + proxy.toHostString() + "]");
			this.get.setURI(uri);
			CloseableHttpResponse response = this.client.execute(this.get);
			Header header = response.getFirstHeader("Content-Type");
			String imageType = "jpg";
			if (header != null) {
				String value = header.getValue();
				if ((value.contains("image")) || (value.contains("Image"))) {
					imageType = value.replaceAll(".*/", "").replace(";", "");
				}
			}
			byte[] imageTypeArr = imageType.getBytes();
			HttpEntity entity = response.getEntity();
			InputStream is = entity.getContent();
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			IOUtils.copy(is, outStream);
			is.close();
			byte[] imageDataArr = outStream.toByteArray();
			fileData = new byte[][] { imageDataArr, imageTypeArr };
			outStream.close();
		} catch (ClientProtocolException localClientProtocolException) {
		} catch (IOException localIOException1) {
		} catch (Exception localException) {
		} finally {
			this.get.abort();
		}
		try {
			this.client.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.get.releaseConnection();
		return fileData;
	}

	public void setProxy(String ip, int port) {
		HttpHost proxy = new HttpHost(ip, port);
		this.config.setProxy(proxy);
		this.client = this.clientBuilder.setDefaultRequestConfig(
				this.config.build()).build();
	}

	public static Charset getCharsetFromByte(byte[] byteArray) {
		String content = new String(byteArray);
		Charset charset = null;
		Pattern pattern = Pattern
				.compile("<[mM][eE][tT][aA][^>]*([cC][Hh][Aa][Rr][Ss][Ee][Tt][\\s]*=[\\s\\\"']*)([\\w\\d-_]*)[^>]*>");
		Matcher matcher = pattern.matcher(content);
		if (matcher.find())
			charset = Charset.forName(matcher.group(2));
		else {
			charset = getCharsetFromBOM(byteArray);
		}

		return charset;
	}

	private static Charset getCharsetFromBOM(byte[] byteArray) {
		Charset charset = null;
		if ((byteArray == null) || (byteArray.length < 2)) {
			return charset;
		}
		int p = (byteArray[0] & 0xFF) << 8 | byteArray[1] & 0xFF;
		switch (p) {
		case 61371:
			charset = Charset.forName("UTF-8");
			break;
		case 65534:
			charset = Charset.forName("Unicode");
			break;
		case 65279:
			charset = Charset.forName("UTF-16BE");
			break;
		default:
			charset = Charset.forName("GBK");
		}
		return charset;
	}
}