package com.mic.castserver;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import com.mic.castserver.service.SimpleServer;
import com.mic.castserver.service.Utils;

public class ServerService extends Service {

    private static SimpleServer simpleServer;

    public static void start(Context context) {
        if(context!=null){
            Intent intent = new Intent(context, ServerService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if(Utils.isAppOnForeground(context)){
                    context.startService(intent);
                }else{
                    context.startForegroundService(intent);
                }
            } else {
                context.startService(intent);
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        startHttpServer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            if(!Utils.isAppOnForeground(this)){
                buildNotification();
            }
            startHttpServer();
        }catch (Exception e){
            e.printStackTrace();
        }
        return Service.START_STICKY;
    }


    private void buildNotification(){
        String id = "13306";
        String name = "channel_cast";
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(id, name, NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(mChannel);
            notification = new Notification.Builder(this)
                    .setChannelId(id)
                    .build();
        } else {
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                    .setPriority(Notification.PRIORITY_DEFAULT);
            notification = notificationBuilder.build();
        }
        startForeground(1, notification);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopHttpServer();
        simpleServer=null;
    }

    public static void stopHttpServer() {
        if (simpleServer != null) {
            simpleServer.stopAsync();
            simpleServer=null;
        }
    }

    private static void startHttpServer() {
        if (simpleServer == null) {
            simpleServer = SimpleServer.createServer(NetUtils.getPort());
        }
        simpleServer.start();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
