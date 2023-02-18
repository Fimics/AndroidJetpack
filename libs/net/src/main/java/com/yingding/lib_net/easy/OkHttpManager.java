package com.yingding.lib_net.easy;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.yingding.lib_net.BuildConfig;
import com.yingding.lib_net.log_print.Logger;
import com.yingding.lib_net.log_print.LoggingInterceptor;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by ytf on 2016/11/9.
 * descption: 网络请求类，支持get和post缓存
 */

public class OkHttpManager
{
    public static final MediaType JSON_TYPE = MediaType.parse("application/json; charset=utf-8");
    //默认超时时间15秒
    private static final int TIMEOUT = 20;
    private volatile static OkHttpManager mInstance;
    private static Params mParams;
    private OkHttpClient clientDefault;
    private OkHttpClient pureClient;

    private OkHttpManager()
    {
    }

    /***
     * 获取实例
     * @return
     */
    public static OkHttpManager getInstance()
    {
        if (mInstance == null)
        {
            synchronized (OkHttpManager.class)
            {
                if (mInstance == null)
                {
                    mInstance = new OkHttpManager();
                }
            }
        }
        return mInstance;
    }

    public void config(Params p)
    {
        mParams = p;

        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        if(mParams != null)
        {
            if(mParams.globalHeaderInterceptor != null)
            {
                builder.addInterceptor(mParams.globalHeaderInterceptor);
            }
            if(mParams.globalParamInterceptor != null)
            {
                builder.addInterceptor(mParams.globalParamInterceptor);
            }
            if(mParams.globalPreRequestInterceptor != null)
            {
                builder.addInterceptor(mParams.globalPreRequestInterceptor);
            }
            if(mParams.interceptors != null && mParams.interceptors.size() > 0)
            {
                for(Interceptor i : mParams.interceptors)
                {
                    builder.addInterceptor(i);
                }
            }
        }
        builder.addInterceptor(prepareLogInterceptor());

        clientDefault = builder.connectTimeout(TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT, TimeUnit.SECONDS)
                .sslSocketFactory(SSLHelper.getSSLSocketFactory(), SSLHelper.getTrustManager())
                .hostnameVerifier(SSLHelper.getHostnameVerifier())//配置
                .retryOnConnectionFailure(true)
                .build();

    }

    private Interceptor prepareLogInterceptor()
    {
//        LogInterceptor logInterceptor = new LogInterceptor();

        return new LoggingInterceptor.Builder()
                .executor(Executors.newCachedThreadPool())
                .loggable(BuildConfig.DEBUG)
                .enableAndroidStudio_v3_LogsHack(true)
                .logger(mParams.logPrinter)
                .tag("http").build();
    }

