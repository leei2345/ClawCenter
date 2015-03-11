package com.aizhizu.bean;


public class HouseQiuzuEntity extends BaseHouseEntity {
	private static final long serialVersionUID = 6829971935795223724L;
	private String phone = "";
	private String phoneImageUrl;
	private String landlord = "房东";
	private int gender = 1;
	private String title;
	private int rentalType;
	private String price;
	private String city = "北京";
	private String district;
	private String area;
	private String circle;
	private String x;
	private String y;
	private String format;
	private String word = "期望遇到好房东，我会尽快入住";

	public String getPhone() {
		return this.phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getPhoneImageUrl() {
		return this.phoneImageUrl;
	}

	public void setPhoneImageUrl(String phoneImageUrl) {
		this.phoneImageUrl = phoneImageUrl;
	}

	public String getLandlord() {
		return this.landlord;
	}

	public void setLandlord(String landlord) {
		this.landlord = landlord;
	}

	public int getGender() {
		return this.gender;
	}

	public void setGender(int gender) {
		this.gender = gender;
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getRentalType() {
		return this.rentalType;
	}

	public void setRentalType(int rentalType) {
		this.rentalType = rentalType;
	}

	public String getPrice() {
		return this.price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public String getCity() {
		return this.city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getDistrict() {
		return this.district;
	}

	public void setDistrict(String district) {
		this.district = district;
	}

	public String getArea() {
		return this.area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	public String getCircle() {
		return this.circle;
	}

	public void setCircle(String circle) {
		this.circle = circle;
	}

	public String getX() {
		return this.x;
	}

	public void setX(String x) {
		this.x = x;
	}

	public String getY() {
		return this.y;
	}

	public void setY(String y) {
		this.y = y;
	}

	public String getFormat() {
		return this.format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public String getWord() {
		return this.word = "\"" + word  + "\"";
	}

	public void setWord(String word) {
		this.word = word;
	}

	public String toString() {
		String returnStr = getNum() + "," + getUrl() + "," + getPhone() + ","
				+ getPhoneImageUrl() + "," + getLandlord() + "," + getGender()
				+ ",\"" + getTitle() + "\"," + getRentalType() + ","
				+ getPrice() + "," + getCity() + ",\"" + getDistrict() + "\","
				+ getArea() + "," + getCircle() + ",\"" + getX() + ";" + getY()
				+ "\",'" + getFormat() + "," + getWord();
		return returnStr;
	}
}