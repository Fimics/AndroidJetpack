package com.mic.libcore.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;

import java.util.ArrayList;
import java.util.Iterator;

public class NetworkObserver {
    private static final String TAG = "nx_app";
    private final ArrayList<NetWorkListener> list = new ArrayList<>();
    private final ConnectivityManager connectivityManager;
    private final ConnectivityManager.NetworkCallback networkCallback;

    private static class Holder{
        private static final NetworkObserver instance = new NetworkObserver();
    }

    public static NetworkObserver getInstance(){
        return  Holder.instance;
    }

     private NetworkObserver() {
        connectivityManager = (ConnectivityManager) AppGlobals.getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                super.onAvailable(network);
                // 网络连接已可用
                list.forEach(it->{
                    it.onAvailable(network);
                });
            }

            @Override
            public void onLost(Network network) {
                super.onLost(network);
                // 网络连接已断开
                list.forEach(it->{
                    it.onLost(network);
                });
            }
        };
    }

    public void startListening() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(networkCallback);
        } else {
            NetworkRequest networkRequest = new NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build();
            connectivityManager.registerNetworkCallback(networkRequest, networkCallback);
        }
    }

    public void stopListening() {
        connectivityManager.unregisterNetworkCallback(networkCallback);
    }

    public void addNetWorkListener(NetWorkListener netWorkListener){
        if(!list.contains(netWorkListener)){
            list.add(netWorkListener);
        }
    }

    public void removeNetWorkListener(NetWorkListener netWorkListener){
        if (list.contains(netWorkListener)){
            Iterator iterator = list.iterator();
            while (iterator.hasNext()){
                NetWorkListener it = (NetWorkListener) iterator.next();
                if (it.equals(netWorkListener)){
                    iterator.remove();
                }
            }
        }
    }

    public interface NetWorkListener{
        void onAvailable(Network network);
        void onLost(Network network);
    }
}
