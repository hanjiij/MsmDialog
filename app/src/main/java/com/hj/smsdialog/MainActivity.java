package com.hj.smsdialog;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.hj.smsdialog.service.smsDialogService;
import com.hj.smsdialog.utils.DataUtil;

public class MainActivity extends ActionBarActivity {

    private Switch smsDialogs;
    private Switch smsdialog_identify;
    private Switch smsdialog_light;
    private Switch smsdialog_start_up;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        setListener();
    }

    /**
     * 设置监听
     */
    private void setListener() {

        // 是否弹出短信监听
        smsDialogs.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                DataUtil.getInstance(MainActivity.this).setConfig(Config.IS_DIALOG_SMS, isChecked);

                Intent intent = new Intent(MainActivity.this, smsDialogService.class);

                if (isChecked) {
                    startService(intent);
                } else {
                    stopService(intent);
                }
            }
        });

        // 是否截取验证码监听
        smsdialog_identify.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                DataUtil.getInstance(MainActivity.this)
                        .setConfig(Config.IS_DIALOG_SMS_IDENTIFY, isChecked);
            }
        });

        // 是否来短信点亮屏幕
        smsdialog_light.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                DataUtil.getInstance(MainActivity.this)
                        .setConfig(Config.IS_DIALOG_SMS_LIGHT, isChecked);
            }
        });

        // 是否开机启动
        smsdialog_start_up.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                DataUtil.getInstance(MainActivity.this)
                        .setConfig(Config.IS_DIALOG_SMS_START_UP, isChecked);
            }
        });
    }

    /**
     * 初始化数据
     */
    private void init() {

        smsDialogs = (Switch) findViewById(R.id.smsdialog);
        smsdialog_identify = (Switch) findViewById(R.id.smsdialog_identify);
        smsdialog_light = (Switch) findViewById(R.id.smsdialog_light);
        smsdialog_start_up= (Switch) findViewById(R.id.smsdialog_start_up);

        boolean isOpen = DataUtil.getInstance(MainActivity.this).getConfig(Config.IS_DIALOG_SMS);
        if (isOpen) {
            smsDialogs.setChecked(true);

            if (!Config.isMyServiceRunning(MainActivity.this,
                    "com.hj.smsdialog.service.smsDialogService")) {

                Intent intent = new Intent(MainActivity.this, smsDialogService.class);
                startService(intent);
            }
        }

        smsdialog_identify.setChecked(
                DataUtil.getInstance(MainActivity.this).getConfig(Config.IS_DIALOG_SMS_IDENTIFY));
        smsdialog_light.setChecked(
                DataUtil.getInstance(MainActivity.this).getConfig(Config.IS_DIALOG_SMS_LIGHT));
        smsdialog_start_up.setChecked(
                DataUtil.getInstance(MainActivity.this).getConfig(Config.IS_DIALOG_SMS_START_UP));
    }
}