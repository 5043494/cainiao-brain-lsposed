package com.cainiao.brain.hook;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class CainiaoHook implements IXposedHookLoadPackage {
    private static final String TARGET = "com.cainiao.wireless";
    private static final Set<String> INSTALLED = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
    private static final ConcurrentHashMap<Integer, Long> RECENT = new ConcurrentHashMap<>();

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) {
        if (!TARGET.equals(lpparam.packageName)) return;
        XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                Context context = (Context) param.args[0];
                HookLog.init(context);
                registerConfigReceiver(context);
                HookLog.runtime("LSPOSED", "已注入进程 " + lpparam.processName + "，版本接口=" + XposedBridge.getXposedVersion());
                installHooks(lpparam.classLoader, lpparam.processName);
            }
        });
    }

    private static void registerConfigReceiver(Context context) {
        try {
            IntentFilter filter = new IntentFilter("com.cainiao.brain.CONFIG_CHANGED");
            BroadcastReceiver receiver = new BroadcastReceiver() {
                @Override public void onReceive(Context c, Intent i) {
                    HookLog.setEnabled(i.getBooleanExtra("enabled", true));
                }
            };
            if (Build.VERSION.SDK_INT >= 33) context.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED);
            else context.registerReceiver(receiver, filter);
        } catch (Throwable e) {
            HookLog.runtime("CONFIG", "配置广播注册失败：" + e);
        }
    }

    private static void installHooks(ClassLoader loader, String process) {
        String key = process + "@" + System.identityHashCode(loader);
        if (!INSTALLED.add(key)) return;
        int installed = 0;
        installed += hookMtopBusiness(loader);
        installed += hookNetworkRequest(loader);
        installed += hookMtopResponse(loader);
        HookLog.runtime("HOOK", "网络 Hook 安装完成，命中组件=" + installed + "，抓包=" + HookLog.isEnabled());
    }

    private static int hookMtopBusiness(ClassLoader loader) {
        int count = 0;
        String[] classes = {
                "com.taobao.tao.remotebusiness.MtopBusiness",
                "com.cainiao.wireless.network.CNMtopBusiness"
        };
        for (String name : classes) {
            try {
                Class<?> clazz = XposedHelpers.findClass(name, loader);
                XposedBridge.hookAllMethods(clazz, "startRequest", new XC_MethodHook() {
                    @Override protected void beforeHookedMethod(MethodHookParam param) {
                        if (!HookLog.isEnabled()) return;
                        try {
                            Object request = XposedHelpers.getObjectField(param.thisObject, "request");
                            String api = stringCall(request, "getApiName");
                            String version = stringCall(request, "getVersion");
                            String data = stringCall(request, "getData");
                            logDeduplicated("MTOP 请求", api + " v" + version + "\n参数: " + data);
                        } catch (Throwable e) {
                            HookLog.runtime("HOOK", "读取 MTOP 请求失败：" + e);
                        }
                    }
                });
                count++;
            } catch (Throwable e) {
                HookLog.runtime("HOOK", name + " 未命中：" + e.getClass().getSimpleName());
            }
        }
        return count;
    }

    private static int hookNetworkRequest(ClassLoader loader) {
        try {
            Class<?> builder = XposedHelpers.findClass("mtopsdk.network.domain.Request$a", loader);
            XposedBridge.hookAllMethods(builder, "cch", new XC_MethodHook() {
                @Override protected void afterHookedMethod(MethodHookParam param) {
                    if (param.getResult() != null) logDeduplicated("HTTP 请求", String.valueOf(param.getResult()));
                }
            });
            return 1;
        } catch (Throwable e) {
            HookLog.runtime("HOOK", "底层 Request Hook 未命中：" + e);
            return 0;
        }
    }

    private static int hookMtopResponse(ClassLoader loader) {
        try {
            Class<?> response = XposedHelpers.findClass("mtopsdk.mtop.domain.MtopResponse", loader);
            XposedHelpers.findAndHookMethod(response, "setBytedata", byte[].class, new XC_MethodHook() {
                @Override protected void beforeHookedMethod(MethodHookParam param) {
                    if (!HookLog.isEnabled()) return;
                    byte[] data = (byte[]) param.args[0];
                    if (data != null) HookLog.packet("MTOP 响应", new String(data, StandardCharsets.UTF_8));
                }
            });
            XposedHelpers.findAndHookMethod(response, "setHeaderFields", java.util.Map.class, new XC_MethodHook() {
                @Override protected void beforeHookedMethod(MethodHookParam param) {
                    if (HookLog.isEnabled()) HookLog.packet("响应头", String.valueOf(param.args[0]));
                }
            });
            return 1;
        } catch (Throwable e) {
            HookLog.runtime("HOOK", "MtopResponse Hook 未命中：" + e);
            return 0;
        }
    }

    private static String stringCall(Object object, String method) {
        if (object == null) return "null";
        try { return String.valueOf(XposedHelpers.callMethod(object, method)); }
        catch (Throwable ignored) { return String.valueOf(object); }
    }

    private static void logDeduplicated(String source, String message) {
        int hash = (source + message).hashCode();
        long now = System.currentTimeMillis();
        Long old = RECENT.put(hash, now);
        if (old == null || now - old > 1500) HookLog.packet(source, message);
        if (RECENT.size() > 500) RECENT.clear();
    }
}
