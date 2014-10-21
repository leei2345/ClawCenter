package com.aizhizu.dao;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.commons.io.IOUtils;

public class FileDataWriter extends DataWriter {

	private String fileName;
	private String charset = "utf-8";
	protected BufferedWriter bw;

	public FileDataWriter(String fileName) {
		this.fileName = fileName;
	}

	public FileDataWriter(String fileName, String charset) {
		this.fileName = fileName;
		this.charset = charset;
	}

	@Override
	protected void open() {
		try {
			bw = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(fileName), charset));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void close() {
		IOUtils.closeQuietly(bw);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected void batchWrite(List l) {
		List<String> ls = (List<String>) l;

		try {
			for (String str : ls) {
				bw.write(str + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return;
	}

	@Override
	protected void singleWrite(Object o) {
		String str = (String) o;
		try {
			bw.write(str + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return;
	}
}
