package com.example.hua.coolweather.until;


import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by fundmarkhua on 2016/10/18.
 * 访问网络的工具类 使用OkHttp第三方库
 */

public class OKHttpUtil {
    public static final MediaType MEDIA_TYPE_MARKDOWN = MediaType.parse("text/x-markdown; charset=utf-8");
    public static void sendHttpRequest(final String address, final HttpCallbackListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String postBody = ""
                        + "Releases\n"
                        + "--------\n"
                        + "\n"
                        + " * _1.0_ May 6, 2013\n"
                        + " * _1.1_ June 15, 2013\n"
                        + " * _1.2_ August 11, 2013\n";
                HttpURLConnection connection = null;
                try {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(address)
                            .get()
                            .build();
                    Response response = client.newCall(request).execute();
                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                    InputStream in = response.body().byteStream();
                    String responseStr = "";
                    StringBuilder responseBulid = new StringBuilder();
                    try {
                        byte[] b = new byte[4096];
                        for (int n; (n = in.read(b)) != -1; ) {
                            responseBulid.append(new String(b, 0, n));
                        }
                        responseStr = responseBulid.toString();
                    }
                    catch (Exception e){
                        e.printStackTrace();
                        LogUntil.w("coolweather","output err "+e.getMessage());
                    }
                    finally {
                        LogUntil.w("coolweather","output responseStr "+responseStr);
                    }
                    if (listener != null) {
                        //回调onFinish()方法
                        listener.onFinish(responseStr);
                    }
                } catch (Exception e) {
                    //回调onError()方法
                    e.printStackTrace();
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