    private void preparePureClient()
    {
        if(pureClient != null)
            return;
        pureClient = new OkHttpClient.Builder()
                .addInterceptor(prepareLogInterceptor())
                .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT, TimeUnit.SECONDS)
                .sslSocketFactory(SSLHelper.getSSLSocketFactory(), SSLHelper.getTrustManager())
                .hostnameVerifier(SSLHelper.getHostnameVerifier())//配置
                .retryOnConnectionFailure(true)
                .build();
    }

    private Handler mHandler;

    private void setDefaultIfNotInit()
    {
        if(clientDefault == null)
        {
            mHandler = new Handler(Looper.getMainLooper());
            config(new ConfigBuilder()
                    .setRespHandler(new HttpReqAndRespHandler()
                    {
                        @Override
                        public void printLog(String msg)
                        {
                            Log.e("s", msg);
                        }

                        @Override
                        public void runOnMainThread(Runnable runnable)
                        {
                            mHandler.post(runnable);
                        }
                    })
                    .setGson(GsonKit.INSTANCE.getTypeFormatGson()).build());
        }
    }

    public ReqBuilder get(String url)
    {
        setDefaultIfNotInit();
        return new ReqBuilder("GET", url, clientDefault);
    }

    public ReqBuilder customGet(String url)
    {
        preparePureClient();
        return new ReqBuilder("GET", url, pureClient);
    }

    public ReqBuilder customPost(String url)
    {
        preparePureClient();
        return new ReqBuilder("POST", url, pureClient);
    }

    public ReqBuilder post(String url)
    {
        setDefaultIfNotInit();
        return new ReqBuilder("POST", url, clientDefault);
    }

    public static class ReqBuilder
    {
        Map<String, String> queryMap = new HashMap<>();
        Map<String, String> files = new HashMap<>();
        Object postJsonObj;
        Map<String, String> headers = new HashMap<>();
        String method;
        String tag;
        String url;
        Gson mGson;
        OkHttpResultCallback<? extends Object> callback;
        boolean mainThread = true;
        OkHttpClient client;

        public ReqBuilder(String method, String url, OkHttpClient clientDefault)
        {
            this.method = method;
            this.url = url;
            this.client = clientDefault;
        }

        public ReqBuilder gson(Gson gson)
        {
            this.mGson = gson;
            return this;
        }

        public ReqBuilder header(String k, String v)
        {
            if (!TextUtils.isEmpty(k) && !TextUtils.isEmpty(v))
            {
                headers.put(k, v);
            }
            return this;
        }

        public ReqBuilder queryParamMap(Map<String, String> querys)
        {
            if (querys != null && querys.size() > 0)
            {
                for (String k : querys.keySet())
                {
                    String v = querys.get(k);
                    if (!TextUtils.isEmpty(v))
                    {
                        queryMap.put(k, v);
                    }
                }
            }
            return this;
        }

        public ReqBuilder queryParam(String k, String v)
        {
            if (!TextUtils.isEmpty(k) && !TextUtils.isEmpty(v))
            {
                queryMap.put(k, v);
            }
            return this;
        }

        public ReqBuilder filesParamMap(Map<String, String> querys)
        {
            if (querys != null && querys.size() > 0)
            {
                for (String k : querys.keySet())
                {
                    String v = querys.get(k);
                    if (!TextUtils.isEmpty(v))
                    {
                        files.put(k, v);
                    }
                }
            }
            return this;
        }

        public ReqBuilder filesParam(String k, String v)
        {
            if (!TextUtils.isEmpty(k) && !TextUtils.isEmpty(v))
            {
                files.put(k, v);
            }
            return this;
        }

        public ReqBuilder tag(String tag)
        {
            this.tag = tag;
            return this;
        }

        public ReqBuilder postJson(Object json)
        {
            this.postJsonObj = json;
            return this;
        }

        public ReqBuilder callback(OkHttpResultCallback<? extends Object> callback)
        {
            this.callback = callback;
            return this;
        }

        public ReqBuilder withMainThread(boolean yn)
        {
            this.mainThread = yn;
            return this;
        }

        /**
         * 除了log以外，不带任何其他拦截器，需要调用gson设置解析器，否则使用reqhandler的gson
         */
        public void pureEnqueue()
        {
            if (callback == null)
            {
                throw new IllegalArgumentException("回调不能为空");
            }
            Request.Builder builder = prepareBuilder(new Request.Builder().tag(tag == null ? url : tag));
            deliveryRequest(client, builder.build(), mGson != null ? mGson : mParams.mGson, callback, mainThread);
        }

        public void enqueue()
        {
            Request.Builder builder = prepare();
            deliveryRequest(client, builder.build(), mParams.mGson, callback, mainThread);
        }

        private Request.Builder prepare()
        {
            if (mParams.reqAndRespHandler != null)
                url = mParams.reqAndRespHandler.handleBeforeReq(url);

            if (callback == null)
            {
                throw new IllegalArgumentException("回调不能为空");
            }

            return prepareBuilder(new Request.Builder().tag(tag == null ? url : tag));
        }

        private Request.Builder prepareBuilder(Request.Builder builder)
        {
            if (headers.size() > 0)
            {
                for (String k : headers.keySet())
                {
                    String v = headers.get(k);

                    if (!TextUtils.isEmpty(v))
                    {
                        builder.header(k, v);
                    }
                }
            }

            if ("GET".equals(method))
            {
                if (queryMap.size() > 0)
                {
                    HttpUrl target = HttpUrl.parse(url);
                    List<String> paths = target.pathSegments();
                    HttpUrl.Builder urlBuilder = new HttpUrl.Builder();
                    urlBuilder.scheme(target.scheme()).host(target.host()).port(target.port());

                    for (String p : paths)
                    {
                        urlBuilder.addPathSegment(p);
                    }

                    for (String k : queryMap.keySet())
                    {
                        String value = queryMap.get(k);
                        if(!TextUtils.isEmpty(value)){
                            urlBuilder.addQueryParameter(k, queryMap.get(k));
                        }
                    }

                    builder.url(urlBuilder.build()).get();
                }else
                {
                    builder.url(url).get();
                }
            } else if ("POST".equals(method))
            {
                RequestBody reqBody;
                if (postJsonObj != null)
                {
                    String json = mParams.mGson.toJson(postJsonObj);
                    mParams.reqAndRespHandler.printLog("Json Post ==> [url = " + url + "] [json_param = " + json + "]");
                    reqBody = RequestBody.create(JSON_TYPE, json);
                } else if (queryMap.size() > 0)
                {
                    if(files.size() > 0)
                    {
                        MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder();
                        multipartBodyBuilder.setType(MultipartBody.FORM);
                        //遍历map中所有参数到builder
                        if (queryMap.size() > 0)
                        {
                            mParams.reqAndRespHandler.handleMultiParam("POST", queryMap);
                            for (String key : queryMap.keySet())
                            {
                                String value = queryMap.get(key);
                                if (!TextUtils.isEmpty(value))
                                {
                                    multipartBodyBuilder.addFormDataPart(key, value);
                                }
                            }
                        }
                        for (String k : files.keySet())
                        {
                            String fpath = files.get(k);
                            File file = new File(fpath);
                            if(!file.exists())
                            {
                                throw new RuntimeException("文件异常：" + fpath + "  不存在");
                            }
                            String fileType = getMimeType(file.getName()); //根据文件的后缀名，获得文件类型
                            multipartBodyBuilder.addFormDataPart(k, file.getName(), RequestBody.create(MediaType.parse(fileType), file));
                        }
                        reqBody = multipartBodyBuilder.build();
                    }else
                    {
                        FormBody.Builder formBodyBuilder = new FormBody.Builder();
                        for (String k : queryMap.keySet())
                        {
                            String value = queryMap.get(k);
                            if (!TextUtils.isEmpty(value))
                            {
                                formBodyBuilder.add(k, value);
                            }
                        }
                        reqBody = formBodyBuilder.build();
                    }
                } else
                {
                    reqBody = buildEmptyBody();
                }
                builder = builder.url(url).post(reqBody);
            }
            return builder;
        }

        /**
         * 获取文件MimeType
         *
         * @param filename 文件名
         * @return
         */
        private static String getMimeType(String filename)
        {
            FileNameMap filenameMap = URLConnection.getFileNameMap();
            String contentType = filenameMap.getContentTypeFor(filename);
            if (contentType == null)
            {
                contentType = "application/octet-stream"; //* exe,所有的可执行程序
            }
            return contentType;
        }

        private RequestBody buildEmptyBody()
        {
            return RequestBody.create(null, new byte[0]);
        }

        /***
         * <b>传递请求</b>
         *
         * @param request
         * @param callback
         */
        private void deliveryRequest(OkHttpClient client, Request request, final Gson gson,
                                     final OkHttpResultCallback callback, final boolean needMain)
        {
            Call call = client.newCall(request);
            if (callback == null)
                throw new RuntimeException("callback can't be null");

            call.enqueue(new Callback()
            {

                @Override
                public void onFailure(@NonNull Call call, IOException e)
                {
                    failedResponse(e, callback, needMain);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException
                {
                    handleResponse(response, callback, needMain, gson);
                }
            });
        }

        private void failedResponse(final Exception e, final OkHttpResultCallback callback, boolean needMain)
        {
            if (callback != null)
            {
                final ApiException ee = GlobalErrorInflater.getError(e);

                if (needMain)
                {
                    if (mParams.reqAndRespHandler != null)
                    {
                        mParams.reqAndRespHandler.runOnMainThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                mParams.reqAndRespHandler.resultErrHandle(ee, callback);
                            }
                        });
                    }
                } else
                {
                    if (mParams.reqAndRespHandler != null)
                        mParams.reqAndRespHandler.resultErrHandle(ee, callback);
                }
            }
        }

        private void handleResponse(Response response, OkHttpResultCallback callback, boolean needMain, Gson gson)
        {
            try
            {
                int httpCode = response.code();

                if(response.isSuccessful())
                {
                    ResponseBody body = response.body();
                    if(body != null)
                    {
                        final String string = body.string();
//                        printLog("Response: " + string);
                        if (TextUtils.isEmpty(string))
                        {
                            failedResponse(new ApiException(CommonCode.SERVER_ERROR_EMPTY, CommonCode.SERVER_ERROR_EMPTY_MSG), callback, needMain);
                        }else
                        {
                            if (callback.mType == String.class)
                            {
                                successResponse(string, callback, needMain);
                            }else
                            {
                                if (callback.needVerifyResultCode())
                                {
                                    JSONObject obj = new JSONObject(string);
                                    if (callback.isSuccess(obj))
                                    {
                                        Object o = gson.fromJson(string, callback.mType);
                                        successResponse(o, callback, needMain);
                                    } else if (callback.needCustomReturn())
                                    {
                                        int serverCode = callback.getServerCode(obj);
                                        Class cs = callback.getCustomReturnClass(serverCode);
                                        if (cs != null)
                                        {
                                            Object o = gson.fromJson(string, cs);
                                            customResponse(o, serverCode, callback, needMain);
                                        }else
                                        {
                                            failedResponse(new ApiException(serverCode, callback.getServerFailedMessage(obj)), callback, needMain);
                                        }
                                    }else
                                    {
                                        failedResponse(new ApiException(callback.getServerCode(obj), callback.getServerFailedMessage(obj)), callback, needMain);
                                    }
                                } else
                                {
                                    //不验证，直接按输入类型转换对象
                                    Object o = gson.fromJson(string, callback.mType);
                                    successResponse(o, callback, needMain);
                                }
                            }
                        }
                    }else
                    {
                        failedResponse(new ApiException(CommonCode.SERVER_ERROR_EMPTY, CommonCode.SERVER_ERROR_EMPTY_MSG), callback, needMain);
                    }
                }else
                {
                    failedResponse(new ApiException(httpCode, CommonCode.NET_ERROR_MSG), callback, needMain);
                }
            } catch (Exception e)
            {
                failedResponse(e, callback, needMain);
            }
        }

        private void printLog(String msg)
        {
            if(mParams != null && mParams.reqAndRespHandler != null)
            {
                mParams.reqAndRespHandler.printLog(msg);
            }
        }

        private void successResponse(final Object object, final OkHttpResultCallback callback, boolean needMain)
        {
            if (callback != null)
            {
                if (needMain)
                {
                    if (mParams.reqAndRespHandler != null)
                    {
                        mParams.reqAndRespHandler.runOnMainThread(new Runnable()
                        {

                            @Override
                            public void run()
                            {
                                callback.onSuccess(object);
                            }
                        });
                    }
                } else
                {
                    callback.onSuccess(object);
                }
            }
        }

        private void customResponse(final Object object, final int serverCode, final OkHttpResultCallback callback, boolean needMain)
        {
            if (callback != null)
            {
                if (needMain)
                {
                    if (mParams.reqAndRespHandler != null)
                    {
                        mParams.reqAndRespHandler.runOnMainThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                callback.onCustomSuccess(serverCode, object);
                            }
                        });
                    }
                } else
                {
                    callback.onCustomSuccess(serverCode, object);
                }
            }
        }

    }

    public static class Params
    {
        public HttpReqAndRespHandler reqAndRespHandler;
        public Gson mGson;

        public GlobalHeaderInterceptor globalHeaderInterceptor;
        public GlobalExtraParamInterceptor globalParamInterceptor;
        public PreRequestInterceptor globalPreRequestInterceptor;

        public List<Interceptor> interceptors;

        public Logger logPrinter;
    }

    public static class ConfigBuilder
    {
        private Params p;

        public ConfigBuilder()
        {
            p = new Params();
        }

        public ConfigBuilder setRespHandler(HttpReqAndRespHandler h)
        {
            p.reqAndRespHandler = h;
            return this;
        }

        public ConfigBuilder setHeaderInterceptor(GlobalHeaderInterceptor i)
        {
            p.globalHeaderInterceptor = i;
            return this;
        }

        public ConfigBuilder setParamInterceptor(GlobalExtraParamInterceptor i)
        {
            p.globalParamInterceptor = i;
            return this;
        }

        public ConfigBuilder setPreReqInterceptor(PreRequestInterceptor i)
        {
            p.globalPreRequestInterceptor = i;
            return this;
        }

        public ConfigBuilder setGson(Gson g)
        {
            p.mGson = g;
            return this;
        }

        public ConfigBuilder setLogPrinter(Logger logPrinter)
        {
            p.logPrinter = logPrinter;
            return this;
        }



        public ConfigBuilder addInterceptors(Interceptor i)
        {
            if (p.interceptors == null)
                p.interceptors = new ArrayList<>();
            p.interceptors.add(i);
            return this;
        }

        public Params build()
        {
            return p;
        }
    }
}
