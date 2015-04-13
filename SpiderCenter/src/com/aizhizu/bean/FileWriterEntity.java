package com.aizhizu.bean;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

public class FileWriterEntity {
	
	private String filePath;
	private OutputStreamWriter writer;
	
	public FileWriterEntity (String filePath, OutputStreamWriter writer) {
		this.filePath = filePath;
		this.writer = writer;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public OutputStreamWriter getWriter() {
		return writer;
	}

	public void setWriter(OutputStreamWriter writer) {
		this.writer = writer;
	}

	public String getDate() {
		String date = filePath.replaceAll("\\D+", "");
		return date;
	}

	public static void main(String[] args) {
		try {
			FileWriterEntity f =  new FileWriterEntity("/house-data/2015041317/web_anjuke", new OutputStreamWriter(new FileOutputStream("/home/leei/tool.sh")));
			System.out.println(f.getDate());
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	
}
