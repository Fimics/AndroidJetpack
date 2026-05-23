package com.mic.libcore.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

public class SettingsUtils {


    public static void goSystemSettings(Context context){
        Intent intent = new Intent();
        ComponentName componentName = new ComponentName("com.android.settings", "com.android.settings.Settings");
        intent.setComponent(componentName);
        context.startActivity(intent);
    }

}
