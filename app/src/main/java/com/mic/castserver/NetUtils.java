package com.mic.castserver;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;


import java.net.Inet4Address;

public class NetUtils {

    public static Inet4Address CAST_ADDRESS;

    public static String getIpOfWifi(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager==null || !wifiManager.isWifiEnabled()) {
            return "";
        }

        WifiInfo wifiInfo = wifiManager.getConnectionInfo();

        int ipAddress = 0;
        if(wifiInfo!=null){
            ipAddress= wifiInfo.getIpAddress();
        }
        return intToIp(ipAddress);
    }

    private static String intToIp(int i) {

        return (i & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                (i >> 24 & 0xFF);
    }



    public static int getPort(){
        return  ServerConfig.SERVER_PORT;
    }

}
