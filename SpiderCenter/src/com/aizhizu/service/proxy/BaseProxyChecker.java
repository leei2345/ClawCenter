package com.aizhizu.service.proxy;

import com.aizhizu.http.HttpMethod;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("deprecation")
public abstract class BaseProxyChecker
{
  protected String identidy;
  protected HttpHost proxy;
  protected String url;
  protected CloseableHttpClient client = new DefaultHttpClient();
  protected RequestConfig.Builder config = RequestConfig.custom();
  protected HttpClientBuilder builder = HttpClientBuilder.create();
  private static Logger logger = LoggerFactory.getLogger("ProxyLogger");

  public BaseProxyChecker(String identidy, String url) {
    this.identidy = identidy;
    this.url = url;
    this.config.setAuthenticationEnabled(true);
    this.config.setConnectTimeout(10000);
    this.config.setSocketTimeout(10000);
    this.builder = HttpClientBuilder.create();
    this.builder.setMaxConnTotal(100);
    this.builder.setMaxConnPerRoute(500);
    this.client = this.builder.setDefaultRequestConfig(this.config.build()).build();
  }

  public void InstallProxyHost(HttpHost proxy) {
    this.proxy = proxy;
    this.config.setProxy(proxy);
    this.client = this.builder.setDefaultRequestConfig(this.config.build()).build();
  }

  public Object[] HttpRequest() {
    HttpGet get = new HttpGet();
    int httpStatusCode = 400;
    String html = "";
    try {
      URI uri = new URI(this.url);
      get.setURI(uri);
      CloseableHttpResponse response = this.client.execute(get);
      httpStatusCode = response.getStatusLine().getStatusCode();
      HttpEntity entity = response.getEntity();
      ContentType contentType = ContentType.getOrDefault(entity);
      entity = new BufferedHttpEntity(entity);
      Charset charset = contentType.getCharset() != null ? contentType.getCharset() : HttpMethod.getCharsetFromByte(EntityUtils.toByteArray(entity));
      String responseCharset = "";
      if (charset != null) {
        responseCharset = charset.toString();
      }
      if (StringUtils.isBlank(responseCharset))
        responseCharset = "UTF-8";
      else if (StringUtils.equals(responseCharset.toLowerCase(), "gb2312")) {
        responseCharset = "GBK";
      }
      InputStream is = entity.getContent();
      BufferedReader reader = null;
      reader = new BufferedReader(new InputStreamReader(is, responseCharset));
      String line = "";
      while ((line = reader.readLine()) != null)
        html = html + line;
    }
    catch (URISyntaxException e) {
      logger.warn("[proxy check][" + this.identidy + "][" + this.proxy.toHostString() + "][URISyntaxException]");
    } catch (ClientProtocolException e) {
      logger.warn("[proxy check][" + this.identidy + "][" + this.proxy.toHostString() + "][ClientProtocolException]");
    } catch (IOException e) {
      logger.warn("[proxy check][" + this.identidy + "][" + this.proxy.toHostString() + "][IOException]");
    } catch (Exception e) {
      logger.warn("[proxy check][" + this.identidy + "][" + this.proxy.toHostString() + "][" + e.getMessage() + "]");
    } finally {
      get.abort();
      get.releaseConnection();
    }
    try {
      this.client.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    Object[] returnArr = new Object[2];
    returnArr[0] = Integer.valueOf(httpStatusCode);
    returnArr[1] = html;
    return returnArr;
  }

  protected abstract boolean analyze(Object[] paramArrayOfObject);

  public int CheckApplicability()
  {
    int res = 0;
    try {
      Object[] httpResponseObject = HttpRequest();
      boolean analyzeRes = analyze(httpResponseObject);
      if (analyzeRes)
        res = 1;
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return res;
  }

  public String getIdentidy() {
    return this.identidy;
  }
}