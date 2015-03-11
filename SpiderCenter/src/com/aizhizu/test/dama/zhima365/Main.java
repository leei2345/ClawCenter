package com.aizhizu.test.dama.zhima365;

import java.io.File;

import com.aizhizu.test.dama.zhima365.com.zhima365.HttpApiClient;
import com.aizhizu.test.dama.zhima365.com.zhima365.support.LogTypeEnum;


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

		File imageFileTest = new File("/home/leei/git/aizhizu/SpiderCenter/src/com/aizhizu/test/dama/imagesource/file5.jpg");
		String recognizeResult = HttpApiClient.recognize(
				"1063",								// 软件ID. 如何获取? (参见 http://www.zhima365.com/jump/api_help_software.php)
				"e9c498b2dff8594d815a48f24178781d", // 软件key. 如何获取? (参见 http://www.zhima365.com/jump/api_help_software.php)
				"leei2345", 							// 需要注册用户, 然后联系客服QQ1766515174充入免费测试题分. 注册地址: http://www.zhima365.com/api.php?id=464
				"xinying2345",							// 用户的密码
				imageFileTest, 						// 需要上传的文件
				"9893", 							// 图片类型. 图片类型是什么? (参见 http://www.zhima365.com/jump/api_help_picture_type.php)
				120, 								// 设置超时时间(单位:秒)
				"", 								// 备注
				LogTypeEnum.YES);					// 是否记录日志
		System.out.println("识别结果：" + recognizeResult);
		
		
//		String queryBalanceResult = HttpApiClient.queryBalance(
//				"test", 							// 需要注册用户, 然后联系客服QQ1766515174充入免费测试题分. 注册地址: http://www.zhima365.com/api.php?id=464
//				"123456");							// 用户的密码
//		System.out.println("查询余额：" + queryBalanceResult);
//		
//
//		String[] recognizeResults = recognizeResult.split("\\|");
//		String reportErrorResult = HttpApiClient
//				.reportError(recognizeResults[3]);	// 报告错误. 注意: 传入的参数是一个字符串, 由识别函数返回得到
//		System.out.println("执行结果：" + reportErrorResult);

	}

}