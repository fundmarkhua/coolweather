package com.example.hua.coolweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.hua.coolweather.until.HttpCallbackListener;
import com.example.hua.coolweather.until.HttpUtil;
import com.example.hua.coolweather.until.LogUntil;
import com.example.hua.coolweather.until.Utility;

public class WeatherActivity extends AppCompatActivity {
    private LinearLayout weatherInfoLayout;
    /**
     * 用于显示城市名
     */
    private TextView cityNameText;
    /**
     * 用于显示发布时间
     */
    private TextView publishText;
    /**
     * 用于显示天气描述
     */
    private TextView weatherDespText;
    /**
     * 用于显示气温1
     */
    private TextView temp1Text;
    /**
     * 用于显示气温2
     */
    private TextView temp2Text;
    /**
     * 用于显示当前日期
     */
    private TextView currentDateText;
    /**
     * 切换城市按钮
     */
    private Button switchCity;
    /**
     * 更新天气按钮
     */
    private Button refreshWeather;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_layout);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        InitView();
        String countyCode = getIntent().getStringExtra("county_code");
        if (!TextUtils.isEmpty(countyCode)) {
            //有区县代号就查询天气
            publishText.setText("同步中...");
            weatherInfoLayout.setVisibility(View.INVISIBLE);
            //cityNameText.setVisibility(View.INVISIBLE);
            queryWeatherCode(countyCode);
        } else {
            //没有区县代号就显示本地天气
            showWeather();
        }
    }

    /**
     * 初始化控件
     */
    private void InitView() {
        weatherInfoLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
        cityNameText = (TextView) findViewById(R.id.city_name);
        publishText = (TextView) findViewById(R.id.publish_text);
        weatherDespText = (TextView) findViewById(R.id.weather_desp);
        temp1Text = (TextView) findViewById(R.id.temp1);
        temp2Text = (TextView) findViewById(R.id.temp2);
        currentDateText = (TextView) findViewById(R.id.current_date);
        switchCity = (Button) findViewById(R.id.switch_city);
        refreshWeather = (Button) findViewById(R.id.refresh_weather);
    }

    /**
     * 按钮点击事件
     */
    public void clickMethod(View v) {
        switch (v.getId()) {
            case R.id.switch_city:
                Intent intent = new Intent(this, ChooseAreaActivity.class);
                intent.putExtra("from_weather_activity", true);
                startActivity(intent);
                finish();
                break;
            case R.id.refresh_weather:
                publishText.setText("同步中...");
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                String weatherCode = prefs.getString("weather_code", "");
                if (!TextUtils.isEmpty(weatherCode)) {
                    queryWeatherInfo(weatherCode);
                }
                break;
            default:
                break;
        }
    }


    /**
     * 查询县级代号所对应的天气代码
     */
    private void queryWeatherCode(String countyCode) {
        String address = "http://www.weather.com.cn/data/list3/city" + countyCode + ".xml";
        queryFromServer(address, "countyCode");
    }

    /**
     * 查询天气代号所对应的天气
     */
    private void queryWeatherInfo(String weatherCode) {
        String address = "http://www.weather.com.cn/data/cityinfo/" + weatherCode + ".html";
        queryFromServer(address, "weatherCode");
    }

    /**
     * 根据传入的代码和类型 去服务器查询天气代号或者天气信息
     */
    private void queryFromServer(final String address, final String type) {
        try{

        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
                    @Override
                    public void onFinish(String response) {

                        if ("countyCode".equals(type)) {
                            //从服务器解析天气代号
                            String[] array = response.split("\\|");
                            if (array != null && array.length == 2) {
                                String weatherCode = array[1];
                                queryWeatherInfo(weatherCode);
                            }
                        } else if ("weatherCode".equals(type)) {
                            Utility.handleWeatherResponse(WeatherActivity.this, response);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showWeather();
                                }
                            });
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                publishText.setText("同步失败");
                            }
                        });
                    }
                }

        );
        }
        catch(Exception e){
            LogUntil.w("coolweather",e.getMessage());
        }
    }

    /**
     * 显示本地天气到界面 读取SharedPreferences
     */

    private void showWeather() {
        try {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            String city_name = preferences.getString("city_name", "");
            LogUntil.w("coolweather", "city_name is" + city_name);
            cityNameText.setText(city_name);
            temp1Text.setText(preferences.getString("temp1", ""));
            temp2Text.setText(preferences.getString("temp2", ""));
            weatherDespText.setText(preferences.getString("weather_desp", ""));
            publishText.setText("今天" + preferences.getString("publish_time", "") + "发布");
            currentDateText.setText(preferences.getString("current_date", ""));
            cityNameText.setText(city_name);
            weatherInfoLayout.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            LogUntil.w("coolweather", e.getMessage());
        }
    }
}
