package com.example.saydaliyati.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.saydaliyati.Utils.NotificationUtils;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra("title");
        String content = intent.getStringExtra("content");

        if (title != null && content != null) {
            NotificationUtils.showNotification(context, title, content);
        }
    }
}