package com.mic.netconfig.softap;

import android.util.Log;
import org.json.JSONObject;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

public class UDPServer extends Thread {
    private static final String TAG = "UDPServer";
    private static final int PORT = 8266;
    private static final int BUFFER_SIZE = 1024;
    
    private DatagramSocket socket;
    private boolean isRunning = true;
    private OnConfigReceivedListener listener;
    
    public interface OnConfigReceivedListener {
        void onConfigReceived(String ssid, String password);
        void onError(String message);
    }
    
    public UDPServer(OnConfigReceivedListener listener) {
        this.listener = listener;
    }
    
    @Override
    public void run() {
        try {
            socket = new DatagramSocket(PORT, InetAddress.getByName("0.0.0.0"));
            socket.setBroadcast(true);
            socket.setSoTimeout(0);
            
            Log.i(TAG, "UDP服务器启动，监听端口: " + PORT);
            
            while (isRunning && !socket.isClosed()) {
                byte[] buffer = new byte[BUFFER_SIZE];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                
                try {
                    socket.receive(packet);
                    String receivedData = new String(
                        packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
                    
                    Log.i(TAG, "收到UDP数据: " + receivedData + " 来自: " + 
                          packet.getAddress().getHostAddress());
                    
                    processData(receivedData, packet.getAddress());
                    
                } catch (IOException e) {
                    if (isRunning) {
                        Log.e(TAG, "接收数据异常", e);
                    }
                }
            }
            
        } catch (SocketException e) {
            Log.e(TAG, "Socket异常", e);
            if (listener != null) {
                listener.onError("Socket异常: " + e.getMessage());
            }
        } catch (Exception e) {
            Log.e(TAG, "UDP服务器异常", e);
            if (listener != null) {
                listener.onError("服务器异常: " + e.getMessage());
            }
        } finally {
            closeSocket();
        }
    }
    
    private void processData(String data, InetAddress clientAddress) {
        try {
            JSONObject json = new JSONObject(data);
            int cmdType = json.optInt("cmdType", 0);
            
            if (cmdType == 1) { // 配网命令
                String ssid = json.optString("ssid", "");
                String password = json.optString("password", "");
                
                if (!ssid.isEmpty()) {
                    Log.i(TAG, "解析到WiFi配置: SSID=" + ssid);
                    
                    // 发送确认响应
                    JSONObject response = new JSONObject();
                    response.put("cmdType", 2);
                    response.put("status", 1);
                    response.put("message", "配置接收成功");
                    
                    sendResponse(response.toString(), clientAddress);
                    
                    // 通知监听器
                    if (listener != null) {
                        listener.onConfigReceived(ssid, password);
                    }
                } else {
                    Log.w(TAG, "收到无效的WiFi配置");
                }
            } else if (cmdType == 3) { // 心跳包
                JSONObject response = new JSONObject();
                response.put("cmdType", 4);
                response.put("status", 1);
                response.put("timestamp", System.currentTimeMillis());
                
                sendResponse(response.toString(), clientAddress);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "处理数据异常", e);
        }
    }
    
    private void sendResponse(String response, InetAddress clientAddress) {
        try {
            byte[] data = response.getBytes(StandardCharsets.UTF_8);
            DatagramPacket packet = new DatagramPacket(
                data, data.length, clientAddress, PORT);
            socket.send(packet);
            Log.i(TAG, "发送响应: " + response);
        } catch (IOException e) {
            Log.e(TAG, "发送响应失败", e);
        }
    }
    
    public void stopServer() {
        isRunning = false;
        closeSocket();
    }
    
    private void closeSocket() {
        if (socket != null && !socket.isClosed()) {
            socket.close();
            Log.i(TAG, "UDP服务器已关闭");
        }
    }
}