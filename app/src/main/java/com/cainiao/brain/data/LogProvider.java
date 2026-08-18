package com.cainiao.brain.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

public class LogProvider extends ContentProvider {
    @Override
    public boolean onCreate() {
        if (getContext() != null) LogStore.append(getContext(), LogStore.TYPE_RUNTIME, "MODULE", "菜鸟智脑日志服务已启动");
        return true;
    }

    @Override
    public Bundle call(String method, String arg, Bundle extras) {
        Bundle result = new Bundle();
        if (getContext() == null) return result;
        if ("capture_enabled".equals(method)) {
            result.putBoolean("enabled", LogStore.captureEnabled(getContext()));
        } else if ("append".equals(method) && extras != null) {
            LogStore.append(getContext(), extras.getString("type"), extras.getString("source"), extras.getString("message"));
            result.putBoolean("ok", true);
        }
        return result;
    }

    @Override public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) { return null; }
    @Override public String getType(Uri uri) { return null; }
    @Override public Uri insert(Uri uri, ContentValues values) { return null; }
    @Override public int delete(Uri uri, String selection, String[] selectionArgs) { return 0; }
    @Override public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) { return 0; }
}
