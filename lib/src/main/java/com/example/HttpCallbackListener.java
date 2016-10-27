package com.example;

/**
 * Created by fundmarkhua on 2016/10/18.
 * 回调接口
 */

public interface HttpCallbackListener {
    void onFinish(String response);
    void onError(Exception e);
}
