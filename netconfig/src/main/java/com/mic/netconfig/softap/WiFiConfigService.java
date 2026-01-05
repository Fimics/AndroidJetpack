package com.mic.netconfig.softap;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.Nullable;

public class WiFiConfigService extends Service {
    private static final String TAG = "WiFiConfigService";
    
    private SoftAPManager softAPManager;
    private UDPServer udpServer;
    private boolean isConfiguring = false;
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "WiFi配网服务创建");
        
        softAPManager = new SoftAPManager(this);
        
        // 启动UDP服务器
        udpServer = new UDPServer(new UDPServer.OnConfigReceivedListener() {
            @Override
            public void onConfigReceived(String ssid, String password) {
                handleWiFiConfig(ssid, password);
            }
            
            @Override
            public void onError(String message) {
                Log.e(TAG, "UDP服务器错误: " + message);
            }
        });
        
        udpServer.start();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "开始WiFi配网流程");
        
        // 启动热点
        new Thread(() -> {
            if (softAPManager.createSoftAP()) {
                String ip = softAPManager.getSoftAPIPAddress();
                Log.i(TAG, "热点创建成功，IP: " + ip);
                
                // 发送广播通知热点已就绪
                sendBroadcast(new Intent("SOFTAP_READY"));
            } else {
                Log.e(TAG, "热点创建失败");
                stopSelf();
            }
        }).start();
        
        return START_STICKY;
    }
    
    private void handleWiFiConfig(String ssid, String password) {
        if (isConfiguring) {
            return;
        }
        
        isConfiguring = true;
        Log.i(TAG, "开始处理WiFi配置: " + ssid);
        
        new Thread(() -> {
            try {
                // 连接指定WiFi
                boolean success = softAPManager.connectToWiFi(ssid, password);
                
                if (success) {
                    Log.i(TAG, "WiFi连接请求已发送，等待连接结果...");
                    
                    // 等待连接
                    Thread.sleep(5000);
                    
                    // 检查连接状态
                    boolean isConnected = checkWiFiConnection(ssid);
                    
                    if (isConnected) {
                        Log.i(TAG, "WiFi连接成功");
                        // 发送成功广播
                        Intent intent = new Intent("WIFI_CONFIG_SUCCESS");
                        intent.putExtra("ssid", ssid);
                        sendBroadcast(intent);
                        
                        // 停止热点
                        softAPManager.disableSoftAP();
                    } else {
                        Log.w(TAG, "WiFi连接失败");
                        // 重新开启热点等待重新配网
                        softAPManager.createSoftAP();
                    }
                } else {
                    Log.e(TAG, "连接WiFi失败");
                    softAPManager.createSoftAP(); // 重新开启热点
                }
                
            } catch (Exception e) {
                Log.e(TAG, "处理WiFi配置异常", e);
            } finally {
                isConfiguring = false;
            }
        }).start();
    }
    
    private boolean checkWiFiConnection(String targetSsid) {
        try {
            // 这里可以添加WiFi连接状态检查逻辑
            // 实际应用中可能需要轮询检查连接状态
            Thread.sleep(3000);
            return true; // 简化处理，实际需要实现连接状态检查
            
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public void onDestroy() {
        Log.i(TAG, "WiFi配网服务销毁");
        
        if (udpServer != null) {
            udpServer.stopServer();
        }
        
        if (softAPManager != null) {
            softAPManager.disableSoftAP();
        }
        
        super.onDestroy();
    }
    
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}