package com.aizhizu.bean;

/**
 * 调度器实体
 * @author leei
 */
import com.alibaba.fastjson.JSONObject;
import java.util.List;

public class ScheduledEntity {
	private String identidy;
	private int status;
	private List<String> conf;

	public String getIdentidy() {
		return this.identidy;
	}

	public void setIdentidy(String identidy) {
		this.identidy = identidy;
	}

	public int getStatus() {
		return this.status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public List<String> getConf() {
		return this.conf;
	}

	public void setConf(List<String> conf) {
		this.conf = conf;
	}

	public String toString() {
		JSONObject obj = (JSONObject) JSONObject.toJSON(this);
		return obj.toJSONString();
	}
}