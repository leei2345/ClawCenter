package com.aizhizu.service.dama;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import com.aizhizu.http.HttpMethod;
import com.aizhizu.util.ConfigUtil;
import com.aizhizu.util.LoggerUtil;
import com.alibaba.fastjson.JSONObject;

public class YunDaMa {

	private static final int RETRYCOUNT = 5;
	private File imageFile;
	private static Map<String, String> uploadparams = new HashMap<String, String>();
	private static Map<String, String> reportparams = new HashMap<String, String>();
	private static Map<Integer, String> configparams = new HashMap<Integer, String>();
	private static String url;
	private static final String defaultPhoneNum = "1";
	
	static  {
		url = ConfigUtil.getString("url");
		uploadparams.put("username", ConfigUtil.getString("username"));
		uploadparams.put("password", ConfigUtil.getString("password"));
		uploadparams.put("codetype", ConfigUtil.getString("codetype"));
		uploadparams.put("appid", ConfigUtil.getString("appid"));
		uploadparams.put("appkey", ConfigUtil.getString("appkey"));
		uploadparams.put("timeout", ConfigUtil.getString("timeout"));
		
		reportparams.put("username", ConfigUtil.getString("username"));
		reportparams.put("password", ConfigUtil.getString("password"));
		reportparams.put("appid", ConfigUtil.getString("appid"));
		reportparams.put("appkey", ConfigUtil.getString("appkey"));
		reportparams.put("flag", "0");
		
		uploadparams.put("method", ConfigUtil.getString("uploadmethod"));
		reportparams.put("method", ConfigUtil.getString("reportmethod"));

		configparams.put(0, "成功");
		configparams.put(-1001, "密码错误");
		configparams.put(-1002, "软件ID/密钥有误");
		configparams.put(-1003, "用户被封");
		configparams.put(-1004, "IP被封");
		configparams.put(-1005, "软件被封");
		configparams.put(-1006, "登录IP与绑定的区域不匹配");
		configparams.put(-1007, "账号余额为零");
		configparams.put(-2001, "验证码类型(codetype)有误");
		configparams.put(-2002, "验证码图片太大");
		configparams.put(-2003, "验证码图片损坏");
		configparams.put(-2004, "上传验证码图片失败");
		configparams.put(-3001, "验证码ID不存在");
		configparams.put(-3002, "验证码正在识别");
		configparams.put(-3003, "验证码识别超时");
		configparams.put(-3004, "验证码看不清");
		configparams.put(-3005, "验证码报错失败");
		configparams.put(-4001, "充值卡号不正确或已使用");
		configparams.put(-5001, "注册用户失败");
		
	} 
	
	
	public YunDaMa (File imageFile) {
		this.imageFile = imageFile;
	}
	
	public String GetPhoneNumber () {
		String phoneNumber = defaultPhoneNum;
		String fileName = imageFile.getName();
		Map<String, File> map = new HashMap<String, File>();
		map.put("file", imageFile);
out:	for (int retryIndex = 1; retryIndex <= RETRYCOUNT; retryIndex++) {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e2) {
				e2.printStackTrace();
			}
			HttpMethod me = new HttpMethod();
			//{"ret":0,"cid":433761793,"text":"13651042000"}
			String phoneStr = me.DaMa(url, uploadparams, map);
			String phoneNumStr;
			long cid;
			int statusCode = 999;
			try {
				JSONObject object = JSONObject.parseObject(phoneStr);
				statusCode = object.getIntValue("ret");
				if (statusCode != 0 && statusCode != -3002) {
					String mes = configparams.get(statusCode);
					LoggerUtil.HttpInfoLog("[" + fileName + "][第" + retryIndex + "次打码失败][" + mes + "][" + statusCode + "]");
					continue out;
				}
				phoneNumStr = object.getString("text");
				cid = object.getLongValue("cid");
				if (StringUtils.isBlank(phoneNumStr)) {
in:				for (int loopIndex = 1; loopIndex <= 30; loopIndex++) {
						Thread.sleep(2000);
						String targetUrl = url +"?cid=" + cid + "&method=result";
						String result = me.LoopDaMa(targetUrl);
						try {
							JSONObject loopObj = JSONObject.parseObject(result);
							int loopStatusCode = loopObj.getIntValue("ret");
							if (loopStatusCode != 0) {
								String mes = configparams.get(loopStatusCode);
								LoggerUtil.HttpInfoLog("[" + fileName + "][第" + retryIndex + "次打码失败][第" + loopIndex + "次重试][" + mes + "][" + statusCode + "]");
								continue in;
							}
							String phoneResult = loopObj.getString("text");
							if (StringUtils.isBlank(phoneResult)) {
								continue in;
							} else {
								phoneNumStr = phoneResult;
								break in;
							}
						} catch (Exception e) {
							continue in;
						}
					}
				}
			} catch (Exception e) {
				LoggerUtil.HttpInfoLog("[" + fileName + "][第" + retryIndex + "次打码失败][exception][" + e.getLocalizedMessage() + "]");
				continue;
			}
			phoneNumStr = phoneNumStr.replace("-", "").replace("-", "");
			if (!StringUtils.isBlank(phoneNumStr) && !phoneNumStr.startsWith("1") && !phoneNumStr.contains("看不清") && !phoneNumStr.contains("U")) {
				LoggerUtil.HttpInfoLog("[" + fileName + "][第" + retryIndex + "次打码结束][" + phoneNumStr + "]");
				break;
			} else if (phoneNumStr.matches("\\d{11}")) {
				phoneNumber = phoneNumStr;
				LoggerUtil.HttpInfoLog("[" + fileName + "][第" + retryIndex + "次打码成功][" + phoneNumber + "]");
				break;
			} else {
				reportparams.put("cid", String.valueOf(cid));
				String targetUrl = "";
				for (Entry<String, String> entry : reportparams.entrySet()) {
					targetUrl += ("&" + entry.getKey() + "=" + entry.getValue());
				}
				targetUrl = targetUrl.replaceFirst("&", "");
				targetUrl = url +"?" + targetUrl;
				me.ReporyDaMaError(targetUrl);
				LoggerUtil.HttpInfoLog("[" + fileName + "][第" + retryIndex + "次打码错误 已经报错][" + phoneNumStr + "]");
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
					continue;
				}
				continue;
			}
		}
		return phoneNumber;
	}
	
	public static void main(String[] args) {
		File imageFile = new File("/home/leei/image/test.png");
		YunDaMa y = new YunDaMa(imageFile);
		String test = y.GetPhoneNumber();
		System.out.println(test);
	}
	

}
