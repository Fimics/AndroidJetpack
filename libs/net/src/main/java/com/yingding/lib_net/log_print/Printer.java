package com.yingding.lib_net.log_print;


import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okio.Buffer;

class Printer
{

    private static final int JSON_INDENT = 3;
    private static final int MAX_LOG_LENGTH = 2000;

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final String DOUBLE_SEPARATOR = LINE_SEPARATOR + LINE_SEPARATOR;

    private static final String[] OMITTED_RESPONSE = {LINE_SEPARATOR, "Omitted response body"};
    private static final String[] OMITTED_REQUEST = {LINE_SEPARATOR, "Omitted request body"};

    private static final String N = "\n";
    private static final String T = "\t";
    private static final String REQUEST_UP_LINE = "┌────── Request ────────────────────────────────────────────────────────────────────────";
    private static final String END_LINE = "└───────────────────────────────────────────────────────────────────────────────────────";
    private static final String RESPONSE_UP_LINE = "┌────── Response ───────────────────────────────────────────────────────────────────────";
    private static final String BODY_TAG = "Body:";
    private static final String URL_TAG = "URL: ";
    private static final String METHOD_TAG = "Method: @";
    private static final String HEADERS_TAG = "Headers:";
    private static final String STATUS_CODE_TAG = "Status Code: ";
    private static final String RECEIVED_TAG = "Received in: ";
    private static final String CORNER_UP = "┌ ";
    private static final String CORNER_BOTTOM = "└ ";
    private static final String CENTER_LINE = "├ ";
    private static final String DEFAULT_LINE = "│ ";
    private static final String OOM_OMITTED = LINE_SEPARATOR + "Output omitted because of Object size.";

    protected Printer()
    {
        throw new UnsupportedOperationException();
    }

    static void printJsonRequest(LoggingInterceptor.Builder builder, Request request)
    {
        String requestBody = LINE_SEPARATOR + BODY_TAG + LINE_SEPARATOR + bodyToString(request);
        String tag = builder.getTag(true);
        if (builder.getLogger() == null)
            I.log(builder.getType(), tag, REQUEST_UP_LINE, builder.isLogHackEnable());
        logLines(builder.getType(), tag, new String[]{URL_TAG + request.url()}, builder.getLogger(), false, builder.isLogHackEnable());
        logLines(builder.getType(), tag, getRequest(request, builder.getLevel()), builder.getLogger(), true, builder.isLogHackEnable());
        if (builder.getLevel() == Level.BASIC || builder.getLevel() == Level.BODY)
        {
            logLines(builder.getType(), tag, requestBody.split(LINE_SEPARATOR), builder.getLogger(), true, builder.isLogHackEnable());
        }
        if (builder.getLogger() == null)
            I.log(builder.getType(), tag, END_LINE, builder.isLogHackEnable());
    }

    private static String bodyToString(final Request request)
    {
        try
        {
            final Request copy = request.newBuilder().build();
            final Buffer buffer = new Buffer();
            RequestBody body = copy.body();
            if (body == null)
                return "";
            if (body instanceof FormBody)
            {
                FormBody bod = (FormBody) body;
                StringBuilder sb = new StringBuilder();
                if(bod.size() > 0)
                {
                    sb.append("{");
                    for (int i = 0; i < bod.size(); i++)
                    {
                        String k = bod.encodedName(i);
                        String v = bod.encodedValue(i);
                        sb.append("\"")
                                .append(k)
                                .append("\"")
                                .append(":")
                                .append("\"")
                                .append(v)
                                .append("\"")
                                .append(",");
                    }
                    sb.delete(sb.length()-1, sb.length());
                    sb.append("}");
                    return getJsonString(sb.toString());
                }
                return "";
            }else
            {
                body.writeTo(buffer);
                return getJsonString(buffer.readUtf8());
            }
        } catch (final IOException e)
        {
            return "{\"err\": \"" + e.getMessage() + "\"}";
        }
    }

