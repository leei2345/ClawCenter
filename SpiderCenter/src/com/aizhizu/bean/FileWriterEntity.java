package com.aizhizu.bean;

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

}
