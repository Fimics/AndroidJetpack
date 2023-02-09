package com.mic.server.request;

import static com.mic.server.Constant.QUERY_STRING_PARAMETER;

import android.util.Log;

import com.mic.server.response.Response;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class Decoder {

    private static final String TAG="decoder";

    protected static Map<String, List<String>> decodeParameters(Map<String, String> parms) {
        return decodeParameters(parms.get(QUERY_STRING_PARAMETER));
    }

    protected static Map<String, List<String>> decodeParameters(String queryString) {
        Map<String, List<String>> parms = new HashMap<String, List<String>>();
        if (queryString != null) {
            StringTokenizer st = new StringTokenizer(queryString, "&");
            while (st.hasMoreTokens()) {
                String e = st.nextToken();
                int sep = e.indexOf('=');
                String propertyName = sep >= 0 ? decodePercent(e.substring(0, sep)).trim() : decodePercent(e).trim();
                if (!parms.containsKey(propertyName)) {
                    parms.put(propertyName, new ArrayList<String>());
                }
                String propertyValue = sep >= 0 ? decodePercent(e.substring(sep + 1)) : null;
                if (propertyValue != null) {
                    parms.get(propertyName).add(propertyValue);
                }
            }
        }
        return parms;
    }

    public static String decodePercent(String str) {
        String decoded = null;
        try {
            decoded = URLDecoder.decode(str, "UTF8");
        } catch (UnsupportedEncodingException e) {
            Log.d(TAG, "Encoding not supported ->"+e.getMessage());
        }
        return decoded;
    }

    public static boolean useGzipWhenAccepted(Response r) {
        return r.getMimeType() != null && r.getMimeType().toLowerCase().contains("text/");
    }
}
