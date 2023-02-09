package com.mic.server.request;

import com.mic.server.response.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class CookieHandler implements Iterable<String> {

        private final HashMap<String, String> cookies = new HashMap<String, String>();

        private final ArrayList<Cookie> queue = new ArrayList<Cookie>();

        public CookieHandler(Map<String, String> httpHeaders) {
            String raw = httpHeaders.get("cookie");
            if (raw != null) {
                String[] tokens = raw.split(";");
                for (String token : tokens) {
                    String[] data = token.trim().split("=");
                    if (data.length == 2) {
                        this.cookies.put(data[0], data[1]);
                    }
                }
            }
        }

        public void delete(String name) {
            set(name, "-delete-", -30);
        }

        @Override
        public Iterator<String> iterator() {
            return this.cookies.keySet().iterator();
        }

        public String read(String name) {
            return this.cookies.get(name);
        }

        public void set(Cookie cookie) {
            this.queue.add(cookie);
        }

        public void set(String name, String value, int expires) {
            this.queue.add(new Cookie(name, value, Cookie.getHTTPTime(expires)));
        }

        public void unloadQueue(Response response) {
            for (Cookie cookie : this.queue) {
                response.addHeader("Set-Cookie", cookie.getHTTPHeader());
            }
        }
    }
