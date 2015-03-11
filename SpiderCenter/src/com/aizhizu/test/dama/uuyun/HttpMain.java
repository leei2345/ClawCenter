package com.aizhizu.test.dama.uuyun;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import com.aizhizu.http.HttpMethod;
import com.aizhizu.http.HttpResponseConfig;
import com.aizhizu.http.Method;
import com.aizhizu.service.DigSign;

public class HttpMain {


    //获取MAC地址的方法  
	private static String getMACAddress() {
		String mac = null;
		BufferedReader bufferedReader = null;
		Process process = null;
		try {
			// linux下的命令，一般取eth0作为本地主网卡 显示信息中包含有mac地址信息
			process = Runtime.getRuntime().exec("ifconfig eth0");
			bufferedReader = new BufferedReader(new InputStreamReader(
					process.getInputStream()));
			String line = null;
			int index = -1;
			while ((line = bufferedReader.readLine()) != null) {
				index = line.toLowerCase().indexOf("硬件地址");
				if (index != -1) {
					// 取出mac地址并去除2边空格
					mac = line.substring(index + 4).trim();
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (bufferedReader != null) {
					bufferedReader.close();
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			bufferedReader = null;
			process = null;
		}
		mac = mac.replace(":", "");
		System.out.println(mac);
		return mac;
	}  
	
	
	public static void main(String[] args) {
		HttpMethod m = new HttpMethod("test");
		m.AddHeader(Method.Get, "SID","104032");
		String md51 = DigSign.getMD5("104032" + new String("455b1c0d05874a0983ac3fc60e0f8821").toUpperCase(), "UTF-8");
		m.AddHeader(Method.Get, "HASH",md51);					//32位MD5加密小写
		m.AddHeader(Method.Get, "UUVersion","1.0.0.1");                    
		m.AddHeader(Method.Get, "UID","100");
		String md52 = DigSign.getMD5(new String("455b1c0d05874a0983ac3fc60e0f8821").toUpperCase() + "100", "UTF-8");
		m.AddHeader(Method.Get, "User-Agent", md52);
		String md53 =  DigSign.getMD5(new String("455b1c0d05874a0983ac3fc60e0f8821").toUpperCase() + new String("leei2345").toUpperCase(), "UTF-8");
		m.AddHeader(Method.Get, "KEY", md53 + getMACAddress());                
		String md54 = DigSign.getMD5(new String("leei2345").toUpperCase() + getMACAddress() + new String("455b1c0d05874a0983ac3fc60e0f8821").toUpperCase(), "UTF-8");
		m.AddHeader(Method.Get, "UUKEY", md54);
		String GetServiceList = m.GetHtml("http://common.taskok.com:9000/Service/ServerConfig.aspx", HttpResponseConfig.ResponseAsString);
		System.out.println(GetServiceList);
		
		String md56 = DigSign.getMD5("xinying2345", "UTF-8");
		String loginUrl = "http://login.uudama.com:9000/Upload/UULogin.aspx?U=leei2345&p=" + md56;
		String loginRes = m.GetHtml(loginUrl, HttpResponseConfig.ResponseAsString);
		System.out.println(loginRes);
		
	}

}
