package com.hj.smsdialog.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by HJ on 2015/9/22.
 * 数据存储包装类
 */
public class DataUtil {

    private static DataUtil dataUtil;
    private SharedPreferences sharedPreferences;
    private Context context;

    private DataUtil(Context context) {

        this.context = context;
        sharedPreferences = context.getSharedPreferences("config", 0);
    }

    /**
     * 获取数据帮助类的实例
     *
     * @return 实例
     */
    public static DataUtil getInstance(Context context) {
        if (dataUtil == null) {
            dataUtil = new DataUtil(context);
        }
        return dataUtil;
    }

    /**存储config数据
     * @param key 键
     * @param data boolean数据
     */
    public void setConfig(String key, boolean data) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, data);
        editor.commit();
    }

    /**获取config数据
     * @param key 键
     * @return boolean数据
     */
    public boolean getConfig(String key){
        return sharedPreferences.getBoolean(key,false);
    }
}
