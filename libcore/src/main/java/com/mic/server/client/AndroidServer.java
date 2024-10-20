package com.mic.server.client;

import static android.content.Context.WIFI_SERVICE;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.util.Log;

public class AndroidServer {

    private static final String TAG="http";
    private static final int DEFAULT_PORT = 9999;

    // INSTANCE OF ANDROID WEB SERVER
    private NanoHTTPD androidWebServer;
    private BroadcastReceiver broadcastReceiverNetworkState;
    private static boolean isStarted = false;

    private static AndroidServer instance =null;

    private Context context;

    private AndroidServer(Context context) {
        this.context = context;
    }

    public void init() {
        if (isConnectedInWifi()) {
            if (!isStarted && startServer()) {
                isStarted = true;
            } else if (stopServer()) {
                isStarted = false;
            }
        }
        // INIT BROADCAST RECEIVER TO LISTEN NETWORK STATE CHANGED
        initBroadcastReceiverNetworkStateChanged();
    }

    //region Start And Stop AndroidWebServer
    public boolean startServer() {
        if (!isStarted) {
            int port = getPortFromEditText();
            try {
                if (port == 0) {
                    throw new Exception();
                }
                androidWebServer = new NanoHTTPD(port);
                androidWebServer.start();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "The PORT " + port + " doesn't work, please change it between 1000 and 9999");
            }
        }
        return false;
    }

    public boolean stopServer() {
        if (isStarted && androidWebServer != null) {
            androidWebServer.stop();
            return true;
        }
        return false;
    }
    //endregion

    private void initBroadcastReceiverNetworkStateChanged() {
        final IntentFilter filters = new IntentFilter();
        filters.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        filters.addAction("android.net.wifi.STATE_CHANGE");
        broadcastReceiverNetworkState = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                getIpAccess();
            }
        };
        context.registerReceiver(broadcastReceiverNetworkState, filters);
    }

    private String getIpAccess() {
        WifiManager wifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
        final String formatedIpAddress = String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
        String host="http://" + formatedIpAddress + ":";
        Log.d(TAG,"host-->"+host);
        return host;
    }

    private int getPortFromEditText() {
        return DEFAULT_PORT;
    }

    public boolean isConnectedInWifi() {
//        WifiManager wifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
//        NetworkInfo networkInfo = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
//        if (networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected()
//                && wifiManager.isWifiEnabled() && networkInfo.getTypeName().equals("WIFI")) {
//            return true;
//        }
//        return false;
        return true;
    }
    //endregion

    protected void destroy() {
        stopServer();
        isStarted = false;
        if (broadcastReceiverNetworkState != null) {
            context.unregisterReceiver(broadcastReceiverNetworkState);
        }
    }

    public String getHost(){
        return getIpAccess()+DEFAULT_PORT;
    }

    public static AndroidServer get(Context context){
        return new AndroidServer(context);
    }

}
