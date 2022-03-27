package com.mic.server.http;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.zip.GZIPOutputStream;

public class Response implements Closeable {

    public interface IStatus {

        String getDescription();

        int getRequestStatus();
    }


    public enum Status implements IStatus {
        SWITCH_PROTOCOL(101, "Switching Protocols"),
        OK(200, "OK"),
        CREATED(201, "Created"),
        ACCEPTED(202, "Accepted"),
        NO_CONTENT(204, "No Content"),
        PARTIAL_CONTENT(206, "Partial Content"),
        REDIRECT(301, "Moved Permanently"),
        NOT_MODIFIED(304, "Not Modified"),
        BAD_REQUEST(400, "Bad Request"),
        UNAUTHORIZED(401, "Unauthorized"),
        FORBIDDEN(403, "Forbidden"),
        NOT_FOUND(404, "Not Found"),
        METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
        NOT_ACCEPTABLE(406, "Not Acceptable"),
        REQUEST_TIMEOUT(408, "Request Timeout"),
        CONFLICT(409, "Conflict"),
        RANGE_NOT_SATISFIABLE(416, "Requested Range Not Satisfiable"),
        INTERNAL_ERROR(500, "Internal Server Error"),
        NOT_IMPLEMENTED(501, "Not Implemented"),
        UNSUPPORTED_HTTP_VERSION(505, "HTTP Version Not Supported");

        private final int requestStatus;

        private final String description;

        Status(int requestStatus, String description) {
            this.requestStatus = requestStatus;
            this.description = description;
        }

        @Override
        public String getDescription() {
            return "" + this.requestStatus + " " + this.description;
        }

        @Override
        public int getRequestStatus() {
            return this.requestStatus;
        }

    }

    private static class ChunkedOutputStream extends FilterOutputStream {

        public ChunkedOutputStream(OutputStream out) {
            super(out);
        }

        @Override
        public void write(int b) throws IOException {
            byte[] data = {
                    (byte) b
            };
            write(data, 0, 1);
        }

        @Override
        public void write(byte[] b) throws IOException {
            write(b, 0, b.length);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            if (len == 0)
                return;
            out.write(String.format("%x\r\n", len).getBytes());
            out.write(b, off, len);
            out.write("\r\n".getBytes());
        }

        public void finish() throws IOException {
            out.write("0\r\n\r\n".getBytes());
        }

    }


    private IStatus status;

    private String mimeType;

    private InputStream data;

    private long contentLength;

    private final Map<String, String> header = new HashMap<String, String>();

    private Method requestMethod;

    private boolean chunkedTransfer;

    private boolean encodeAsGzip;

    private boolean keepAlive;

    protected Response(IStatus status, String mimeType, InputStream data, long totalBytes) {
        this.status = status;
        this.mimeType = mimeType;
        if (data == null) {
            this.data = new ByteArrayInputStream(new byte[0]);
            this.contentLength = 0L;
        } else {
            this.data = data;
            this.contentLength = totalBytes;
        }
        this.chunkedTransfer = this.contentLength < 0;
        keepAlive = true;
    }

    @Override
    public void close() throws IOException {
        if (this.data != null) {
            this.data.close();
        }
    }

    public void addHeader(String name, String value) {
        this.header.put(name, value);
    }

    public InputStream getData() {
        return this.data;
    }

    public String getHeader(String name) {
        for (String headerName : header.keySet()) {
            if (headerName.equalsIgnoreCase(name)) {
                return header.get(headerName);
            }
        }
        return null;
    }

    public String getMimeType() {
        return this.mimeType;
    }

    public Method getRequestMethod() {
        return this.requestMethod;
    }

    public IStatus getStatus() {
        return this.status;
    }

    public void setGzipEncoding(boolean encodeAsGzip) {
        this.encodeAsGzip = encodeAsGzip;
    }

    public void setKeepAlive(boolean useKeepAlive) {
        this.keepAlive = useKeepAlive;
    }