    private static void logLines(int type, String tag, String[] lines, Logger logger,
                                 boolean withLineSize, boolean useLogHack)
    {
        for (String line : lines)
        {
            int lineLength = line.length();
            int MAX_LONG_SIZE = withLineSize ? MAX_LOG_LENGTH : lineLength;
//            int MAX_LONG_SIZE = withLineSize ? 110 : lineLength;

            int resizeLen = lineLength / MAX_LONG_SIZE;
            if(resizeLen > 0)
            {
                for (int i = 0; i <= resizeLen; i++)
                {
                    int start = i * MAX_LONG_SIZE;
                    int end = (i + 1) * MAX_LONG_SIZE;
                    end = end > line.length() ? line.length() : end;
                    if (logger == null)
                    {
                        I.log(type, tag, DEFAULT_LINE + line.substring(start, end), useLogHack);
                    } else
                    {
                        logger.log(type, tag, line.substring(start, end));
                    }
                }
            }else
            {
                if (logger == null)
                {
                    I.log(type, tag, DEFAULT_LINE + line, useLogHack);
                } else
                {
                    logger.log(type, tag, line);
                }
            }
        }
    }

    private static String[] getRequest(Request request, Level level)
    {
        String log;
        String header = request.headers().toString();
        boolean loggableHeader = level == Level.HEADERS || level == Level.BASIC;
        log = METHOD_TAG + request.method() + DOUBLE_SEPARATOR +
                (isEmpty(header) ? "" : loggableHeader ? HEADERS_TAG + LINE_SEPARATOR + dotHeaders(header) : "");
        return log.split(LINE_SEPARATOR);
    }

    static String getJsonString(final String msg)
    {
        String message;
        try
        {
            if (msg.startsWith("{"))
            {
                JSONObject jsonObject = new JSONObject(msg);
                message = jsonObject.toString(JSON_INDENT);
            } else if (msg.startsWith("["))
            {
                JSONArray jsonArray = new JSONArray(msg);
                message = jsonArray.toString(JSON_INDENT);
            } else
            {
                message = msg;
            }
        } catch (JSONException e)
        {
            message = msg;
        } catch (OutOfMemoryError e1)
        {
            message = OOM_OMITTED;
        }
        return message;
    }

    private static boolean isEmpty(String line)
    {
        return TextUtils.isEmpty(line) || N.equals(line) || T.equals(line) || TextUtils.isEmpty(line.trim());
    }

    private static String dotHeaders(String header)
    {
        String[] headers = header.split(LINE_SEPARATOR);
        StringBuilder builder = new StringBuilder();
        String tag = "─ ";
        if (headers.length > 1)
        {
            for (int i = 0; i < headers.length; i++)
            {
                if (i == 0)
                {
                    tag = CORNER_UP;
                } else if (i == headers.length - 1)
                {
                    tag = CORNER_BOTTOM;
                } else
                {
                    tag = CENTER_LINE;
                }
                builder.append(tag).append(headers[i]).append("\n");
            }
        } else
        {
            for (String item : headers)
            {
                builder.append(tag).append(item).append("\n");
            }
        }
        return builder.toString();
    }

    static void printJsonResponse(LoggingInterceptor.Builder builder, long chainMs, boolean isSuccessful,
                                  int code, String headers, String bodyString, List<String> segments, String message, final String responseUrl)
    {
        final String responseBody = LINE_SEPARATOR + BODY_TAG + LINE_SEPARATOR + getJsonString(bodyString);
        final String tag = builder.getTag(false);
        final String[] urlLine = {URL_TAG + responseUrl, N};
        final String[] response = getResponse(headers, chainMs, code, isSuccessful,
                builder.getLevel(), segments, message);

        if (builder.getLogger() == null)
        {
            I.log(builder.getType(), tag, RESPONSE_UP_LINE, builder.isLogHackEnable());
        }

        logLines(builder.getType(), tag, urlLine, builder.getLogger(), false, builder.isLogHackEnable());
        logLines(builder.getType(), tag, response, builder.getLogger(), true, builder.isLogHackEnable());

        if (builder.getLevel() == Level.BASIC || builder.getLevel() == Level.BODY)
        {
            logLines(builder.getType(), tag, responseBody.split(LINE_SEPARATOR), builder.getLogger(),
                    true, builder.isLogHackEnable());
        }
        if (builder.getLogger() == null)
        {
            I.log(builder.getType(), tag, END_LINE, builder.isLogHackEnable());
        }
    }

