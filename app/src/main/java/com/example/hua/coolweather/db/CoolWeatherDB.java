package com.example.hua.coolweather.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.hua.coolweather.model.City;
import com.example.hua.coolweather.model.County;
import com.example.hua.coolweather.model.Province;
import com.example.hua.coolweather.until.LogUntil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fundmarkhua on 2016/10/17.
 * 封装常用数据库操作  定义成了一个单例类
 */

public class CoolWeatherDB {
    //数据库名字
    public static final String DB_NAME = "cool_weather";
    //数据库版本
    public static final int VERSION = 1;
    private static CoolWeatherDB coolWeatherDB;
    private SQLiteDatabase db;

    /**
     * 构造方式私有化
     */
    private CoolWeatherDB(Context context) {
        CoolWeatherOpenHelper dbHelper = new CoolWeatherOpenHelper(context,
                DB_NAME, null, VERSION);
        db = dbHelper.getWritableDatabase();
    }

    /**
     * 获取CoolWeatherDB的实例  可以保证全局范围内只会有一个CoolWeatherDB 的实例
     */
    public synchronized static CoolWeatherDB getInstance(Context context) {
        //双重锁定检查 保证线程安全 避免性能损耗
        if (coolWeatherDB == null) {
            synchronized (CoolWeatherDB.class) {
                if (coolWeatherDB == null) {
                    coolWeatherDB = new CoolWeatherDB(context);
                }
            }
        }


        return coolWeatherDB;
    }

    /**
     * province实例存储到数据库中
     */
    public void saveProvince(Province province) {
        if (province != null) {
            ContentValues values = new ContentValues();
            values.put("province_name", province.getProvinceName());
            values.put("province_code", province.getProvinceCode());
            db.insert("Province", null, values);
        }
    }

    /**
     * 从数据库获取全国省份信息
     */
    public List<Province> loadProvince() {
        List<Province> list = new ArrayList<>();
        Cursor cursor = db.query("Province", null, null, null, null, null, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    Province province = new Province();
                    province.setId(cursor.getInt(cursor.getColumnIndex("id")));
                    province.setProvinceName(cursor.getString(cursor.getColumnIndex("province_name")));
                    province.setProvinceCode(cursor.getString(cursor.getColumnIndex("province_code")));
                    list.add(province);
                }
                while (cursor.moveToNext());
            }
            cursor.close();//关闭cursor
        } catch (Exception e) {
            e.printStackTrace();
            LogUntil.w("CoolWeather",e.getMessage());
            if (cursor != null) {
                cursor.close();
            }
        }
        return list;
    }

    /**
     * 把city实例存储到数据库
     */
    public void saveCity(City city) {
        if (city != null) {
            ContentValues values = new ContentValues();
            values.put("city_name", city.getCityName());
            values.put("city_code", city.getCityCode());
            values.put("province_id", city.getProvinceId());
            db.insert("City", null, values);
        }
    }

    /**
     * 从数据库里获取某省下所有城市信息
     */
    public List<City> loadCities(int provinceId) {
        List<City> list = new ArrayList<>();
        Cursor cursor = db.query("City", null, "province_id=?", new String[]{String.valueOf(provinceId)}, null, null, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    City city = new City();
                    city.setId(cursor.getInt(cursor.getColumnIndex("id")));
                    city.setCityName(cursor.getString(cursor.getColumnIndex("city_name")));
                    city.setCityCode(cursor.getString(cursor.getColumnIndex("city_code")));
                    city.setProvinceId(provinceId);
                    list.add(city);
                }
                while (cursor.moveToNext());
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
            LogUntil.w("CoolWeather",e.getMessage());
            if (cursor != null) {
                cursor.close();
            }
        }
        return list;
    }

    /**
     * 把county实例存储到数据库
     */
    public void saveCounty(County county) {
        if (county != null) {
            ContentValues values = new ContentValues();
            values.put("county_name", county.getCountyName());
            values.put("county_code", county.getCountyCode());
            values.put("city_id", county.getCityId());
            db.insert("City", null, values);
        }
    }

    /**
     * 从数据库中获取某城市下所有区县信息
     */
    public List<County> loadCounties(int cityid) {
        List<County> list = new ArrayList<>();
        Cursor cursor = db.query("County", null, "city_id=?", new String[]{String.valueOf(cityid)}, null, null, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    County county = new County();
                    county.setId(cursor.getInt(cursor.getColumnIndex("id")));
                    county.setCountyName(cursor.getString(cursor.getColumnIndex("county_name")));
                    county.setCountyCode(cursor.getString(cursor.getColumnIndex("county_code")));
                    county.setCityId(cityid);
                    list.add(county);
                }
                while (cursor.moveToNext());
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
            LogUntil.w("CoolWeather",e.getMessage());
            if (cursor != null) {
                cursor.close();
            }
        }
        return list;
    }
}
