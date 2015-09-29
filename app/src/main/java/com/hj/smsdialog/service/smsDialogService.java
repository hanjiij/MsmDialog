package com.hj.smsdialog.service;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hj.smsdialog.Config;
import com.hj.smsdialog.R;
import com.hj.smsdialog.utils.DataUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.provider.ContactsContract.PhoneLookup;

public class smsDialogService extends Service {

    private SmsObserver smsObserver;
    private Uri SMS_INBOX = Uri.parse("content://sms");
    private String dateStr = "";

    private ClipboardManager myClipboard;
    private ClipData myClip;

    public smsDialogService() {}

    @Override
    public void onCreate() {
        System.out.println("onCreate");
        super.onCreate();

        smsObserver = new SmsObserver(smshandler);
        getContentResolver().registerContentObserver(SMS_INBOX, true, smsObserver);
        myClipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
    }

    public Handler smshandler = new Handler() {};

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.

        System.out.println("onBind");
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {

        System.out.println("onDestroy");
        super.onDestroy();

        getContentResolver().unregisterContentObserver(smsObserver);

    }

    class SmsObserver extends ContentObserver {

        /**
         * Creates a content observer.
         *
         * @param handler The handler to run {@link #onChange} on, or null if none.
         */
        public SmsObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);

            System.out.println("======" + uri);
            if ((!"content://sms/raw".equals(uri.toString())) &&
                    (!"content://sms/inbox".equals(uri.toString()))) {// 去除多余的监听
                getSmsFromPhone();
            }
        }
    }

    /**
     * 获取手机短信
     */
    private void getSmsFromPhone() {

        ContentResolver cr = getContentResolver();
        String[] projection = new String[]{"address", "body", "date",
                "type"};//"_id", "address", "person", "date", "type"
        Cursor cur = cr.query(SMS_INBOX, projection, null, null, "date desc");

        if (cur != null) {
            if (cur.moveToNext()) {
                String address = cur.getString(cur.getColumnIndex("address"));
                String body = cur.getString(cur.getColumnIndex("body"));
                String date = cur.getString(cur.getColumnIndex("date"));
                String type = cur.getString(cur.getColumnIndex("type"));

                System.out.println(
                        "address:" + address + "、body:" + body + "、date:" + date + "、type:" + type);
                if ((!date.equals(dateStr)) && type.equals("1")) {

                    // 获取通讯录中号码的姓名
                    Cursor cursor =
                            cr.query(Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, address),
                                    new String[]{PhoneLookup.DISPLAY_NAME}, null, null, null);
                    boolean isCursor = cursor.moveToNext();

                    // 判断是否为手机号或通讯录中是否存在，若不是则检测内容是否有数字验证码
                    if (isMobileNO(address) || isCursor) {
                        // 若通讯录中存在该号码，则address修改为联系人姓名
                        if (isCursor) {
                            address = cursor.getString(0);
                        }
                        setDialog(address, body, date);
//                        dateStr = date;
                    } else {

                        Pattern pattern = Pattern.compile("(\\d{6})");
                        final Matcher matcher = pattern.matcher(body);

                        // 是否含有验证码，没有则正常弹出窗口
                        if (matcher.find() && DataUtil.getInstance(smsDialogService.this)
                                .getConfig(Config.IS_DIALOG_SMS_IDENTIFY)) {

                            Builder builder = new Builder(getApplicationContext());
                            builder.setMessage(matcher.group(0));
                            builder.setNegativeButton("复制", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    myClip = ClipData.newPlainText("text", matcher.group(0));
                                    myClipboard.setPrimaryClip(myClip);
                                    Toast.makeText(getApplicationContext(), "已复制到剪贴板",
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                            builder.setPositiveButton("取消", null);
                            Dialog dialog = builder.create();
                            dialog.getWindow().setType(LayoutParams.TYPE_SYSTEM_ALERT);
                            dialog.show();
                            //设置弹出框的宽高
                            DisplayMetrics windowManager = getResources().getDisplayMetrics();
                            int w = windowManager.widthPixels;
                            LayoutParams params = dialog.getWindow().getAttributes();
                            params.width = (int) (w * (6.0 / 8));
                            //params.height = (int) (h * (2.0 / 3));
                            dialog.getWindow().setAttributes(params);

                        } else {
                            setDialog(address, body, date);
//                            dateStr = date;
                        }
                    }
                    // 防止重复弹窗
                    dateStr = date;
                    // 是否需要点亮屏幕
                    if (DataUtil.getInstance(smsDialogService.this)
                            .getConfig(Config.IS_DIALOG_SMS_LIGHT)) {
                        AcquireWakeLock();
                    }
                }
            }
        }
    }

    /**
     * 点亮屏幕，释放时间2秒
     */
    private void AcquireWakeLock() {

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock m_wakeObj =
                pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP |
                        PowerManager.ON_AFTER_RELEASE, "");
        m_wakeObj.acquire(1000);
        m_wakeObj.release();//释放资源
    }

    /**
     * 弹出短信dialog
     *
     * @param title 标题
     * @param body  文本内容
     * @param date  发送的时间
     */
    private void setDialog(String title, String body, String date) {

        Builder builder = new Builder(getApplicationContext());
        final View dialog_view =
                LayoutInflater.from(smsDialogService.this).inflate(R.layout.dialog_layout, null);

        ((TextView) dialog_view.findViewById(R.id.dialog_title)).setText(title);
        ((TextView) dialog_view.findViewById(R.id.dialog_body)).setText(body);

        builder.setView(dialog_view);
        final Dialog dialog = builder.create();
        dialog.getWindow().setType(LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.show();

        // dialog取消按钮监听
        dialog_view.findViewById(R.id.dialog_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        // dialog发送按钮的监听
        dialog_view.findViewById(R.id.dialog_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText editText = (EditText) dialog_view.findViewById(R.id.dialog_send_body);
                Toast.makeText(smsDialogService.this, editText.getText().toString(),
                        Toast.LENGTH_SHORT).show();
                dialog.cancel();
            }
        });

        DisplayMetrics windowManager = getResources().getDisplayMetrics();
        int w = windowManager.widthPixels;
//        int h = windowManager.heightPixels;
        LayoutParams params = dialog.getWindow().getAttributes();  // 设置dialog的宽度
        params.width = (int) (w * (6.0 / 8)); // 显示的宽度
//        params.height = (int) (h * (2.0 / 3));
        dialog.getWindow().setAttributes(params);
    }

    /**
     * 判断是否为正确的手机号码
     *
     * @param mobiles 手机号码
     * @return 是否为正确的手机号
     */
    private boolean isMobileNO(String mobiles) {
        Pattern p = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$");
        Matcher m = p.matcher(mobiles);
        return m.matches();
    }
}

