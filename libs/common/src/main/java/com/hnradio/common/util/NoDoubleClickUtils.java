package com.hnradio.common.util;

/**
 * Created by yingding on 2018/5/22.防止连续点击
 */

public class NoDoubleClickUtils {
    private static long lastClickTime;
    private final static int SPACE_TIME = 500;

    public static void initLastClickTime() {
        lastClickTime = 0;
    }

    public synchronized static boolean isDoubleClick() {
        long currentTime = System.currentTimeMillis();
        boolean isClick2;
        if (currentTime - lastClickTime >
                SPACE_TIME) {
            isClick2 = false;
            lastClickTime = currentTime;
        } else {
            isClick2 = true;
        }
        return isClick2;
    }
}
