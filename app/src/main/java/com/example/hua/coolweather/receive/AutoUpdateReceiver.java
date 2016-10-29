package com.example.hua.coolweather.receive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.hua.coolweather.service.AutoUpdateService;

/**
 * Created by Administrator on 2016/10/28.
 * 广播接收器用于重启 天气更新服务
 */

public class AutoUpdateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, AutoUpdateService.class);
        context.startService(i);
    }
}
