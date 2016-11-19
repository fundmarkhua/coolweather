package com.example.hua.coolweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hua.coolweather.service.AutoUpdateService;
import com.example.hua.coolweather.until.HttpCallbackListener;
import com.example.hua.coolweather.until.HttpUtil;
import com.example.hua.coolweather.until.LogUntil;
import com.example.hua.coolweather.until.Utility;

import org.w3c.dom.Text;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

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
    private TextView textType;
    /**
     * 用于显示实时温度
     */
    private TextView textWendu;
    /**
     * 用于显示风力风向
     */
    private TextView textFengli;
    /**
     * 用于显示湿度
     */
    private TextView textShidu;
    /**
     * 用于显示空气指数
     */
    private TextView textAqi;
    /**
     * 用于显示空气质量
     */
    private TextView textQuality;
    /**
     * 用于显示运动指数
     */
    private TextView textSport;
    /**
     * 用于显示感冒指数
     */
    private TextView textGanmao;
    /**
     * 用于显示今日温度
     */
    private TextView texTodayWendu;
    /**
     * 用于显示今日天气
     */
    private TextView texTodayType;
    /**
     * 用于显示今日风力
     */
    private TextView texTodayFeng;
    /**
     * 用于显示明日温度
     */
    private TextView texTomWendu;
    /**
     * 用于显示明日天气
     */
    private TextView texTomType;
    /**
     * 用于显示明日风力
     */
    private TextView textTomFeng;


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
            cityNameText.setVisibility(View.INVISIBLE);
            queryWeatherInfo(countyCode,"city");
        } else {
            //没有区县代号就显示本地天气
            showWeather();
        }
        LogUntil.w("coolweather","3321");
    }

    /**
     * 初始化控件
     */
    private void InitView() {
        weatherInfoLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
        cityNameText = (TextView) findViewById(R.id.city_name);
        publishText = (TextView) findViewById(R.id.publish_text);
        textType = (TextView) findViewById(R.id.tex_type);
        textWendu = (TextView) findViewById(R.id.tex_wendu);
        textFengli = (TextView) findViewById(R.id.tex_fengli);
        textShidu = (TextView) findViewById(R.id.tex_shidu);
        textAqi = (TextView) findViewById(R.id.tex_aqi);
        textQuality = (TextView) findViewById(R.id.tex_quality);
        textSport = (TextView) findViewById(R.id.tex_sport);
        textGanmao = (TextView) findViewById(R.id.tex_ganmao);
        texTodayWendu = (TextView) findViewById(R.id.tex_today_wendu);
        texTodayType = (TextView) findViewById(R.id.tex_today_type);
        texTodayFeng = (TextView) findViewById(R.id.tex_today_feng);
        texTomWendu = (TextView) findViewById(R.id.tex_tom_wendu);
        texTomType = (TextView) findViewById(R.id.tex_tom_type);
        textTomFeng = (TextView) findViewById(R.id.tex_tom_feng);


    }

    /**
     * 按钮点击事件
     */
    public void clickMethod(View v) {
        switch (v.getId()) {
            case R.id.switch_city:
                Intent intent = new Intent(this, ChooseCityActivity.class);
                intent.putExtra("from_weather_activity", true);
                startActivity(intent);
                finish();
                break;
            case R.id.refresh_weather:
                publishText.setText("同步中...");
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                String weatherCode = prefs.getString("city_name", "");
                if (!TextUtils.isEmpty(weatherCode)) {
                    queryWeatherInfo(weatherCode, "city");
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
    private void queryWeatherInfo(String weatherCode, String type) {
        if ("city".equals(type)) {
            try {
                weatherCode = URLEncoder.encode(weatherCode, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        //String address = "http://wthrcdn.etouch.cn/weather_mini?" + type + "=" + weatherCode;
        String address = "http://wthrcdn.etouch.cn/WeatherApi?" +type+ "=" + weatherCode;
        LogUntil.w("coolweather", address);
        //queryFromServer(address, "weatherCode");
        queryFromServer(address, "weatherCodeXml");
    }

    /**
     * 根据传入的代码和类型 去服务器查询天气代号或者天气信息
     */
    private void queryFromServer(final String address, final String type) {
        try {

            HttpUtil.sendOkHttpRequest(address, new HttpCallbackListener() {
                        @Override
                        public void onFinish(String response) {

                            if ("countyCode".equals(type)) {
                                //从服务器解析天气代号
                                String[] array = response.split("\\|");
                                if (array != null && array.length == 2) {
                                    String weatherCode = array[1];
                                    queryWeatherInfo(weatherCode, "citykey");
                                }
                            } else if ("weatherCode".equals(type)) {
                                if (Utility.handleWeatherResponse(WeatherActivity.this, response)) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            showWeather();
                                        }
                                    });
                                } else {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(WeatherActivity.this, "当前查询城市暂时无数据", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    showWeather();
                                }

                            }
                            //测试
                            else if ("weatherCodeXml".equals(type)) {
                                if (Utility.handleWeatherResponseXml(WeatherActivity.this, response)) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            showWeather();
                                        }
                                    });
                                } else {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(WeatherActivity.this, "当前查询城市暂时无数据", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    showWeather();
                                }

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
        } catch (Exception e) {
            LogUntil.w("coolweather", e.getMessage());
        }
    }

    /**
     * 显示本地天气到界面 读取SharedPreferences
     */

    private void showWeather() {
        try {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            if (preferences == null) {
                queryWeatherInfo("101010100", "citykey");
            }
            try {
                cityNameText.setText(preferences.getString("city_name", ""));
                textType.setText(preferences.getString("weather_desp", ""));
                publishText.setText(String.valueOf(preferences.getString("publish_time", "") + " 更新"));
                textWendu.setText(preferences.getString("温度", ""));
                textFengli.setText(String.valueOf(preferences.getString("fengxiang", "") + preferences.getString("fengli", "")));
                textShidu.setText(preferences.getString("shidu", ""));
                textAqi.setText(preferences.getString("aqi", ""));
                textQuality.setText(preferences.getString("quality", ""));
                textSport.setText(preferences.getString("sportDesp", ""));
                textGanmao.setText(preferences.getString("ganmaoDesp", ""));
                texTodayWendu.setText(String.valueOf(preferences.getString("temp1", "") + preferences.getString("temp2", "")));
                texTodayType.setText(preferences.getString("weather_desp", ""));
                texTodayFeng.setText("");
                textTomFeng.setText("");
                texTomType.setText(preferences.getString("tomWeatherDesp", ""));
                texTomWendu.setText(String.valueOf(preferences.getString("tomtemp1", "") + preferences.getString("tomtemp2", "")));

            } catch (Exception e) {
                e.printStackTrace();
            }

            weatherInfoLayout.setVisibility(View.VISIBLE);
            cityNameText.setVisibility(View.VISIBLE);
            Intent intent = new Intent(this, AutoUpdateService.class);
            startService(intent);
        } catch (Exception e) {
            LogUntil.w("coolweather", e.getMessage());
        }
    }
}
