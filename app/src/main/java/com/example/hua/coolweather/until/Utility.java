package com.example.hua.coolweather.until;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import java.io.StringReader;
import java.text.SimpleDateFormat;

import com.example.hua.coolweather.db.CoolWeatherDB;
import com.example.hua.coolweather.model.City;
import com.example.hua.coolweather.model.County;
import com.example.hua.coolweather.model.Province;
import com.example.hua.coolweather.model.WeatherInfo;
import com.example.hua.coolweather.model.WeatherInfoMore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.util.Date;
import java.util.Locale;

/**
 * Created by fundmarkhua on 2016/10/18.
 * 解析和处理处理省市县区数据
 */

public class Utility {
    /**
     * 解析和处理省级数据
     */
    public synchronized static boolean handleProvincesResponse(CoolWeatherDB coolWeatherDB, String response) {
        if (!TextUtils.isEmpty(response)) {
            String[] allProvinces = response.split(",");
            if (allProvinces.length > 0) {
                for (String p : allProvinces) {
                    String[] array = p.split("\\|");
                    Province province = new Province();
                    province.setProvinceCode(array[0]);
                    province.setProvinceName(array[1]);
                    //解析出来的数据存储到数据库Province表
                    coolWeatherDB.saveProvince(province);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 解析和处理市级数据
     */
    public static boolean handleCitiesResponse(CoolWeatherDB coolWeatherDB, String response, int provinceId) {
        if (!TextUtils.isEmpty(response)) {
            String[] allCities = response.split(",");
            if (allCities.length > 0) {
                for (String c : allCities) {
                    String[] array = c.split("\\|");
                    City city = new City();
                    city.setCityCode(array[0]);
                    city.setCityName(array[1]);
                    city.setProvinceId(provinceId);
                    //解析出来的数据存储到数据库Province表
                    coolWeatherDB.saveCity(city);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 解析和处理县区数据
     */
    public static boolean handleCountiesResponse(CoolWeatherDB coolWeatherDB, String response, int cityId) {
        if (!TextUtils.isEmpty(response)) {
            String[] allCounties = response.split(",");
            if (allCounties.length > 0) {
                for (String c : allCounties) {
                    County county = new County();
                    String[] array = c.split("\\|");
                    county.setCountyCode(array[0]);
                    county.setCountyName(array[1]);
                    county.setCityId(cityId);
                    coolWeatherDB.saveCounty(county);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 解析服务器返回的JSON数据，并将解析出的数据存储到本地。
     */
    public static boolean handleWeatherResponse(Context context, String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            int status = jsonObject.getInt("status");
            if (status == 1000) {
                JSONObject weatherData = jsonObject.getJSONObject("data");
                JSONArray weatherArray = weatherData.getJSONArray("forecast");
                if (weatherArray.length() > 1) {
                    JSONObject weatherInfo = weatherArray.getJSONObject(0);
                    WeatherInfo weatherInfoObj = new WeatherInfo();
                    weatherInfoObj.setCityName(weatherData.getString("city"));
                    weatherInfoObj.setWeatherCode(weatherData.getString("city"));
                    String high = weatherInfo.getString("high");
                    String low = weatherInfo.getString("low");
                    weatherInfoObj.setTemp1(high.replace("高温", ""));
                    weatherInfoObj.setTemp2(low.replace("低温", ""));
                    weatherInfoObj.setWeatherDesp(weatherInfo.getString("type"));
                    weatherInfoObj.setPublishTime(weatherInfo.getString("date"));
                    saveWeatherInfo(context, weatherInfoObj);
                    return true;
                }
            }
            return false;

        } catch (JSONException e) {
            LogUntil.w("coolWeather", e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 解析服务器返回的XML数据，并将解析出的数据存储到本地。
     */
    public static boolean handleWeatherResponseXml(Context context, String response) {
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = factory.newPullParser();
            xmlPullParser.setInput(new StringReader(response));
            int eventType = xmlPullParser.getEventType();
            WeatherInfoMore weatherInfoMore = new WeatherInfoMore();
            int tagType = 1;//解析阶段 1,主数据 2,空气数据  3,五日天气  4,指数
            int fiveNode = 1; //五日天气 天数判断
            int zhishutype = 1;//天气指数判断
            boolean isDay = false;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String nodeName = xmlPullParser.getName();

                switch (eventType) {
                    //  开始解析某个结点
                    case XmlPullParser.START_TAG: {
                        //主数据
                        if (tagType == 1) {
                            if ("city".equals(nodeName)) {
                                weatherInfoMore.setCityName(xmlPullParser.nextText());
                            } else if ("updatetime".equals(nodeName)) {
                                weatherInfoMore.setPublishTime(xmlPullParser.nextText());
                            } else if ("wendu".equals(nodeName)) {
                                weatherInfoMore.setWendu(xmlPullParser.nextText());
                            } else if ("fengli".equals(nodeName)) {
                                weatherInfoMore.setFengli(xmlPullParser.nextText());
                            } else if ("shidu".equals(nodeName)) {
                                weatherInfoMore.setShidu(xmlPullParser.nextText());
                            } else if ("fengxiang".equals(nodeName)) {
                                weatherInfoMore.setFengxiang(xmlPullParser.nextText());
                            } else if ("sunrise_1".equals(nodeName)) {
                                weatherInfoMore.setSunRise(xmlPullParser.nextText());
                            } else if ("sunset_1".equals(nodeName)) {
                                weatherInfoMore.setSunSet(xmlPullParser.nextText());
                            }
                        }
                        //空气数据
                        if (tagType == 2) {
                            if ("aqi".equals(nodeName)) {
                                weatherInfoMore.setAqi(xmlPullParser.nextText());
                            } else if ("quality".equals(nodeName)) {
                                weatherInfoMore.setQuality(xmlPullParser.nextText());
                            }
                        }
                        //五日数据  天气情况读取白天的
                        if (tagType == 3) {
                            //第一天  今天
                            if (fiveNode == 1) {
                                //判断白天还是黑夜
                                if ("day".equals(nodeName)) {
                                    isDay = true;
                                } else if ("night".equals(nodeName)) {
                                    isDay = false;
                                }

                                if ("high".equals(nodeName)) {
                                    weatherInfoMore.setTemp1(xmlPullParser.nextText());
                                } else if ("low".equals(nodeName)) {
                                    weatherInfoMore.setTemp2(xmlPullParser.nextText());
                                } else if ("type".equals(nodeName) && isDay) {
                                    weatherInfoMore.setWeatherDesp(xmlPullParser.nextText());
                                }
                            }
                            //第二天 明天
                            if (fiveNode == 2) {
                                //判断白天还是黑夜
                                if ("day".equals(nodeName)) {
                                    isDay = true;
                                } else if ("night".equals(nodeName)) {
                                    isDay = false;
                                }

                                if ("high".equals(nodeName)) {
                                    weatherInfoMore.setTomtemp1(xmlPullParser.nextText());
                                } else if ("low".equals(nodeName)) {
                                    weatherInfoMore.setTomtemp2(xmlPullParser.nextText());
                                } else if ("type".equals(nodeName) && isDay) {
                                    weatherInfoMore.setTomWeatherDesp(xmlPullParser.nextText());
                                }
                            }
                        }
                        //各项指数
                        if (tagType == 4) {
                            if ("value".equals(nodeName) && zhishutype == 4) {
                                //感冒指数
                                weatherInfoMore.setGanmaoDesp(xmlPullParser.nextText());
                            } else if ("value".equals(nodeName) && zhishutype == 9) {
                                //运动指数
                                weatherInfoMore.setSportDesp(xmlPullParser.nextText());
                            }
                        }

                        break;
                    }
                    //  完成解析某个结点
                    case XmlPullParser.END_TAG: {
                        if ("sunset_2".equals(nodeName)) {
                            tagType = 2;//开始解析空气数据
                        } else if ("yesterday".equals(nodeName)) {
                            tagType = 3;//开始解析五日天气数据
                        } else if ("forecast".equals(nodeName)) {
                            tagType = 4;//开始解析指数数据
                        }
                        //五日天气日期切换判断
                        if ("weather".equals(nodeName) && tagType == 3) {
                            fiveNode++;
                        }
                        //指数类型判断
                        if ("zhishu".equals(nodeName) && tagType == 4) {
                            zhishutype++;
                        }
                        break;
                    }
                    default:
                        break;
                }
                eventType = xmlPullParser.next();
            }
            saveWeatherInfoXml(context, weatherInfoMore);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 将服务器返回的所有天气信息存储到SharedPreferences文件中。
     */
    private static void saveWeatherInfo(Context context, WeatherInfo weatherInfo) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年M月d日", Locale.CHINA);
        SimpleDateFormat hour = new SimpleDateFormat("M月d日 HH:mm", Locale.CHINA);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();

        editor.putBoolean("city_selected", true);
        editor.putString("city_name", weatherInfo.getCityName());
        editor.putString("weather_code", weatherInfo.getWeatherCode());
        editor.putString("temp1", weatherInfo.getTemp1());
        editor.putString("temp2", weatherInfo.getTemp2());
        editor.putString("weather_desp", weatherInfo.getWeatherDesp());
        editor.putString("publish_time", hour.format(new Date()));
        editor.putString("current_date", sdf.format(new Date()));
        editor.commit();
    }

    /**
     * 将服务器返回的XML格式所有天气信息存储到SharedPreferences文件中。
     */
    private static void saveWeatherInfoXml(Context context, WeatherInfoMore weatherInfoMore) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年M月d日", Locale.CHINA);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();

        editor.putBoolean("city_selected", true);
        editor.putString("city_name", weatherInfoMore.getCityName());
        editor.putString("wendu", weatherInfoMore.getWendu());
        editor.putString("fengxiang",weatherInfoMore.getFengxiang());
        editor.putString("fengli",weatherInfoMore.getFengli());
        editor.putString("shidu",weatherInfoMore.getShidu());
        editor.putString("aqi",weatherInfoMore.getAqi());
        editor.putString("quality",weatherInfoMore.getQuality());
        editor.putString("sunRise",weatherInfoMore.getSunRise());
        editor.putString("sunSet",weatherInfoMore.getSunSet());
        editor.putString("temp1", weatherInfoMore.getTemp1());
        editor.putString("temp2", weatherInfoMore.getTemp2());
        editor.putString("weather_desp", weatherInfoMore.getWeatherDesp());
        editor.putString("sportDesp", weatherInfoMore.getSportDesp());
        editor.putString("ganmaoDesp", weatherInfoMore.getGanmaoDesp());
        editor.putString("tomtemp1", weatherInfoMore.getTomtemp1());
        editor.putString("tomtemp2", weatherInfoMore.getTomtemp2());
        editor.putString("tomWeatherDesp", weatherInfoMore.getTomWeatherDesp());
        editor.putString("publish_time", weatherInfoMore.getPublishTime());
        editor.putString("current_date", sdf.format(new Date()));
        editor.commit();
    }

}
