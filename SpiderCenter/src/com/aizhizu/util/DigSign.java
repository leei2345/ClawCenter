package com.aizhizu.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class DigSign {
	/**
	 * 数组排序（冒泡排序法）
	 * 
	 * @param originalArray
	 *            待排序字符串数组
	 * @return 经过冒泡排序过的字符串数组
	 */
	public static String[] bubbleSort(String[] originalArray) {
		int i, j; // 交换标志
		String temp;
		Boolean exchange;

		for (i = 0; i < originalArray.length; i++) // 最多做R.Length-1趟排序
		{
			exchange = false; // 本趟排序开始前，交换标志应为假

			for (j = originalArray.length - 2; j >= i; j--) {
				if (originalArray[j + 1].compareTo(originalArray[j]) < 0)// 交换条件
				{
					temp = originalArray[j + 1];
					originalArray[j + 1] = originalArray[j];
					originalArray[j] = temp;

					exchange = true; // 发生了交换，故将交换标志置为真
				}
			}

			if (!exchange) // 本趟排序未发生交换，提前终止算法
			{
				break;
			}
		}

		return originalArray;
	}

	/**
	 * 请求服务器URL地址返回数据POST方式
	 * 
	 * @param url
	 *            远程访问的地址
	 * @param data
	 *            参数
	 * @return // * 远程页面调用结果
	 * @throws Exception
	 */
	public static String callUrl(String url, String data) throws Exception {
		HttpURLConnection conn = null;
		String content = "";

		URL getUrl = new URL(url);
		conn = (HttpURLConnection) getUrl.openConnection();

		// conn.setConnectTimeout(timeOut);
		// conn.setReadTimeout(timeOut);

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
			content += inputLine;
		}
		reader.close();
		conn.disconnect();

		return content;
	}

	/**
	 * 创建数字签名
	 * 
	 * @param ht
	 *            存放数字签名参数的Hashtable
	 * @return String DigitalSign
	 * @throws Exception
	 */
	public static String createDigitalSign(Map<String, String> ht) {
		if (!ht.containsKey("accountKey")
				|| ht.get("accountKey").toString().trim().length() == 0) {
			return "";
		}
		String accountKey = ht.get("accountKey").toString().trim(); // API帐户密钥

		String version = ""; // 版本号
		String serviceName = ""; // 调用接口的方法名称
		String accountId = ""; // API帐户密钥
		String reqTime = ""; // 当前日期

		if (ht.containsKey("version")) {
			version = ht.get("version");
		}

		if (ht.containsKey("serviceName")) {
			serviceName = ht.get("serviceName");
		}

		if (ht.containsKey("accountId")) {
			accountId = ht.get("accountId");
		}

		if (ht.containsKey("reqTime")) {
			reqTime = ht.get("reqTime");
		}

		String[] originalArray = { "Version=" + version,
				"AccountID=" + accountId, "ServiceName=" + serviceName,
				"ReqTime=" + reqTime };

		String[] sortedArray = bubbleSort(originalArray);

		String digitalSing = getMD5ByArray(sortedArray, accountKey, "utf-8");

		return digitalSing;

	}

	/**
	 * MD5 哈希运算
	 * 
	 * @param input
	 *            待计算MD5哈希值的输入字符串
	 * @param charset
	 *            输入字符串的字符集
	 * @return 输入字符串的MD5哈希值
	 */
	public static String getMD5(String input, String charset) {

		String s = null;
		char hexDigits[] = { // 用来将字节转换成 16 进制表示的字符
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd',
				'e', 'f' };
		try {
			java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
			md.update(input.getBytes(charset));
			byte tmp[] = md.digest(); // MD5 的计算结果是一个 128 位的长整数，用字节表示就是 16 个字节

			char str[] = new char[16 * 2]; // 每个字节用 16 进制表示的话，使用两个字符，所以表示成 16
											// 进制需要 32 个字符

			int k = 0; // 表示转换结果中对应的字符位置
			for (int i = 0; i < 16; i++) { // 从第一个字节开始，对 MD5 的每一个字节转换成 16
											// 进制字符的转换

				byte byte0 = tmp[i]; // 取第 i 个字节
				str[k++] = hexDigits[byte0 >>> 4 & 0xf]; // 取字节中高 4 位的数字转换,
															// >>>
															// 为逻辑右移，将符号位一起右移
				str[k++] = hexDigits[byte0 & 0xf]; // 取字节中低 4 位的数字转换
			}
			s = new String(str); // 换后的结果转换为字符串

		} catch (Exception e) {
			e.printStackTrace();
		}
		return s;
	}

	/**
	 * 获取字符数组的MD5哈希值
	 * 
	 * @param sortedArray
	 *            待计算MD5哈希值的输入字符数组
	 * @param key
	 *            密钥
	 * @param charset
	 *            输入字符串的字符集
	 * @return 输入字符数组的MD5哈希值
	 */
	public static String getMD5ByArray(String[] sortedArray, String key,
			String charset) {
		// 构造待md5摘要字符串
		StringBuilder prestr = new StringBuilder();

		for (int i = 0; i < sortedArray.length; i++) {
			if (i == sortedArray.length - 1) {
				prestr.append(sortedArray[i]);
			} else {
				prestr.append(sortedArray[i] + "&");
			}
		}

		prestr.append(key);// 此处key为上面的innerKey

		return getMD5(prestr.toString(), charset);
	}
	
}