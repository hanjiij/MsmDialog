package com.hj.smsdialog.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.hj.smsdialog.Config;
import com.hj.smsdialog.service.smsDialogService;
import com.hj.smsdialog.utils.DataUtil;

public class bootReceiver extends BroadcastReceiver {
    public bootReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving

        if (DataUtil.getInstance(context).getConfig(Config.IS_DIALOG_SMS) &&
                DataUtil.getInstance(context).getConfig(Config.IS_DIALOG_SMS_START_UP) &&
                (!Config.isMyServiceRunning(context,
                        "com.hj.smsdialog.service.smsDialogService"))) {
            Intent service = new Intent(context, smsDialogService.class);
            context.startService(service);
        }
    }
}
