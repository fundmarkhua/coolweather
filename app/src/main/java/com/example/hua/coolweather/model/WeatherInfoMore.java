package com.example.hua.coolweather.model;

/**
 * Created by fundmarkhua on 2016/11/3.
 * 详细天气数据的实体类
 */

public class WeatherInfoMore {
    private String cityName;
    private String wendu;
    private String fengxiang;
    private String fengli;
    private String shidu;
    //空气指数
    private String aqi;
    //空气质量描述
    private String quality;
    //日出
    private String sunRise;
    //日落
    private String sunSet;

    private String temp1;
    private String temp2;
    private String weatherDesp;
    //运动指数
    private String sportDesp;
    //感冒指数
    private String ganmaoDesp;

    //明日气温情况
    private String tomtemp1;
    private String tomtemp2;
    private String tomWeatherDesp;
    private String publishTime;

    public String getTomWeatherDesp() {
        return tomWeatherDesp;
    }

    public void setTomWeatherDesp(String tomWeatherDesp) {
        this.tomWeatherDesp = tomWeatherDesp;
    }

    public String getTomtemp2() {
        return tomtemp2;
    }

    public void setTomtemp2(String tomtemp2) {
        this.tomtemp2 = tomtemp2;
    }

    public String getTomtemp1() {
        return tomtemp1;
    }

    public void setTomtemp1(String tomtemp1) {
        this.tomtemp1 = tomtemp1;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getWendu() {
        return wendu;
    }

    public void setWendu(String wendu) {
        this.wendu = wendu;
    }

    public String getFengxiang() {
        return fengxiang;
    }

    public void setFengxiang(String fengxiang) {
        this.fengxiang = fengxiang;
    }

    public String getFengli() {
        return fengli;
    }

    public void setFengli(String fengli) {
        this.fengli = fengli;
    }

    public String getShidu() {
        return shidu;
    }

    public void setShidu(String shidu) {
        this.shidu = shidu;
    }

    public String getAqi() {
        return aqi;
    }

    public void setAqi(String aqi) {
        this.aqi = aqi;
    }

    public String getQuality() {
        return quality;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }

    public String getSunRise() {
        return sunRise;
    }

    public void setSunRise(String sunRise) {
        this.sunRise = sunRise;
    }

    public String getSunSet() {
        return sunSet;
    }

    public void setSunSet(String sunSet) {
        this.sunSet = sunSet;
    }

    public String getTemp1() {
        return temp1;
    }

    public void setTemp1(String temp1) {
        this.temp1 = temp1;
    }

    public String getTemp2() {
        return temp2;
    }

    public void setTemp2(String temp2) {
        this.temp2 = temp2;
    }

    public String getWeatherDesp() {
        return weatherDesp;
    }

    public void setWeatherDesp(String weatherDesp) {
        this.weatherDesp = weatherDesp;
    }

    public String getSportDesp() {
        return sportDesp;
    }

    public void setSportDesp(String sportDesp) {
        this.sportDesp = sportDesp;
    }

    public String getGanmaoDesp() {
        return ganmaoDesp;
    }

    public void setGanmaoDesp(String ganmaoDesp) {
        this.ganmaoDesp = ganmaoDesp;
    }

    public String getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(String publishTime) {
        this.publishTime = publishTime;
    }

}
