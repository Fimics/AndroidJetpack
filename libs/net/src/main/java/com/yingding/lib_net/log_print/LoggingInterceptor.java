package com.yingding.lib_net.log_print;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.platform.Platform;


public class LoggingInterceptor implements Interceptor
{

    private final boolean isDebug;
    private final Builder builder;

    private LoggingInterceptor(Builder builder)
    {
        this.builder = builder;
        this.isDebug = builder.isDebug;
    }

    @Override
    public Response intercept(@NonNull Chain chain) throws IOException
    {
        Request request = chain.request();

        if (!isDebug || builder.getLevel() == Level.NONE)
        {
            return chain.proceed(request);
        }

        final RequestBody requestBody = request.body();

        String rSubtype = null;

        if (requestBody != null && requestBody.contentType() != null)
        {
            rSubtype = requestBody.contentType().subtype();
        }

        Executor executor = builder.executor;

        if (isNotFileRequest(rSubtype))
        {
            if (executor != null)
            {
                executor.execute(createPrintJsonRequestRunnable(builder, request));
            } else
            {
                Printer.printJsonRequest(builder, request);
            }
        } else
        {
            if (executor != null)
            {
                executor.execute(createFileRequestRunnable(builder, request));
            } else
            {
                Printer.printFileRequest(builder, request);
            }
        }

        final long st = System.nanoTime();
        final Response response;

        /*try
        {
            TimeUnit.MILLISECONDS.sleep(builder.sleepMs);
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        response = new Response.Builder()
                .body(ResponseBody.create(MediaType.parse("application/json"), builder.listener.getJsonResponse(request)))
                .request(chain.request())
                .protocol(Protocol.HTTP_2)
                .message("Mock")
                .code(200)
                .build();*/

        response = chain.proceed(request);

        final long chainMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - st);

        final List<String> segmentList = request.url().encodedPathSegments();
        final String header = response.headers().toString();
        final int code = response.code();
        final boolean isSuccessful = response.isSuccessful();
        final String message = response.message();

        final ResponseBody responseBody = response.body();
        final MediaType contentType = responseBody != null ? responseBody.contentType() : null;

        String subtype = null;
        final ResponseBody body;

        if (contentType != null)
        {
            subtype = contentType.subtype();
        }

        if (isNotFileRequest(subtype))
        {
            final String bodyString = Printer.getJsonString(responseBody != null ? responseBody.string() : "");
            final String url = response.request().url().toString();

            if (executor != null)
            {
                executor.execute(createPrintJsonResponseRunnable(builder, chainMs, isSuccessful, code, header, bodyString,
                        segmentList, message, url));
            } else
            {
                Printer.printJsonResponse(builder, chainMs, isSuccessful, code, header, bodyString,
                        segmentList, message, url);
            }
            body = ResponseBody.create(contentType, bodyString);
        } else
        {
            if (executor != null)
            {
                executor.execute(createFileResponseRunnable(builder, chainMs, isSuccessful, code, header, segmentList, message));
            } else
            {
                Printer.printFileResponse(builder, chainMs, isSuccessful, code, header, segmentList, message);
            }
            return response;
        }

        return response.newBuilder().
                body(body).
                build();
    }

    private boolean isNotFileRequest(final String subtype)
    {
        if(subtype == null) return true;

        return subtype.contains("json")
                || subtype.contains("xml")
                || subtype.contains("plain")
                || subtype.contains("html")
                || subtype.contains("x-www-form-urlencoded");
    }

    private static Runnable createPrintJsonRequestRunnable(final Builder builder, final Request request)
    {
        return new Runnable()
        {
            @Override
            public void run()
            {
                Printer.printJsonRequest(builder, request);
            }
        };
    }

    private static Runnable createFileRequestRunnable(final Builder builder, final Request request)
    {
        return new Runnable()
        {
            @Override
            public void run()
            {
                Printer.printFileRequest(builder, request);
            }
        };
    }

    private static Runnable createPrintJsonResponseRunnable(final Builder builder, final long chainMs, final boolean isSuccessful,
                                                            final int code, final String headers, final String bodyString, final List<String> segments, final String message, final String responseUrl)
    {
        return new Runnable()
        {
            @Override
            public void run()
            {
                Printer.printJsonResponse(builder, chainMs, isSuccessful, code, headers, bodyString, segments, message, responseUrl);
            }
        };
    }

    private static Runnable createFileResponseRunnable(final Builder builder, final long chainMs, final boolean isSuccessful,
                                                       final int code, final String headers, final List<String> segments, final String message)
    {
        return new Runnable()
        {
            @Override
            public void run()
            {
                Printer.printFileResponse(builder, chainMs, isSuccessful, code, headers, segments, message);
            }
        };
    }

    @SuppressWarnings({"unused", "SameParameterValue"})
    public static class Builder
    {

        private static String TAG = "LoggingI";
        private boolean isLogHackEnable = false;
        private boolean isDebug;
        private int type = Platform.INFO;
        private String requestTag;
        private String responseTag;
        private Level level = Level.BASIC;
        private Logger logger;
        private Executor executor;

        public Builder()
        {
        }

        int getType()
        {
            return type;
        }

        Level getLevel()
        {
            return level;
        }

        /**
         * @param level set log level
         * @return Builder
         * @see Level
         */
        public Builder setLevel(Level level)
        {
            this.level = level;
            return this;
        }

        String getTag(boolean isRequest)
        {
            if (isRequest)
            {
                return TextUtils.isEmpty(requestTag) ? TAG : requestTag;
            } else
            {
                return TextUtils.isEmpty(responseTag) ? TAG : responseTag;
            }
        }

        Logger getLogger()
        {
            return logger;
        }

        Executor getExecutor()
        {
            return executor;
        }

        boolean isLogHackEnable()
        {
            return isLogHackEnable;
        }

        /**
         * Set request and response each log tag
         *
         * @param tag general log tag
         * @return Builder
         */
        public Builder tag(String tag)
        {
            TAG = tag;
            return this;
        }

        /**
         * Set request log tag
         *
         * @param tag request log tag
         * @return Builder
         */
        public Builder request(String tag)
        {
            this.requestTag = tag;
            return this;
        }

        /**
         * Set response log tag
         *
         * @param tag response log tag
         * @return Builder
         */
        public Builder response(String tag)
        {
            this.responseTag = tag;
            return this;
        }

        /**
         * @param isDebug set can sending log output
         * @return Builder
         */
        public Builder loggable(boolean isDebug)
        {
            this.isDebug = isDebug;
            return this;
        }

        /**
         * @param type set sending log output type
         * @return Builder
         * @see Platform
         */
        public Builder log(int type)
        {
            this.type = type;
            return this;
        }

        /**
         * @param logger manuel logging interface
         * @return Builder
         * @see Logger
         */
        public Builder logger(Logger logger)
        {
            this.logger = logger;
            return this;
        }

        /**
         * @param executor manual executor for printing
         * @return Builder
         * @see Logger
         */
        public Builder executor(Executor executor)
        {
            this.executor = executor;
            return this;
        }

        /**
         * Call this if you want to have formatted pretty output in Android Studio logCat.
         * By default this 'hack' is not applied.
         *
         * @param useHack setup builder to use hack for Android Studio v3+ in order to have nice
         *                output as it was in previous A.S. versions.
         * @return Builder
         * @see Logger
         */
        public Builder enableAndroidStudio_v3_LogsHack(final boolean useHack)
        {
            isLogHackEnable = useHack;
            return this;
        }

        public LoggingInterceptor build()
        {
            return new LoggingInterceptor(this);
        }
    }

}