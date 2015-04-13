package com.aizhizu.bean;

import java.io.OutputStreamWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileWriterEntity {
	
	private String filePath;
	private OutputStreamWriter writer;
	private String date;
	private static Pattern pattern = Pattern.compile("/.*?/(\\d+)/.*");
	
	public FileWriterEntity (String filePath, OutputStreamWriter writer) {
		this.filePath = filePath;
		this.writer = writer;
		Matcher matcher = pattern.matcher(filePath);
		if (matcher.find()) {
			this.date = matcher.group();
		}
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
		return date;
	}

}
