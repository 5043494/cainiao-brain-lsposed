package com.cainiao.brain.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.cainiao.brain.R;
import com.cainiao.brain.data.LogStore;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class LogActivity extends AppCompatActivity {
    public static final String EXTRA_TYPE = "type";
    public static final String EXTRA_TITLE = "title";
    private String type;
    private TextView logView;
    private ScrollView scrollView;
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();
    private final AtomicInteger refreshGeneration = new AtomicInteger();
    private static final int REQUEST_EXPORT = 1201;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);
        type = getIntent().getStringExtra(EXTRA_TYPE);
        if (!LogStore.TYPE_PACKET.equals(type)) type = LogStore.TYPE_RUNTIME;

        ((TextView) findViewById(R.id.tv_title)).setText(getIntent().getStringExtra(EXTRA_TITLE));
        logView = findViewById(R.id.tv_log);
        scrollView = findViewById(R.id.log_scroll);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_refresh).setOnClickListener(v -> refresh());
        findViewById(R.id.btn_export).setOnClickListener(v -> chooseExportFile());
        findViewById(R.id.btn_clear).setOnClickListener(v -> {
            LogStore.clear(this, type);
            refresh();
        });
        refresh();
    }

    private void refresh() {
        final int generation = refreshGeneration.incrementAndGet();
        logView.setText("正在加载最新日志…");
        ioExecutor.execute(() -> {
            String content = LogStore.read(getApplicationContext(), type);
            runOnUiThread(() -> {
                if (isFinishing() || generation != refreshGeneration.get()) return;
                logView.setText(content);
                scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
            });
        });
    }

    private void chooseExportFile() {
        String stamp = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.CHINA).format(new Date());
        String prefix = LogStore.TYPE_PACKET.equals(type) ? "菜鸟抓包日志-" : "菜鸟运行日志-";
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TITLE, prefix + stamp + ".txt");
        startActivityForResult(intent, REQUEST_EXPORT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_EXPORT || resultCode != Activity.RESULT_OK || data == null) return;
        Uri destination = data.getData();
        if (destination == null) return;
        Toast.makeText(this, "正在导出完整日志…", Toast.LENGTH_SHORT).show();
        ioExecutor.execute(() -> {
            try {
                LogStore.export(getApplicationContext(), type, destination);
                runOnUiThread(() -> Toast.makeText(this, "日志导出成功", Toast.LENGTH_SHORT).show());
            } catch (Throwable e) {
                runOnUiThread(() -> Toast.makeText(this, "导出失败：" + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    @Override
    protected void onDestroy() {
        refreshGeneration.incrementAndGet();
        ioExecutor.shutdownNow();
        super.onDestroy();
    }
}