    private static String[] getResponse(String header, long tookMs, int code, boolean isSuccessful,
                                        Level level, List<String> segments, String message)
    {
        String log;
        boolean loggableHeader = level == Level.HEADERS || level == Level.BASIC;
        String segmentString = slashSegments(segments);
        log = ((!TextUtils.isEmpty(segmentString) ? segmentString + " - " : "") + "is success : "
                + isSuccessful + " - " + RECEIVED_TAG + tookMs + "ms" + DOUBLE_SEPARATOR + STATUS_CODE_TAG +
                code + " / " + message + DOUBLE_SEPARATOR + (isEmpty(header) ? "" : loggableHeader ? HEADERS_TAG + LINE_SEPARATOR +
                dotHeaders(header) : ""));
        return log.split(LINE_SEPARATOR);
    }

    private static String slashSegments(List<String> segments)
    {
        StringBuilder segmentString = new StringBuilder();
        for (String segment : segments)
        {
            segmentString.append("/").append(segment);
        }
        return segmentString.toString();
    }

    static void printFileRequest(LoggingInterceptor.Builder builder, Request request)
    {
        String tag = builder.getTag(true);
        if (builder.getLogger() == null)
            I.log(builder.getType(), tag, REQUEST_UP_LINE, builder.isLogHackEnable());
        logLines(builder.getType(), tag, new String[]{URL_TAG + request.url()}, builder.getLogger(),
                false, builder.isLogHackEnable());
        logLines(builder.getType(), tag, getRequest(request, builder.getLevel()), builder.getLogger(),
                true, builder.isLogHackEnable());
        if (builder.getLevel() == Level.BASIC || builder.getLevel() == Level.BODY)
        {
            final Request copy = request.newBuilder().build();
            RequestBody body = copy.body();
             if(body instanceof MultipartBody)
            {
                MultipartBody bod = (MultipartBody) body;
                List<MultipartBody.Part> parts = bod.parts();
                if(parts.size() > 0)
                {
                    for (MultipartBody.Part p : parts)
                    {
                        StringBuilder sb = new StringBuilder();
                        Headers head = p.headers();
                        if(head != null)
                        {
                            Set<String> ns = head.names();
                            for (String s : ns)
                            {
                                String hv = head.get(s);
                                sb.append(s).append("--").append(hv);
                            }
                        }

                        RequestBody rb = p.body();
                        if(rb != null)
                        {
                            try
                            {
//                                final MediaType contentType = rb.contentType();
//                                String subtype = null;
//                                if (contentType != null)
//                                {
//                                    subtype = contentType.subtype();
//                                }
                                if(rb.contentLength() < 1000)
                                {
                                    Buffer buf = new Buffer();
                                    rb.writeTo(buf);
                                    sb.append("==>").append(buf.readUtf8());
                                }else
                                {
                                    sb.append("==>").append("Omitted content");
                                }
                            } catch (IOException e)
                            {
                                e.printStackTrace();
                            }
                        }
                        logLines(builder.getType(), tag, new String[]{sb.toString()}, builder.getLogger(), false, builder.isLogHackEnable());
                    }
                }
            }
//            logLines(builder.getType(), tag, OMITTED_REQUEST, builder.getLogger(), true, builder.isLogHackEnable());
        }
        if (builder.getLogger() == null)
            I.log(builder.getType(), tag, END_LINE, builder.isLogHackEnable());
    }

    private static boolean isNotFileRequest(final String subtype)
    {
        if(subtype == null) return true;

        return subtype.contains("json")
                || subtype.contains("xml")
                || subtype.contains("plain")
                || subtype.contains("html")
                || subtype.contains("x-www-form-urlencoded");
    }

    static void printFileResponse(LoggingInterceptor.Builder builder, long chainMs, boolean isSuccessful,
                                  int code, String headers, List<String> segments, String message)
    {
        String tag = builder.getTag(false);
        if (builder.getLogger() == null)
            I.log(builder.getType(), tag, RESPONSE_UP_LINE, builder.isLogHackEnable());
        logLines(builder.getType(), tag, getResponse(headers, chainMs, code, isSuccessful,
                builder.getLevel(), segments, message), builder.getLogger(), true, builder.isLogHackEnable());
        logLines(builder.getType(), tag, OMITTED_RESPONSE, builder.getLogger(), true, builder.isLogHackEnable());
        if (builder.getLogger() == null)
            I.log(builder.getType(), tag, END_LINE, builder.isLogHackEnable());
    }
}