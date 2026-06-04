package com.mic.netconfig.softap;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiInfo;
import android.os.Build;
import android.util.Log;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class SoftAPManager {
    private static final String TAG = "SoftAPManager";
    private WifiManager wifiManager;
    private Context context;
    
    public SoftAPManager(Context context) {
        this.context = context.getApplicationContext();
        wifiManager = (WifiManager) context.getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
    }
    
    /**
     * 获取设备MAC地址
     */
    public String getMacAddress() {
        String macAddress = "";
        try {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo != null) {
                macAddress = wifiInfo.getMacAddress();
                if (macAddress != null) {
                    // 去除冒号，转换为大写
                    macAddress = macAddress.replace(":", "").toUpperCase();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "获取MAC地址失败", e);
        }
        return macAddress;
    }
    
    /**
     * 创建热点
     */
    public boolean createSoftAP() {
        try {
            String macAddress = getMacAddress();
            if (macAddress == null || macAddress.isEmpty()) {
                macAddress = "RK3588_" + Build.SERIAL.substring(0, 6);
            }
            
            String ssid = "RK_" + macAddress.substring(macAddress.length() - 6);
            String password = "12345678";
            
            Log.i(TAG, "创建热点: SSID=" + ssid + ", Password=" + password);
            
            // 先关闭WiFi客户端模式
            if (wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(false);
                Thread.sleep(1000);
            }
            
            // 创建热点配置
            WifiConfiguration apConfig = new WifiConfiguration();
            apConfig.SSID = ssid;
            apConfig.preSharedKey = password;
            apConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            apConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            apConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            apConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            apConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            apConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            apConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            apConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            apConfig.status = WifiConfiguration.Status.ENABLED;
            
            // 使用反射设置热点
            Method method = wifiManager.getClass().getMethod(
                "setWifiApEnabled", WifiConfiguration.class, boolean.class);
            
            // 先关闭已有热点
            method.invoke(wifiManager, null, false);
            Thread.sleep(1000);
            
            // 开启新热点
            boolean result = (boolean) method.invoke(wifiManager, apConfig, true);
            
            Log.i(TAG, "热点创建结果: " + result);
            return result;
            
        } catch (Exception e) {
            Log.e(TAG, "创建热点失败", e);
            return false;
        }
    }
    
    /**
     * 获取热点IP地址
     */
    public String getSoftAPIPAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.isUp() && !networkInterface.isLoopback()) {
                    Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        InetAddress address = addresses.nextElement();
                        if (!address.isLoopbackAddress() && address.getAddress().length == 4) {
                            String ip = address.getHostAddress();
                            if (ip.startsWith("192.168.")) {
                                return ip;
                            }
                        }
                    }
                }
            }
        } catch (SocketException e) {
            Log.e(TAG, "获取IP地址失败", e);
        }
        return "192.168.43.1"; // Android热点默认IP
    }
    
    /**
     * 关闭热点
     */
    public boolean disableSoftAP() {
        try {
            Method method = wifiManager.getClass().getMethod(
                "setWifiApEnabled", WifiConfiguration.class, boolean.class);
            return (boolean) method.invoke(wifiManager, null, false);
        } catch (Exception e) {
            Log.e(TAG, "关闭热点失败", e);
            return false;
        }
    }
    
    /**
     * 连接指定WiFi
     */
    public boolean connectToWiFi(String ssid, String password) {
        try {
            Log.i(TAG, "开始连接WiFi: " + ssid);
            
            // 先关闭热点
            disableSoftAP();
            Thread.sleep(2000);
            
            // 启用WiFi客户端模式
            if (!wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(true);
                Thread.sleep(2000);
            }
            
            // 移除已保存的相同网络
            WifiConfiguration existingConfig = getExistingNetwork(ssid);
            if (existingConfig != null) {
                wifiManager.removeNetwork(existingConfig.networkId);
                wifiManager.saveConfiguration();
            }
            
            // 创建新的WiFi配置
            WifiConfiguration wifiConfig = new WifiConfiguration();
            wifiConfig.SSID = "\"" + ssid + "\"";
            
            if (password.isEmpty()) {
                wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            } else {
                wifiConfig.preSharedKey = "\"" + password + "\"";
                wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                wifiConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            }
            
            wifiConfig.status = WifiConfiguration.Status.ENABLED;
            
            // 添加网络配置
            int netId = wifiManager.addNetwork(wifiConfig);
            if (netId == -1) {
                Log.e(TAG, "添加网络配置失败");
                return false;
            }
            
            // 启用网络
            wifiManager.enableNetwork(netId, true);
            wifiManager.saveConfiguration();
            
            // 重新连接
            wifiManager.reconnect();
            
            Log.i(TAG, "WiFi连接请求已发送，网络ID: " + netId);
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "连接WiFi失败", e);
            return false;
        }
    }
    
    private WifiConfiguration getExistingNetwork(String ssid) {
        for (WifiConfiguration config : wifiManager.getConfiguredNetworks()) {
            if (config.SSID != null && config.SSID.equals("\"" + ssid + "\"")) {
                return config;
            }
        }
        return null;
    }
}