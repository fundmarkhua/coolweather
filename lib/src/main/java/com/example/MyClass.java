package com.example;

public class MyClass {
    public static void main(String[] args) {
        LogUntil.w("", "hello world11");
        String address = "http://www.weather.com.cn/data/cityinfo/101050705.html";
       // String address = "http://www.weather.com.cn/data/cityinfo/101050303.html";
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
                    @Override
                    public void onFinish(String response) {
                        LogUntil.w("", response);
                    }
                    @Override
                    public void onError(Exception e) {

                    }
                }

        );
    }
}