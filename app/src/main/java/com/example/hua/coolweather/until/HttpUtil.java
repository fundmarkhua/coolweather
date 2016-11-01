package com.example.hua.coolweather.until;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by fundmarkhua on 2016/10/18.
 * 访问网络的工具类
 */

public class HttpUtil {
    public static void sendOkHttpRequest(final String address, final HttpCallbackListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(address)
                            .get()
                            .build();
                    Response response = client.newCall(request).execute();
                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                    String result = response.body().string();
                    if (listener != null) {
                        //回调onFinish()方法
                        LogUntil.w("coolweather", "233 "+result);
                        listener.onFinish(result);
                    }
                } catch (Exception e) {
                    LogUntil.w("coolweather", e.getMessage());
                    //回调onError()方法
                    listener.onError(e);
                } finally {
                    if (connection != null) {
                        //关闭连接
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }
    /**
    public static void sendHttpRequest(final String address, final HttpCallbackListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(address);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    connection.setRequestProperty("Accept-Charset", "utf-8");
                    InputStream in = connection.getInputStream();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String line;
                    try {
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }

                    } catch (Exception e) {
                        LogUntil.w("coolweather", address+" out put error"+e.toString());
                        e.printStackTrace();
                    }

                    if (listener != null) {
                        //回调onFinish()方法
                        LogUntil.w("coolweather", "233 "+response.toString());
                        listener.onFinish(response.toString());
                    }
                } catch (Exception e) {
                    LogUntil.w("coolweather", e.getMessage());
                    //回调onError()方法
                    listener.onError(e);
                } finally {
                    if (connection != null) {
                        //关闭连接
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }*/

}
