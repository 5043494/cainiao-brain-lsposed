package com.cainiao.brain.hook;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import de.robv.android.xposed.XposedBridge;
import android.app.AndroidAppHelper;

final class HookLog {
    private static final int CHUNK = 46_000;
    private static final Uri PROVIDER = Uri.parse("content://com.cainiao.brain.logs");
    private static volatile Context context;
    private static volatile boolean enabled = true;
    private static volatile boolean pointsEnabled;
    private static volatile long lastConfigRead;

    private HookLog() {}

    static void init(Context value) {
        Context app = value == null ? null : value.getApplicationContext();
        context = app != null ? app : value;
        refreshEnabled(true);
    }

    static void setEnabled(boolean value) {
        enabled = value;
        runtime("CONFIG", "菜鸟抓包实时切换为：" + value);
    }

    static void setPointsEnabled(boolean value) {
        pointsEnabled = value;
        runtime("积分模块", "赚裹酱流程识别实时切换为：" + value);
    }

    static boolean isEnabled() {
        refreshEnabled(false);
        return enabled;
    }

    static boolean isPointsEnabled() {
        refreshEnabled(false);
        return pointsEnabled;
    }

    private static void refreshEnabled(boolean force) {
        Context c = getContext();
        long now = System.currentTimeMillis();
        if (c == null || (!force && now - lastConfigRead < 5000)) return;
        lastConfigRead = now;
        try {
            Bundle result = c.getContentResolver().call(PROVIDER, "capture_enabled", null, null);
            if (result != null) enabled = result.getBoolean("enabled", true);
            Bundle points = c.getContentResolver().call(PROVIDER, "points_enabled", null, null);
            if (points != null) pointsEnabled = points.getBoolean("enabled", false);
        } catch (Throwable e) {
            XposedBridge.log("菜鸟智脑：读取配置失败，沿用当前值 - " + e);
        }
    }

    static void runtime(String source, String message) { send("runtime", source, message); }
    static void packet(String source, String message) { if (isEnabled()) send("packet", source, message); }
    static void points(String source, String message) { if (isPointsEnabled()) send("runtime", source, message); }

    private static void send(String type, String source, String message) {
        Context c = getContext();
        if (c == null || message == null) {
            XposedBridge.log("菜鸟智脑[" + source + "]: " + message);
            return;
        }
        try {
            int count = Math.max(1, (message.length() + CHUNK - 1) / CHUNK);
            for (int i = 0; i < count; i++) {
                int from = i * CHUNK;
                int to = Math.min(message.length(), from + CHUNK);
                String part = message.substring(from, to);
                if (count > 1) part = "[分片 " + (i + 1) + "/" + count + "] " + part;
                Intent intent = new Intent("com.cainiao.brain.APPEND_LOG");
                intent.setComponent(new ComponentName("com.cainiao.brain", "com.cainiao.brain.data.LogReceiver"));
                intent.putExtra("type", type);
                intent.putExtra("source", source);
                intent.putExtra("message", part);
                c.sendBroadcast(intent);
            }
        } catch (Throwable e) {
            XposedBridge.log("菜鸟智脑：写日志失败 " + e);
        }
    }

    private static Context getContext() {
        Context c = context;
        if (c != null) return c;
        try {
            Context current = AndroidAppHelper.currentApplication();
            if (current != null) {
                Context app = current.getApplicationContext();
                context = app != null ? app : current;
            }
        } catch (Throwable ignored) { }
        return context;
    }
}
