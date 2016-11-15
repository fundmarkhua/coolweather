package com.example.hua.coolweather.model;

public class CityInfo {
	public String name;
	public String pinyi;

	public CityInfo(String name, String pinyi) {
		super();
		this.name = name;
		this.pinyi = pinyi;
	}

	public CityInfo() {
		super();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPinyi() {
		return pinyi;
	}

	public void setPinyi(String pinyi) {
		this.pinyi = pinyi;
	}

}
