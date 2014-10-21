package com.aizhizu.service;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.Map;

public class DigSign {
	
	public static String[] bubbleSort(String[] originalArray) {
		for (int i = 0; i < originalArray.length; i++) {
			Boolean exchange = Boolean.valueOf(false);

			for (int j = originalArray.length - 2; j >= i; j--) {
				if (originalArray[(j + 1)].compareTo(originalArray[j]) >= 0)
					continue;
				String temp = originalArray[(j + 1)];
				originalArray[(j + 1)] = originalArray[j];
				originalArray[j] = temp;

				exchange = Boolean.valueOf(true);
			}

			if (!exchange.booleanValue()) {
				break;
			}
		}

		return originalArray;
	}

	public static String callUrl(String url, String data) throws Exception {
		HttpURLConnection conn = null;
		String content = "";

		URL getUrl = new URL(url);
		conn = (HttpURLConnection) getUrl.openConnection();

		conn.setDoOutput(true);
		conn.setDoInput(true);
		conn.setRequestMethod("POST");
		conn.setUseCaches(false);
		conn.setInstanceFollowRedirects(true);
		conn.setRequestProperty("Content-Type", "text/xml");

		byte[] bdata = data.getBytes("utf-8");
		conn.setRequestProperty("Content-Length", String.valueOf(bdata.length));
		conn.connect();
		DataOutputStream out = new DataOutputStream(conn.getOutputStream());
		out.write(bdata);
		out.flush();
		out.close();

		BufferedReader reader = new BufferedReader(new InputStreamReader(
				conn.getInputStream()));
		String inputLine;
		while ((inputLine = reader.readLine()) != null) {
			content = content + inputLine;
		}
		reader.close();
		conn.disconnect();

		return content;
	}

	public static String createDigitalSign(Map<String, String> ht) {
		if ((!ht.containsKey("accountKey"))
				|| (((String) ht.get("accountKey")).toString().trim().length() == 0)) {
			return "";
		}
		String accountKey = ((String) ht.get("accountKey")).toString().trim();

		String version = "";
		String serviceName = "";
		String accountId = "";
		String reqTime = "";

		if (ht.containsKey("version")) {
			version = (String) ht.get("version");
		}

		if (ht.containsKey("serviceName")) {
			serviceName = (String) ht.get("serviceName");
		}

		if (ht.containsKey("accountId")) {
			accountId = (String) ht.get("accountId");
		}

		if (ht.containsKey("reqTime")) {
			reqTime = (String) ht.get("reqTime");
		}

		String[] originalArray = { "Version=" + version,
				"AccountID=" + accountId, "ServiceName=" + serviceName,
				"ReqTime=" + reqTime };

		String[] sortedArray = bubbleSort(originalArray);

		String digitalSing = getMD5ByArray(sortedArray, accountKey, "utf-8");

		return digitalSing;
	}

	public static String getMD5(String input, String charset) {
		String s = null;
		char[] hexDigits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'a', 'b', 'c', 'd', 'e', 'f' };
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(input.getBytes(charset));
			byte[] tmp = md.digest();

			char[] str = new char[32];

			int k = 0;
			for (int i = 0; i < 16; i++) {
				byte byte0 = tmp[i];
				str[(k++)] = hexDigits[(byte0 >>> 4 & 0xF)];

				str[(k++)] = hexDigits[(byte0 & 0xF)];
			}
			s = new String(str);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return s;
	}

	public static String getMD5ByArray(String[] sortedArray, String key,
			String charset) {
		StringBuilder prestr = new StringBuilder();

		for (int i = 0; i < sortedArray.length; i++) {
			if (i == sortedArray.length - 1)
				prestr.append(sortedArray[i]);
			else {
				prestr.append(sortedArray[i] + "&");
			}
		}

		prestr.append(key);

		return getMD5(prestr.toString(), charset);
	}
}