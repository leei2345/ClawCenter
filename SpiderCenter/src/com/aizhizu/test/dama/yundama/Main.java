package com.aizhizu.test.dama.yundama;

import java.io.File;


/**
 * 
 * @author ryan
 * 
 */
public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		File imageFileTest = new File("/home/leei/git/aizhizu/SpiderCenter/src/com/aizhizu/test/dama/imagesource/file6.jpg");
		
		
		String recognizeResult = HttpApiClient.recognize(imageFileTest);					// 是否记录日志
		System.out.println("识别结果：" + recognizeResult);
		
		
//		String queryBalanceResult = HttpApiClient.queryBalance(
//				"test", 							// 需要注册用户, 然后联系客服QQ1766515174充入免费测试题分. 注册地址: http://www.zhima365.com/api.php?id=464
//				"123456");							// 用户的密码
//		System.out.println("查询余额：" + queryBalanceResult);
		

//		String[] recognizeResults = recognizeResult.split("\\|");
//		String reportErrorResult = HttpApiClient
//				.reportError(recognizeResults[3]);	// 报告错误. 注意: 传入的参数是一个字符串, 由识别函数返回得到
//		System.out.println("执行结果：" + reportErrorResult);

	}

}
