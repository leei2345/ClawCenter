package com.aizhizu.service.house;

import com.aizhizu.http.HttpMethod;
import com.aizhizu.http.Method;
import com.aizhizu.service.DigSign;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageOcr {
	private static String imageTempFilePath = "image_temp/";
	private static String imageFilePath;

	static {
		String filePath = ImageOcr.class.getClassLoader().getResource("")
				.getPath();
		File file = new File(filePath);
		String supFilePath = file.getParent();
		File supFile = new File(supFilePath + "/" + imageTempFilePath);
		if (!supFile.exists()) {
			supFile.mkdirs();
		}
		imageFilePath = supFilePath + "/" + imageTempFilePath;
	}

	public static String getImageLast4Num(String identidy, String url) {
		String tempName = DigSign.getMD5(url, "UTF-8");
		HttpMethod me = new HttpMethod(identidy);
		me.AddHeader(Method.Get, "Host", "image.58.com");
		me.AddHeader(Method.Get, "User-Agent",
				"Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:32.0) Gecko/20100101 Firefox/32.0");
		me.AddHeader(Method.Get, "Accept",
				"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		me.AddHeader(Method.Get, "Accept-Language",
				"zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3");
		me.AddHeader(Method.Get, "Accept-Encoding", "gzip, deflate");
		byte[][] imageByteArr = me.GetImageByteArr(url);
		if (imageByteArr == null) {
			return "";
		}
		String imageType = new String(imageByteArr[1]);
		byte[] imageData = imageByteArr[0];
		File imageFile = new File(imageFilePath + tempName + "." + imageType);
		FileOutputStream fileStream = null;
		try {
			imageFile.createNewFile();
			fileStream = new FileOutputStream(imageFile);
			fileStream.write(imageData);
			fileStream.flush();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fileStream != null) {
				try {
					fileStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		String valCode = "";
		try {
			valCode = new OCR().recognizeText(imageFile, imageType);
			valCode = valCode.replace("-", "").replaceAll("\\s+", "");
			int length = valCode.length();
			if (length != 0)
				valCode = valCode.substring(length - 4, length);
		} catch (IOException e) {
		} catch (Exception e) {
		} finally {
			imageFile.delete();
		}
		return valCode;
	}

	public static void main(String[] args) {
		String url = "http://image.58.com/showphone.aspx?t=v1&v=2733FB275AB1F50D2007762BE3EC09A82";
		String code = getImageLast4Num("web_wuba", url);
		System.out.println(code);
	}
}