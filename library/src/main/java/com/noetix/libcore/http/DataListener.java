package com.noetix.libcore.http;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;

public interface DataListener {
    void onFailure(Exception e);
    void onSuccess(@NonNull Call call, @NonNull Response response) throws IOException;
}
