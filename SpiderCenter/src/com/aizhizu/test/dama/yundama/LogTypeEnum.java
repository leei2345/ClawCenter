package com.aizhizu.test.dama.yundama;

/**
 * 是否记录日志
 * 
 * @author ryan
 * 
 */
public enum LogTypeEnum {

	YES(1), NO(0);

	private int type;

	private LogTypeEnum(int type) {
		this.type = type;
	}

	public int getType() {
		return type;
	}

}
