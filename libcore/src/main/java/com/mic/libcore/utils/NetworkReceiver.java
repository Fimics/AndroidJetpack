package com.mic.libcore.utils;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class NetworkReceiver extends BroadcastReceiver {

    private final CopyOnWriteArrayList<NetworkState> list = new CopyOnWriteArrayList<>();

    public NetworkReceiver() {
    }

    public void addNetworkState(NetworkState networkState){
        if (!list.contains(networkState)){
            list.add(networkState);
        }
    }

    public void removeNetworkState(NetworkState networkState){
        list.remove(networkState);
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        // 监听网络连接，包括wifi和移动数据的打开和关闭,以及连接上可用的连接都会接到监听
        // 特殊注意：如果if条件生效，那么证明当前是有连接wifi或移动网络的，如果有业务逻辑最好把esle场景酌情考虑进去！
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            //获取联网状态的NetworkInfo对象
            NetworkInfo info = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            if (info != null) {
                //如果当前的网络连接成功并且网络连接可用
                if (NetworkInfo.State.CONNECTED == info.getState() && info.isAvailable()) {
                    if (info.getType() == ConnectivityManager.TYPE_WIFI || info.getType() == ConnectivityManager.TYPE_MOBILE) {
                        Log.e("TAG", getConnectionType(info.getType()) + "已连接");
                    }
                    list.forEach(it->{
                        it.onConnected();
                    });
                } else {
                    Log.e("TAG", getConnectionType(info.getType()) + "已断开");
                    list.forEach(it->it.onDisConnected());
                }
            }
        }
    }

    /**
     * 获取连接类型
     * @param type
     * @return
     */
    private String getConnectionType(int type) {
        String connType = "";
        if (type == ConnectivityManager.TYPE_MOBILE) {
            connType = "3-4G网络数据";
        } else if (type == ConnectivityManager.TYPE_WIFI) {
            connType = "WIFI网络";
        }
        return connType;
    }

    public interface NetworkState{
        void onConnected();
        void onDisConnected();
    }
}
