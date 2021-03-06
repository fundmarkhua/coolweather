package com.example;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by fundmarkhua on 2016/10/18.
 * 访问网络的工具类
 */

public class HttpUtil {
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

                    BufferedReader reader1 = new BufferedReader(new InputStreamReader(in, "UTF-8"),8);
                    StringBuilder response = new StringBuilder();
                    String line;
                    try {
                    //    byte[] b = new byte[4096];
                    //    for (int n; (n = in.read(b)) != -1; ) {
                    //        response.append(new String(b, 0, n));
                    //    }
                                  while ((line = reader1.readLine()) != null) {
                                      response.append(line);
                                 }
                        LogUntil.w("coolweather", "1");

                    } catch (Exception e) {
                        LogUntil.w("coolweather", "2");
                        LogUntil.w("coolweather", e.toString());
                    }
                    if (listener != null) {
                        //回调onFinish()方法
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
    }
}
