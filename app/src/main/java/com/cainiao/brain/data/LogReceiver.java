package com.cainiao.brain.data;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class LogReceiver extends BroadcastReceiver {
    public static final String ACTION_APPEND = "com.cainiao.brain.APPEND_LOG";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !ACTION_APPEND.equals(intent.getAction())) return;
        String type = intent.getStringExtra("type");
        String source = intent.getStringExtra("source");
        String message = intent.getStringExtra("message");
        LogStore.append(context, type, source, message);
    }
}
