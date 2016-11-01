package com.example.hua.coolweather.until;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.widget.Toast;

import java.text.SimpleDateFormat;

import com.example.hua.coolweather.db.CoolWeatherDB;
import com.example.hua.coolweather.model.City;
import com.example.hua.coolweather.model.County;
import com.example.hua.coolweather.model.Province;
import com.example.hua.coolweather.model.WeatherInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
     * 将服务器返回的所有天气信息存储到SharedPreferences文件中。
     */
    public static void saveWeatherInfo(Context context, WeatherInfo weatherInfo) {
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

}
