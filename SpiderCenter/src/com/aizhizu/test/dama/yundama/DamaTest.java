//package com.aizhizu.test.dama.yundama;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.util.HashMap;
//import java.util.Map;
//
//import com.aizhizu.http.HttpMethod;
//import com.aizhizu.http.Method;
//
//public class DamaTest {
//	
//	public static void main(String[] args) {
//		HttpMethod m = new HttpMethod("test");
//		String filePath = "/home/leei/git/aizhizu/SpiderCenter/src/com/aizhizu/test/dama/file1.png";
//		
//		m.AddHeader(Method.Post, "Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
//		m.AddHeader(Method.Post, "Accept-Encoding", "deflate");
//		m.AddHeader(Method.Post, "Accept-Language", "zh-CN,zh;q=0.8");
//		m.AddHeader(Method.Post, "Cache-Control", "max-age=0");
//		m.AddHeader(Method.Post, "Connection", "keep-alive");
////		m.AddHeader(Method.Post, "Content-Length", String.valueOf(100000));
//		m.AddHeader(Method.Post, "Content-Type", "multipart/form-data; boundary=----WebKitFormBoundaryGBuhAFKoyBH0EAkt");
//		m.AddHeader(Method.Post, "Host", "api.yundama.com");
//		m.AddHeader(Method.Post, "Origin", "http://www.yundama.com");
//		m.AddHeader(Method.Post, "Referer", "http://www.yundama.com/download/YDMHttp.html");
//		m.AddHeader(Method.Post, "User-Agent", "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.115 Safari/537.36");
//		Map<String, String> map = new HashMap<String, String>();
//		map.put("username", "leei2345");
//		map.put("password", "21321423423");
//		map.put("codetype", "4011");
//		map.put("appid", "1103");
//		map.put("appkey", "827d1846b055c05e49660d8827bf6d71");
//		map.put("method", "upload");
////		map.put("file", "file1.png");
//		
//		String res = m.PostDama("http://api.yundama.com/api.php", map, filePath);
//		System.out.println(res);
//		
//	}
//	
//
//}
