package com.aizhizu.dao;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

public class FileDataReader extends DataReader {
	// 读文件
	private String fileName;
	private String charset = "utf-8";

	public FileDataReader(String fileName) {
		this.fileName = fileName;
	}

	public FileDataReader(String fileName, String charset) {
		this.fileName = fileName;
		this.charset = charset;
	}

	protected BufferedReader br;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected List batchRead(int batchCount) {
		List l = new ArrayList();

		String t = null;
		int count = 0;

		while (true) {
			try {

				if (count >= batchCount) {
					break;
				}
				t = br.readLine();

				// 结束
				if (t == null) {
					break;
				}

				// 处理
				l.add(t);

				count++;

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return l;
	}

	@Override
	protected Object singleRead() {
		String t = null;
		try {
			t = br.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return t;
	}

	@Override
	protected void open() {
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(
					fileName), charset));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void close() {
		IOUtils.closeQuietly(br);
	}

	@SuppressWarnings("rawtypes")
	protected List allRead() {
		List<String> all = null;
		try {
			all = IOUtils.readLines(br);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return all;
	}
}
