package com.hj.smsdialog;

import android.app.ActivityManager;
import android.content.Context;

/**
 * Created by HJ on 2015/9/22.
 */
public class Config {

    public static String IS_DIALOG_SMS = "is_dialog_sms";
    public static String IS_DIALOG_SMS_IDENTIFY = "is_dialog_sms_identify";
    public static String IS_DIALOG_SMS_LIGHT = "is_dialog_sms_light";
    public static String IS_DIALOG_SMS_START_UP = "is_dialog_sms_start_up";

    /**
     * 判断服务是否启动
     *
     * @return
     */
    public static boolean isMyServiceRunning(Context context, String package_name) {
        ActivityManager manager =
                (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager
                .getRunningServices(Integer.MAX_VALUE)) {
            if (package_name.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
