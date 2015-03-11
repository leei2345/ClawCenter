package com.aizhizu.test.dama.yundama;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 调用api 的客户端
 * 
 * @author ryan
 * 
 */
public class HttpApiClient {

	/**
	 * 
	 * 验证码识别
	 * 
	 * @param soft_id
	 *            软件ID(详情请参考：http://www.zhima365.com/jump/api_help_software.php)
	 * @param soft_key
	 *            软件KEY
	 * @param user_name
	 *            用户名(详情请参考：http://www.zhima365.com/jump/api_help_user_name.php)
	 * @param password
	 *            密码
	 * @param image_path
	 *            图片本地路径
	 * @param pic_type
	 *            验证码类型(详情请参考：http://www.zhima365.com/jump/api_help_picture_type
	 *            .php)，由于验证码类型太多，不适合用枚举类型
	 * @param timeout
	 *            以秒为单位
	 * @param remark
	 *            备注
	 * @param log
	 *            是否记录日志
	 * @return 返回如下格式: 返回值|返回结果|公告|pic_id|
	 */
	public static String recognize( File imageFile) {
		if(imageFile==null || !imageFile.exists()) return "上传的文件不存在";
		Map<String, String> params = new HashMap<String, String>();
		params.put("username", "leei2345");
		params.put("password", "123123");
		params.put("codetype", "4011");
		params.put("appid", "1103");
		params.put("appkey", "827d1846b055c05e49660d8827bf6d71");
		params.put("method", "upload");

		Map<String, File> files = new HashMap<String, File>();
		files.put("file", imageFile);

		return HttpClientUtil.sendPostRequest(RequestUrl.HTTP_API_URL.getUrl(), 	params, files, "UTF-8");
	}

	/**
	 * 验证码汇报错误
	 * 
	 * @param pic_id
	 *            图片ID, 由识别函数返回
	 * @return 返回如下格式: 返回值|返回结果|
	 */
	public static String reportError(String pic_id) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("type", RequestType.REPORT_ERROR.getKey());
		params.put("pic_id", pic_id);
		return HttpClientUtil.sendPostRequest(RequestUrl.HTTP_API_URL.getUrl(),
				params, "UTF-8", "UTF-8");
	}

	/**
	 * 
	 * @param user_name
	 *            用户名(详情请参考：http://www.zhima365.com/jump/api_help_user_name.php)
	 * @param password
	 *            密码
	 * @return 返回如下格式: 返回值|返回结果|余额|
	 */
	public static String queryBalance(String user_name, String password) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("type", RequestType.QUERY_BALANCE.getKey());
		params.put("user_name", user_name);
		params.put("password", password);
		return HttpClientUtil.sendPostRequest(RequestUrl.HTTP_API_URL.getUrl(),
				params, "UTF-8", "UTF-8");
	}

}
