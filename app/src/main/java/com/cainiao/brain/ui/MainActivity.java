package com.cainiao.brain.ui;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.cainiao.brain.BuildConfig;
import com.cainiao.brain.R;
import com.cainiao.brain.data.LogStore;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.card_runtime).setOnClickListener(v -> openLog(LogStore.TYPE_RUNTIME, "全部运行日志"));
        findViewById(R.id.card_packet).setOnClickListener(v -> openLog(LogStore.TYPE_PACKET, "抓包日志"));
        findViewById(R.id.card_settings).setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
        ((TextView) findViewById(R.id.tv_version)).setText("菜鸟智脑 v" + BuildConfig.VERSION_NAME + "  ·  目标：菜鸟 " + targetVersion());
        LogStore.append(this, LogStore.TYPE_RUNTIME, "UI", "主页已打开");
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean enabled = LogStore.captureEnabled(this);
        String runtime = LogStore.read(this, LogStore.TYPE_RUNTIME);
        boolean injected = runtime.contains("网络 Hook 安装完成");
        TextView status = findViewById(R.id.tv_module_status);
        status.setText((injected ? "LSPosed 已注入" : "等待 LSPosed 注入") + " · 抓包" + (enabled ? "开启" : "关闭"));
    }

    private void openLog(String type, String title) {
        Intent intent = new Intent(this, LogActivity.class);
        intent.putExtra(LogActivity.EXTRA_TYPE, type);
        intent.putExtra(LogActivity.EXTRA_TITLE, title);
        startActivity(intent);
    }

    private String targetVersion() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo("com.cainiao.wireless", 0);
            return info.versionName == null ? "已安装" : info.versionName;
        } catch (Throwable ignored) {
            return "未安装";
        }
    }
}
