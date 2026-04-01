package com.mic.nav.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import okhttp3.Response;
import okhttp3.WebSocket;
import okio.ByteString;

public interface NXListener {

     void onOpen(@NonNull WebSocket webSocket, @NonNull Response response);

     void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, @Nullable Response response);

     void onMessage(@NonNull WebSocket webSocket, @NonNull String text) ;

     void onMessage(@NonNull WebSocket webSocket, @NonNull ByteString bytes) ;

     void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason);

     void onClosing(@NonNull WebSocket webSocket, int code, @NonNull String reason);

     void onSendHeartBeatPacket();
}
