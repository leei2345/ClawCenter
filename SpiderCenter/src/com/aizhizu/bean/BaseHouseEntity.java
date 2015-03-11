package com.aizhizu.bean;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * 房型实体
 * @author leei
 *
 */
public class BaseHouseEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	protected String url;
	protected int num;
	protected Set<String> imageUrlList = new HashSet<String>();
	protected boolean push = false;

	public boolean isPush() {
		return push;
	}

	public void setPush(boolean push) {
		this.push = push;
	}

	public String getUrl() {
		return this.url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getNum() {
		return this.num;
	}

	public void setNum(int num) {
		this.num = num;
	}

	public Set<String> getImageUrlList() {
		return this.imageUrlList;
	}

	public void setImageUrlList(Set<String> imageUrlList) {
		this.imageUrlList = imageUrlList;
	}
}