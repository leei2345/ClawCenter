package com.aizhizu.service.house.ganji;

public enum UserStat {
	
	Normal(0, "正常"), UseLess(1, "超限"), PasswdError(2, "密码错误"), Other(3, "其他状态"), OnUse(4, "正在使用");

	private String status;
	private int statusCode;
	private UserStat (int statCode, String stat) {
		this.status = stat;
		this.statusCode = statCode;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public int getStatusCode() {
		return statusCode;
	}
	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

}
