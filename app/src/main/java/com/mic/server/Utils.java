package com.mic.server;


import com.mic.server.client.NanoHTTPD;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Utils {

    protected static Map<String, String> MIME_TYPES;
    private static final Logger LOG = Logger.getLogger(NanoHTTPD.class.getName());

    public static Map<String, String> mimeTypes() {
        if (MIME_TYPES == null) {
            MIME_TYPES = new HashMap<String, String>();
            loadMimeTypes(MIME_TYPES, "META-INF/nanohttpd/default-mimetypes.properties");
            loadMimeTypes(MIME_TYPES, "META-INF/nanohttpd/mimetypes.properties");
            if (MIME_TYPES.isEmpty()) {
                LOG.log(Level.WARNING, "no mime types found in the classpath! please provide mimetypes.properties");
            }
        }
        return MIME_TYPES;
    }

    private static void loadMimeTypes(Map<String, String> result, String resourceName) {
        try {
            Enumeration<URL> resources = NanoHTTPD.class.getClassLoader().getResources(resourceName);
            while (resources.hasMoreElements()) {
                URL url = (URL) resources.nextElement();
                Properties properties = new Properties();
                InputStream stream = null;
                try {
                    stream = url.openStream();
                    properties.load(url.openStream());
                } catch (IOException e) {
                    LOG.log(Level.SEVERE, "could not load mimetypes from " + url, e);
                } finally {
                    safeClose(stream);
                }
                result.putAll((Map) properties);
            }
        } catch (IOException e) {
            LOG.log(Level.INFO, "no mime types available at " + resourceName);
        }
    };

    public static String getMimeTypeForFile(String uri) {
        int dot = uri.lastIndexOf('.');
        String mime = null;
        if (dot >= 0) {
            mime = mimeTypes().get(uri.substring(dot + 1).toLowerCase());
        }
        return mime == null ? "application/octet-stream" : mime;
    }


    public static final void safeClose(Object closeable) {
        try {
            if (closeable != null) {
                if (closeable instanceof Closeable) {
                    ((Closeable) closeable).close();
                } else if (closeable instanceof Socket) {
                    ((Socket) closeable).close();
                } else if (closeable instanceof ServerSocket) {
                    ((ServerSocket) closeable).close();
                } else {
                    throw new IllegalArgumentException("Unknown object to close");
                }
            }
        } catch (IOException e) {
           LOG.log(Level.SEVERE, "Could not close", e);
        }
    }

    public static boolean headerAlreadySent(Map<String, String> header, String name) {
        boolean alreadySent = false;
        for (String headerName : header.keySet()) {
            alreadySent |= headerName.equalsIgnoreCase(name);
        }
        return alreadySent;
    }
}
