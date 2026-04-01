package com.noetix.libcore.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class P {
    private static P spManager = null;
    private static SharedPreferences sp = null;
    private static SharedPreferences.Editor editor = null;
    private static final String SHARE_NAME = "Digital";
    public static final String KEY_RESOLUTION="resolution";
    public static final String KEY_AUTO_TO_FRONT="auto_to_front";

    private P(Context context) {
        sp = context.getSharedPreferences(SHARE_NAME, Context.MODE_PRIVATE);
        editor = sp.edit();
    }

    public static P get() {
        Context context = AppGlobals.getApplication();
        if (spManager == null || sp == null || editor == null) {
            spManager = new P(context);
        }
        return spManager;
    }

    public void putInt(String key, int value) {
        editor.putInt(key, value);
        editor.apply();
    }

    public int getInt(String key, int defaultValue) {
        return sp.getInt(key, defaultValue);
    }

    public void putLong(String key, Long value) {
        editor.putLong(key, value);
        editor.apply();
    }

    public long getLong(String key, long defaultValue) {
        return sp.getLong(key, defaultValue);
    }

    public void putString(String key, String value) {
        editor.putString(key, value);
        editor.apply();
    }

    public String getString(String key, String defaultValue) {
        return sp.getString(key, defaultValue);
    }

    public void putFloat(String key, float value) {
        editor.putFloat(key, value);
        editor.apply();
    }

    public boolean isKeyExist(String key) {
        return sp.contains(key);
    }

    public float getFloat(String key, float defaultValue) {
        return sp.getFloat(key, defaultValue);
    }

    public void putBoolean(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.apply();
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return sp.getBoolean(key, defaultValue);
    }

    public void remove(String key) {
        editor.remove(key);
        editor.apply();
    }

    public void removeAll(){
        editor.clear();
        editor.apply();
    }
}