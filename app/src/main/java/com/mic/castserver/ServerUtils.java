package com.mic.castserver;


import android.net.Uri;
import android.text.TextUtils;
import android.util.Base64;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;


public class ServerUtils {

    public static final HashMap<String, String> MIME_TYPES = new HashMap<>();

    static {
        MIME_TYPES.put("mp4", "video/mp4");
        MIME_TYPES.put("3gp", "video/3gpp");
        MIME_TYPES.put("webm", "video/webm");
        MIME_TYPES.put("mov", "video/quicktime");
        MIME_TYPES.put("m4v", "video/mp4");
        MIME_TYPES.put("mkv", "video/x-matroska");
        MIME_TYPES.put("png", "image/png");
    }

    public static String encodePath(String path) {
        if(TextUtils.isEmpty(path)) return "";
        String extension = FileUtils.getExtension(path);
        if(TextUtils.isEmpty(extension)) return "";
        byte[] encode = Base64.encode(path.getBytes(), Base64.NO_WRAP);
        for (int i = 0; i < encode.length; i++) {
            byte b = encode[i];
            encode[i] = (byte) (b ^ 0xff);
        }

        return "/" + EncUtil.parseByte2HexStr(encode) + "." + extension;
    }

    public static String decodePath(String encode) {
        if(TextUtils.isEmpty(encode)) return "";
        String extension = FileUtils.getExtension(encode);
        if(TextUtils.isEmpty(extension)) return "";
        byte[] bytes = EncUtil.toByte(encode.substring(1, encode.length() - extension.length() - 1));
        for (int i = 0; i < bytes.length; i++) {
            byte b = bytes[i];
            bytes[i] = (byte) (b ^ 0xff);
        }

        byte[] decode = Base64.decode(bytes, Base64.NO_WRAP);
        return new String(decode);
    }

    public static Uri filter(Uri uri) {
        if(uri==null) return null;
        String LEGAL_EXT[] = {"mp4", "mkv", "3gp", "webm", "mov", "m4v"};
        String LEGAL_EXT_STREAM[] = {"m3u8", "mpd", "ism"};

        String scheme = uri.getScheme();
        if (TextUtils.isEmpty(scheme)) {
            return null;
        }
        String path = uri.getPath();
        if (TextUtils.isEmpty(path))
            return null;

        scheme = scheme.toLowerCase();

        File file = new File(path);

        if ("file".equals(scheme)) {
            String ext = ext(file.getName());
            for (String str : LEGAL_EXT) {
                if (str.equalsIgnoreCase(ext)) {
                    return uri;
                }
            }

            return null;
        }else {
            if (!scheme.contains("http"))
                return null;

            String ext = ext(file.getName());
            for (String str : LEGAL_EXT_STREAM) {
                if (str.equalsIgnoreCase(ext)) {
                    return uri;
                }
            }
        }

        return null;
    }

    public static Uri[] filter(Uri[] uriList) {
        if(uriList==null || uriList.length==0) return null;
        ArrayList<Uri> list = new ArrayList<>();
        for (Uri uri : uriList) {
            Uri filter = filter(uri);
            if (filter != null) {
                list.add(filter);
            }
        }

        return list.toArray(new Uri[0]);
    }

    public static String contentType(Uri uri) {
        String contentType = "video/mp4";

        if (uri==null ||uri.getScheme() == null||uri.getPath()==null)
            return contentType;

        File file = new File(uri.getPath());
        String extension = FileUtils.getExtension(file.getPath());
        if (extension != null)
            extension = extension.toLowerCase(Locale.getDefault());

        if (uri.getScheme().equals("file")) {
            contentType = MIME_TYPES.get(extension);
            if (TextUtils.isEmpty(contentType)) {
                contentType = "video/mp4";
            }
        }else if ("m3u8".equals(extension)) {
            contentType = "application/x-mpegURL";
        }else if ("mpd".equals(extension)) {
            contentType = "application/dash+xml";
        }else if ("ism".equals(extension)) {
            contentType = "application/vnd.ms-sstr+xml";
        }
        else if (extension != null) {
            contentType = "video/" + extension;
        }

        return contentType;
    }

    private static String ext(String name) {
        if (TextUtils.isEmpty(name))
            return "";

        int i = name.lastIndexOf('.');
        if (i == -1)
            return "";

        return name.substring(i+1);
    }

    public static String getRootDir(){
        return "/";
    }

}
