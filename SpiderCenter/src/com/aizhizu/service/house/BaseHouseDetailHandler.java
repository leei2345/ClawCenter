package com.aizhizu.service.house;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.apache.commons.lang3.StringUtils;

import com.aizhizu.bean.BaseHouseEntity;
import com.aizhizu.bean.FileWriterEntity;
import com.aizhizu.dao.HouseSourceCheckAndWrite;
import com.aizhizu.util.LoggerUtil;


public abstract class BaseHouseDetailHandler extends BaseHouseClawer {

	private FileWriterEntity fileWriter;
	private int lineNum;
	
	public BaseHouseDetailHandler(String mark) {
		super(mark);
	}
	
	public void setWriter(FileWriterEntity writer) {
		this.fileWriter = writer;
	}

	public void setLineNum(int lineNum) {
		this.lineNum = lineNum;
	}

	protected boolean DealWithChuzuData(BaseHouseEntity house) {
		String lineSeparator=System.getProperty("line.separator");
		boolean res = false;
		String url = house.getUrl();
		house.setNum(lineNum);
		String phoneNum = house.getPhone();
		if (phoneNum.contains("**") || StringUtils.isBlank(phoneNum) || !phoneNum.matches("\\d{11}")) {
			return false;
		} else {
			/** 记录数据库 */
			try {
				HouseSourceCheckAndWrite.InsertClawHistory(url, identidy, this.fileWriter.getDate());
				LoggerUtil.ClawerLog(identidy, "[HouseHistory][Add Done][" + identidy + "][" + url + "]");
			} catch (Exception e) {
				LoggerUtil.ClawerLog(identidy, "[HouseHistory][Add Error][" + identidy + "][" + url + "]");
			}
		}
		String filePath = this.fileWriter.getFilePath();
		File imageFileDir = new File(filePath + "/" + lineNum);
		if (!imageFileDir.exists()) {
			imageFileDir.mkdirs();
		}
		String imageFilePath = imageFileDir.getAbsolutePath();
		File imageSourceDataFile = new File(imageFilePath + "/source.html");
		FileWriter sourceDataWriter = null;
		OutputStreamWriter writer = this.fileWriter.getWriter();
		try {
			/** 写文件 */
			writer.write(house.toString() + lineSeparator);
			writer.flush();
			sourceDataWriter = new FileWriter(imageSourceDataFile);
			String html = "<html><body onload=\"parent.location='" + house.getUrl() + "'\"></body></html>";
			sourceDataWriter.write(html);  
			sourceDataWriter.flush();
		} catch (IOException e) {
			e.printStackTrace();
			return res;
		} finally {
			if (sourceDataWriter != null) {
				try {
					sourceDataWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		res = true;
		/** 图片异步抓取 */
		ImageDownLoader downLoader = new ImageDownLoader(this.identidy, imageFilePath, url);
		downLoader.setImageUrlList(house.getImageUrlList());
		new Thread(downLoader).start();
		
		/** 数据异步推送 */
		DataPusher pusher = new DataPusher(house, devUrl);
		new Thread(pusher).start();
		DataPusher pusher_back = new DataPusher(house, baseUrl);
		new Thread(pusher_back).start();
		return res;
	}

}
