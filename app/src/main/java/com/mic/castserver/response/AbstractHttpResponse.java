package com.mic.castserver.response;

import static com.mic.castserver.StatusCode.HTTP_FORBIDDEN;
import static com.mic.castserver.StatusCode.HTTP_NOT_FOUND;
import static com.mic.castserver.StatusCode.HTTP_OK;
import static com.mic.castserver.StatusCode.HTTP_PARTIAL_CONTENT;
import static com.mic.castserver.StatusCode.HTTP_RANGE_NOT_SATISFIABLE;
import static com.mic.castserver.StatusCode.MIME_DEFAULT_BINARY;
import static com.mic.castserver.StatusCode.MIME_PLAIN_TEXT;
import android.net.Uri;
import android.text.TextUtils;

import com.mic.castserver.FileUtils;
import com.mic.castserver.ServerException;
import com.mic.castserver.ServerUtils;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Properties;


abstract class AbstractHttpResponse implements IResponse {

    private static final int BUFFER_SIZE = 64 * 1024;
    protected OutputStream out;
    protected InputStream is;
    protected InetAddress address;
    protected Properties header = new Properties();

    private final static ServerException SERVER_ERROR_EXCEPTION = new ServerException("BAD REQUEST");

    AbstractHttpResponse() {
    }

    AbstractHttpResponse(Socket socket) throws IOException {
        if (socket != null && socket.isConnected()) {
            this.out = socket.getOutputStream();
            this.is = socket.getInputStream();
            this.address = socket.getInetAddress();
        }
    }

    void addHeader(String name, String value) {
        if (header != null && !header.contains(name)) {
            header.put(name, value);
        }
    }

    void sendError(String status, String msg) throws IOException, ServerException {
        sendResponse(status, MIME_PLAIN_TEXT, null, new ByteArrayInputStream(msg.getBytes()));
    }

    void sendResponse(String status, String mime, Properties header, InputStream data) throws IOException, ServerException {

        if (status == null)
            throw SERVER_ERROR_EXCEPTION;

        PrintWriter pw = new PrintWriter(out);
        pw.print("HTTP/1.0 " + status + " \r\n");
        if (mime != null){
            pw.print("Content-Type: " + mime + "\r\n");
        }

        if (header != null) {
            Enumeration e = header.keys();
            while (e.hasMoreElements()) {
                String key = (String) e.nextElement();
                String value = header.getProperty(key);
                pw.print(key + ": " + value + "\r\n");
            }
        }

        pw.print("\r\n");
        pw.flush();

        if (data != null) {
            int pending = data.available();    // This is to support partial sends, see serveFile()
            byte[] buff = new byte[BUFFER_SIZE];
            while (pending > 0) {
                int read = data.read(buff, 0, ((pending > BUFFER_SIZE) ? BUFFER_SIZE : pending));
                if (read <= 0) break;
                out.write(buff, 0, read);
                pending -= read;
            }
        }
        out.flush();
        out.close();
        if (data != null)
            data.close();
    }

    HttpResponse serve(String uri, Properties header, File dir) {
        return serveFile(uri, header, dir);
    }

    private HttpResponse serveFile(String uri, Properties header, File homeDir) {
        HttpResponse res = null;

        // Remove URL arguments

        if(TextUtils.isEmpty(uri)) return null;
        uri = uri.trim().replace(File.separatorChar, '/');
        if (uri.indexOf('?') >= 0)
            uri = uri.substring(0, uri.indexOf('?'));

        try {
            uri = ServerUtils.decodePath(uri);
            if (uri.contains("///")) {
                uri = uri.split(":///")[1];
            }


        } catch (Exception e) {
            e.printStackTrace();
            res = new HttpResponse(HTTP_NOT_FOUND, MIME_PLAIN_TEXT,
                    "Error 404");
        }
        uri = Uri.decode(uri);
        File f = new File(homeDir, uri);

        if (res == null && (!f.exists() || !f.isFile())) {
            res = new HttpResponse(HTTP_NOT_FOUND, MIME_PLAIN_TEXT,
                    "Error 404");
        }

        try {
            if (res == null) {
                // Get MIME type from file name extension, if possible
                String mime;
                String extension = FileUtils.getExtension(f.getPath());
                if (extension != null)
                    extension = extension.toLowerCase();

                mime = ServerUtils.MIME_TYPES.get(extension);

                if (mime == null)
                    mime = MIME_DEFAULT_BINARY;
                // Support (simple) skipping:
                long startFrom = 0;
                long endAt = -1;
                String range = header.getProperty("range");
                if (range != null) {
                    if (range.startsWith("bytes=")) {
                        range = range.substring("bytes=".length());
                        int minus = range.indexOf('-');
                        try {
                            if (minus > 0 && !TextUtils.isEmpty(range)) {
                                startFrom = Long.parseLong(range.substring(0, minus));
                                endAt = Long.parseLong(range.substring(minus + 1));
                            }
                        } catch (NumberFormatException nfe) {
                            nfe.printStackTrace();
                        }
                    }
                }

                // Change return code and add Content-Range header when skipping is requested
                long fileLen = f.length();
                if (range != null && startFrom >= 0) {
                    if (startFrom >= fileLen) {
                        res = new HttpResponse(HTTP_RANGE_NOT_SATISFIABLE, MIME_PLAIN_TEXT, "");
                        res.addHeader("Content-Range", "bytes 0-0/" + fileLen);
                        if (mime.startsWith("application/"))
                            res.addHeader("Content-Disposition", "attachment; filename=\"" + f.getName() + "\"");
                    } else {
                        if (endAt < 0)
                            endAt = fileLen - 1;
                        long newLen = endAt - startFrom + 1;
                        if (newLen < 0) newLen = 0;

                        final long dataLen = newLen;
                        FileInputStream fis = new FileInputStream(f) {
                            public int available() {
                                return (int) dataLen;
                            }
                        };
                        final long skip = fis.skip(startFrom);

                        res = new HttpResponse(HTTP_PARTIAL_CONTENT, mime, new BufferedInputStream(fis, BUFFER_SIZE));
                        res.addHeader("Access-Control-Allow-Origin", "*");
                        res.addHeader("Content-Length", "" + dataLen);
                        res.addHeader("Content-Range", "bytes " + startFrom + "-" + endAt + "/" + fileLen);
                        if (mime.startsWith("application/"))
                            res.addHeader("Content-Disposition", "attachment; filename=\"" + f.getName() + "\"");
                    }
                } else {
                    res = new HttpResponse(HTTP_OK, mime, new BufferedInputStream(new FileInputStream(f), BUFFER_SIZE));
                    res.addHeader("Access-Control-Allow-Origin", "*");
                    res.addHeader("Content-Length", "" + fileLen);
                    if (mime.startsWith("application/"))
                        res.addHeader("Content-Disposition", "attachment; filename=\"" + f.getName() + "\"");
                }
            }
        } catch (IOException ioe) {
            res = new HttpResponse(HTTP_FORBIDDEN, MIME_PLAIN_TEXT, "FORBIDDEN");
        }

        res.addHeader("Accept-Ranges", "bytes"); // Announce that the file server accepts partial content requestes
        return res;
    }
}
