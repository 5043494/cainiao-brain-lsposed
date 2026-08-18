package com.cainiao.brain.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

import com.cainiao.brain.R;
import com.cainiao.brain.data.LogStore;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

public class SettingsActivity extends AppCompatActivity {
    private SwitchCompat captureSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        captureSwitch = findViewById(R.id.switch_capture);
        captureSwitch.setChecked(LogStore.captureEnabled(this));
        captureSwitch.setOnCheckedChangeListener((button, enabled) -> applyCapture(enabled));
        LinearLayout row = findViewById(R.id.row_capture);
        row.setOnClickListener(v -> captureSwitch.setChecked(!captureSwitch.isChecked()));
    }

    private void applyCapture(boolean enabled) {
        LogStore.setCaptureEnabled(this, enabled);
        Intent intent = new Intent("com.cainiao.brain.CONFIG_CHANGED");
        intent.setPackage("com.cainiao.wireless");
        intent.putExtra("enabled", enabled);
        sendBroadcast(intent);
    }
}
