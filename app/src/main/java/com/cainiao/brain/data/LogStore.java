package com.cainiao.brain.data;

import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class LogStore {
    public static final String TYPE_RUNTIME = "runtime";
    public static final String TYPE_PACKET = "packet";
    private static final Object LOCK = new Object();
    private static final int MAX_PREVIEW_BYTES = 220_000;
    private static final long MAX_FILE_BYTES = 8L * 1024L * 1024L;

    private LogStore() {}

    private static File file(Context context, String type) {
        File dir = new File(context.getFilesDir(), "logs");
        if (!dir.exists()) dir.mkdirs();
        return new File(dir, TYPE_PACKET.equals(type) ? "packet.log" : "runtime.log");
    }

    public static void append(Context context, String type, String source, String message) {
        if (context == null || message == null) return;
        String stamp = new SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.CHINA).format(new Date());
        String line = "[" + stamp + "][" + (source == null ? "MODULE" : source) + "] " + message + "\n";
        synchronized (LOCK) {
            File target = file(context, type);
            boolean rotated = target.length() >= MAX_FILE_BYTES;
            try (FileOutputStream out = new FileOutputStream(target, !rotated)) {
                if (rotated) {
                    out.write(("[" + stamp + "][SYSTEM] 日志达到 8 MB，已自动轮转\n").getBytes(StandardCharsets.UTF_8));
                }
                out.write(line.getBytes(StandardCharsets.UTF_8));
            } catch (Throwable ignored) { }
        }
    }

    public static String read(Context context, String type) {
        File f = file(context, type);
        if (!f.exists()) return "暂无日志。\n请在 LSPosed 中启用模块并重新启动菜鸟。";
        synchronized (LOCK) {
            try (RandomAccessFile reader = new RandomAccessFile(f, "r")) {
                long length = reader.length();
                int count = (int) Math.min(length, MAX_PREVIEW_BYTES);
                long start = length - count;
                reader.seek(start);
                byte[] bytes = new byte[count];
                reader.readFully(bytes);
                String value = new String(bytes, StandardCharsets.UTF_8);
                if (start > 0) value = "…… 仅显示最新 " + (MAX_PREVIEW_BYTES / 1000) + " KB，完整内容请点“导出” ……\n" + value;
                return value.isEmpty() ? "暂无日志。" : value;
            } catch (Throwable e) {
                return "读取日志失败：" + e;
            }
        }
    }

    public static void export(Context context, String type, Uri destination) throws Exception {
        File source = file(context, type);
        synchronized (LOCK) {
            try (FileInputStream in = new FileInputStream(source);
                 OutputStream out = context.getContentResolver().openOutputStream(destination, "w")) {
                if (out == null) throw new IllegalStateException("无法打开导出文件");
                byte[] buffer = new byte[64 * 1024];
                int size;
                while ((size = in.read(buffer)) >= 0) out.write(buffer, 0, size);
            }
        }
    }

    public static void clear(Context context, String type) {
        synchronized (LOCK) {
            try (FileOutputStream ignored = new FileOutputStream(file(context, type), false)) { }
            catch (Throwable ignored) { }
        }
    }

    public static boolean captureEnabled(Context context) {
        return context.getSharedPreferences("settings", Context.MODE_PRIVATE)
                .getBoolean("capture_enabled", true);
    }

    public static void setCaptureEnabled(Context context, boolean enabled) {
        context.getSharedPreferences("settings", Context.MODE_PRIVATE)
                .edit().putBoolean("capture_enabled", enabled).apply();
        append(context, TYPE_RUNTIME, "SETTINGS", "菜鸟抓包已" + (enabled ? "开启" : "关闭"));
    }

    public static boolean pointsEnabled(Context context) {
        return context.getSharedPreferences("settings", Context.MODE_PRIVATE)
                .getBoolean("points_enabled", false);
    }

    public static void setPointsEnabled(Context context, boolean enabled) {
        context.getSharedPreferences("settings", Context.MODE_PRIVATE)
                .edit().putBoolean("points_enabled", enabled).apply();
        append(context, TYPE_RUNTIME, "SETTINGS", "赚裹酱流程识别已" + (enabled ? "开启" : "关闭"));
    }
}
