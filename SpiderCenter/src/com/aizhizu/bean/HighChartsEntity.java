package com.aizhizu.bean;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 图形显示
 * @author leei
 *
 */
public class HighChartsEntity implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private List<String> categories = new ArrayList<String>();
	private List<Map<String, Object>> series;

	public List<String> getCategories() {
		return this.categories;
	}

	public void setCategories(List<String> categories) {
		this.categories = categories;
	}

	public List<Map<String, Object>> getSeries() {
		return this.series;
	}

	public void setSeries(List<Map<String, Object>> series) {
		this.series = series;
	}

	public String toString() {
		String r = JSON.toJSONString(this, new SerializerFeature[] {
				SerializerFeature.DisableCircularReferenceDetect,
				SerializerFeature.WriteMapNullValue });
		return r;
	}

	public float roundHalfAndUp(float source) {
		BigDecimal big = new BigDecimal(source);
		float res = big.setScale(2, 4).floatValue();
		return res;
	}

	public static void main(String[] args) {
	}
}