    protected void send(OutputStream outputStream) {
        String mime = this.mimeType;
        SimpleDateFormat gmtFrmt = new SimpleDateFormat("E, d MMM yyyy HH:mm:ss 'GMT'", Locale.US);
        gmtFrmt.setTimeZone(TimeZone.getTimeZone("GMT"));

        try {
            if (this.status == null) {
                throw new Error("sendResponse(): Status can't be null.");
            }
            PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8")), false);
            pw.print("HTTP/1.1 " + this.status.getDescription() + " \r\n");

            if (mime != null) {
                pw.print("Content-Type: " + mime + "\r\n");
            }

            if (this.header == null || this.header.get("Date") == null) {
                pw.print("Date: " + gmtFrmt.format(new Date()) + "\r\n");
            }

            if (this.header != null) {
                for (String key : this.header.keySet()) {
                    String value = this.header.get(key);
                    pw.print(key + ": " + value + "\r\n");
                }
            }

            if (!Utils.headerAlreadySent(header, "connection")) {
                pw.print("Connection: " + (this.keepAlive ? "keep-alive" : "close") + "\r\n");
            }

            if (Utils.headerAlreadySent(this.header, "content-length")) {
                encodeAsGzip = false;
            }

            if (encodeAsGzip) {
                pw.print("Content-Encoding: gzip\r\n");
                setChunkedTransfer(true);
            }

            long pending = this.data != null ? this.contentLength : 0;
            if (this.requestMethod != Method.HEAD && this.chunkedTransfer) {
                pw.print("Transfer-Encoding: chunked\r\n");
            } else if (!encodeAsGzip) {
                pending = sendContentLengthHeaderIfNotAlreadyPresent(pw, this.header, pending);
            }
            pw.print("\r\n");
            pw.flush();
            sendBodyWithCorrectTransferAndEncoding(outputStream, pending);
            outputStream.flush();
            Utils.safeClose(this.data);
        } catch (IOException ioe) {
            NanoHTTPD.LOG.log(Level.SEVERE, "Could not send response to the client", ioe);
        }
    }

    private void sendBodyWithCorrectTransferAndEncoding(OutputStream outputStream, long pending) throws IOException {
        if (this.requestMethod != Method.HEAD && this.chunkedTransfer) {
            ChunkedOutputStream chunkedOutputStream = new ChunkedOutputStream(outputStream);
            sendBodyWithCorrectEncoding(chunkedOutputStream, -1);
            chunkedOutputStream.finish();
        } else {
            sendBodyWithCorrectEncoding(outputStream, pending);
        }
    }

    private void sendBodyWithCorrectEncoding(OutputStream outputStream, long pending) throws IOException {
        if (encodeAsGzip) {
            GZIPOutputStream gzipOutputStream = new GZIPOutputStream(outputStream);
            sendBody(gzipOutputStream, -1);
            gzipOutputStream.finish();
        } else {
            sendBody(outputStream, pending);
        }
    }

    private void sendBody(OutputStream outputStream, long pending) throws IOException {
        long BUFFER_SIZE = 16 * 1024;
        byte[] buff = new byte[(int) BUFFER_SIZE];
        boolean sendEverything = pending == -1;
        while (pending > 0 || sendEverything) {
            long bytesToRead = sendEverything ? BUFFER_SIZE : Math.min(pending, BUFFER_SIZE);
            int read = this.data.read(buff, 0, (int) bytesToRead);
            if (read <= 0) {
                break;
            }
            outputStream.write(buff, 0, read);
            if (!sendEverything) {
                pending -= read;
            }
        }
    }

    protected static long sendContentLengthHeaderIfNotAlreadyPresent(PrintWriter pw, Map<String, String> header, long size) {
        for (String headerName : header.keySet()) {
            if (headerName.equalsIgnoreCase("content-length")) {
                try {
                    return Long.parseLong(header.get(headerName));
                } catch (NumberFormatException ex) {
                    return size;
                }
            }
        }

        pw.print("Content-Length: " + size + "\r\n");
        return size;
    }

    public void setChunkedTransfer(boolean chunkedTransfer) {
        this.chunkedTransfer = chunkedTransfer;
    }

    public void setData(InputStream data) {
        this.data = data;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public void setRequestMethod(Method requestMethod) {
        this.requestMethod = requestMethod;
    }

    public void setStatus(IStatus status) {
        this.status = status;
    }


    public static Response newChunkedResponse(IStatus status, String mimeType, InputStream data) {
        return new Response(status, mimeType, data, -1);
    }

    public static Response newFixedLengthResponse(IStatus status, String mimeType, InputStream data, long totalBytes) {
        return new Response(status, mimeType, data, totalBytes);
    }

    public static Response newFixedLengthResponse(IStatus status, String mimeType, String txt) {
        if (txt == null) {
            return newFixedLengthResponse(status, mimeType, new ByteArrayInputStream(new byte[0]), 0);
        } else {
            byte[] bytes;
            try {
                bytes = txt.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                NanoHTTPD.LOG.log(Level.SEVERE, "encoding problem, responding nothing", e);
                bytes = new byte[0];
            }
            return newFixedLengthResponse(status, mimeType, new ByteArrayInputStream(bytes), bytes.length);
        }
    }

    public static Response newFixedLengthResponse(String msg) {
        return newFixedLengthResponse(Status.OK, NanoHTTPD.MIME_HTML, msg);
    }
}