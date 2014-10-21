package com.aizhizu.service.house;

import com.aizhizu.http.HttpMethod;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 异步下载图片
 * @author leei
 *
 */
public class ImageDownLoader implements Runnable {
	private String identidy;
	private String filePath;
	private String sourceUrl;
	private Set<String> imageUrlList = new HashSet<String>();
	private static final Logger logger = LoggerFactory
			.getLogger("ClawerLogger");

	public Set<String> getImageUrlList() {
		return this.imageUrlList;
	}

	public void setImageUrlList(Set<String> imageUrlList) {
		this.imageUrlList = imageUrlList;
	}

	public ImageDownLoader(String identidy, String filePath, String sourceUrl) {
		this.identidy = identidy;
		this.filePath = filePath;
		this.sourceUrl = sourceUrl;
	}

	public void run() {
		int imageIndex = 1;
		int size = this.imageUrlList.size();
		for (String imageUrl : this.imageUrlList) {
			HttpMethod imageMe = new HttpMethod(this.identidy);
			byte[][] imageArr = imageMe.GetImageByteArr(imageUrl);
			if (imageArr == null) {
				continue;
			}
			String imageLastName = new String(imageArr[1]);
			File wrapperImageFile = new File(this.filePath);
			if (!wrapperImageFile.exists()) {
				wrapperImageFile.mkdir();
			}
			String imageFilePathName = wrapperImageFile.getAbsolutePath() + "/"
					+ imageIndex + "." + imageLastName;
			FileOutputStream imageOut = null;
			try {
				imageOut = new FileOutputStream(new File(imageFilePathName));
				imageOut.write(imageArr[0]);
				imageOut.flush();
				logger.info("[" + this.sourceUrl + "][" + imageIndex + " / " + size + "][image download succ]");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (imageOut != null) {
					try {
						imageOut.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				imageIndex++;
			}
		}
	}
}