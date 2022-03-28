package com.mic.server.request;

import static com.mic.server.Constant.MIME_PLAINTEXT;
import static com.mic.server.Constant.QUERY_STRING_PARAMETER;
import static com.mic.server.PatternConst.BOUNDARY_PATTERN;
import static com.mic.server.PatternConst.CHARSET_PATTERN;
import static com.mic.server.PatternConst.CONTENT_DISPOSITION_ATTRIBUTE_PATTERN;
import static com.mic.server.PatternConst.CONTENT_DISPOSITION_PATTERN;
import static com.mic.server.PatternConst.CONTENT_TYPE_PATTERN;
import android.text.TextUtils;
import android.util.Log;
import com.mic.server.Method;
import com.mic.server.ServerUtils;
import com.mic.server.Utils;
import com.mic.server.response.Response;
import com.mic.server.response.ResponseException;
import com.mic.server.tempfile.TempFile;
import com.mic.server.tempfile.TempFileManager;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HTTPSession implements IHTTPSession {

    private static final String TAG = "session";

    private static final int REQUEST_BUFFER_LEN = 512;

    private static final int MEMORY_STORE_LIMIT = 1024;

    public static final int BUFSIZE = 8192;

    public static final int MAX_HEADER_SIZE = 1024;

    private final TempFileManager tempFileManager;

    private final OutputStream outputStream;

    private final BufferedInputStream inputStream;

    private int splitbyte;

    private int rlen;

    private String uri;

    private Method method;

    private Map<String, String> parms;

    private Map<String, String> headers;

    private CookieHandler cookies;

    private String queryParameterString;

    private String remoteIp;

    private String protocolVersion;

    public HTTPSession(TempFileManager tempFileManager, InputStream inputStream, OutputStream outputStream) {
        this.tempFileManager = tempFileManager;
        this.inputStream = new BufferedInputStream(inputStream, HTTPSession.BUFSIZE);
        this.outputStream = outputStream;
    }

    public HTTPSession(TempFileManager tempFileManager, InputStream inputStream, OutputStream outputStream, InetAddress inetAddress) {
        this.tempFileManager = tempFileManager;
        this.inputStream = new BufferedInputStream(inputStream, HTTPSession.BUFSIZE);
        this.outputStream = outputStream;
        this.remoteIp = inetAddress.isLoopbackAddress() || inetAddress.isAnyLocalAddress() ? "127.0.0.1" : inetAddress.getHostAddress().toString();
        this.headers = new HashMap<String, String>();
    }

    private void decodeHeader(BufferedReader in, Map<String, String> pre, Map<String, String> parms, Map<String, String> headers) throws ResponseException {
        try {
            // Read the request line
            String inLine = in.readLine();
            if (inLine == null) {
                return;
            }

            StringTokenizer st = new StringTokenizer(inLine);
            if (!st.hasMoreTokens()) {
                throw new ResponseException(Response.Status.BAD_REQUEST, "BAD REQUEST: Syntax error. Usage: GET /example/file.html");
            }

            pre.put("method", st.nextToken());

            if (!st.hasMoreTokens()) {
                throw new ResponseException(Response.Status.BAD_REQUEST, "BAD REQUEST: Missing URI. Usage: GET /example/file.html");
            }

            String uri = st.nextToken();

            // Decode parameters from the URI
            int qmi = uri.indexOf('?');
            if (qmi >= 0) {
                decodeParms(uri.substring(qmi + 1), parms);
                uri = Decoder.decodePercent(uri.substring(0, qmi));
            } else {
                uri = Decoder.decodePercent(uri);
            }

            // If there's another token, its protocol version,
            // followed by HTTP headers.
            // NOTE: this now forces header names lower case since they are
            // case insensitive and vary by client.
            if (st.hasMoreTokens()) {
                protocolVersion = st.nextToken();
            } else {
                protocolVersion = "HTTP/1.1";
                Log.d(TAG, "no protocol version specified, strange. Assuming HTTP/1.1.");
            }
            String line = in.readLine();
            while (line != null && line.trim().length() > 0) {
                int p = line.indexOf(':');
                if (p >= 0) {
                    headers.put(line.substring(0, p).trim().toLowerCase(Locale.US), line.substring(p + 1).trim());
                }
                line = in.readLine();
            }

            pre.put("uri", uri);
        } catch (IOException ioe) {
            throw new ResponseException(Response.Status.INTERNAL_ERROR, "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage(), ioe);
        }
    }

    private void decodeMultipartFormData(String boundary, String encoding, ByteBuffer fbuf, Map<String, String> parms, Map<String, String> files) throws ResponseException {
        try {
            int[] boundary_idxs = getBoundaryPositions(fbuf, boundary.getBytes());
            if (boundary_idxs.length < 2) {
                throw new ResponseException(Response.Status.BAD_REQUEST, "BAD REQUEST: Content type is multipart/form-data but contains less than two boundary strings.");
            }

            byte[] part_header_buff = new byte[MAX_HEADER_SIZE];
            for (int bi = 0; bi < boundary_idxs.length - 1; bi++) {
                fbuf.position(boundary_idxs[bi]);
                int len = (fbuf.remaining() < MAX_HEADER_SIZE) ? fbuf.remaining() : MAX_HEADER_SIZE;
                fbuf.get(part_header_buff, 0, len);
                BufferedReader in = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(part_header_buff, 0, len), Charset.forName(encoding)), len);

                int headerLines = 0;
                // First line is boundary string
                String mpline = in.readLine();
                headerLines++;
                if (!mpline.contains(boundary)) {
                    throw new ResponseException(Response.Status.BAD_REQUEST, "BAD REQUEST: Content type is multipart/form-data but chunk does not start with boundary.");
                }

                String part_name = null, file_name = null, content_type = null;
                // Parse the reset of the header lines
                mpline = in.readLine();
                headerLines++;
                while (mpline != null && mpline.trim().length() > 0) {
                    Matcher matcher = CONTENT_DISPOSITION_PATTERN.matcher(mpline);
                    if (matcher.matches()) {
                        String attributeString = matcher.group(2);
                        matcher = CONTENT_DISPOSITION_ATTRIBUTE_PATTERN.matcher(attributeString);
                        while (matcher.find()) {
                            String key = matcher.group(1);
                            if (key.equalsIgnoreCase("name")) {
                                part_name = matcher.group(2);
                            } else if (key.equalsIgnoreCase("filename")) {
                                file_name = matcher.group(2);
                            }
                        }
                    }
                    matcher = CONTENT_TYPE_PATTERN.matcher(mpline);
                    if (matcher.matches()) {
                        content_type = matcher.group(2).trim();
                    }
                    mpline = in.readLine();
                    headerLines++;
                }
                int part_header_len = 0;
                while (headerLines-- > 0) {
                    part_header_len = scipOverNewLine(part_header_buff, part_header_len);
                }
                // Read the part data
                if (part_header_len >= len - 4) {
                    throw new ResponseException(Response.Status.INTERNAL_ERROR, "Multipart header size exceeds MAX_HEADER_SIZE.");
                }
                int part_data_start = boundary_idxs[bi] + part_header_len;
                int part_data_end = boundary_idxs[bi + 1] - 4;

                fbuf.position(part_data_start);
                if (content_type == null) {
                    // Read the part into a string
                    byte[] data_bytes = new byte[part_data_end - part_data_start];
                    fbuf.get(data_bytes);
                    parms.put(part_name, new String(data_bytes, encoding));
                } else {
                    // Read it into a file
                    String path = saveTmpFile(fbuf, part_data_start, part_data_end - part_data_start, file_name);
                    if (!files.containsKey(part_name)) {
                        files.put(part_name, path);
                    } else {
                        int count = 2;
                        while (files.containsKey(part_name + count)) {
                            count++;
                        }
                        files.put(part_name + count, path);
                    }
                    parms.put(part_name, file_name);
                }
            }
        } catch (ResponseException re) {
            throw re;
        } catch (Exception e) {
            throw new ResponseException(Response.Status.INTERNAL_ERROR, e.toString());
        }
    }

    private int scipOverNewLine(byte[] part_header_buff, int index) {
        while (part_header_buff[index] != '\n') {
            index++;
        }
        return ++index;
    }

    private void decodeParms(String parms, Map<String, String> p) {
        if (parms == null) {
            this.queryParameterString = "";
            return;
        }

        this.queryParameterString = parms;
        StringTokenizer st = new StringTokenizer(parms, "&");
        while (st.hasMoreTokens()) {
            String e = st.nextToken();
            int sep = e.indexOf('=');
            if (sep >= 0) {
                p.put(Decoder.decodePercent(e.substring(0, sep)).trim(), Decoder.decodePercent(e.substring(sep + 1)));
            } else {
                p.put(Decoder.decodePercent(e).trim(), "");
            }
        }
    }

    @Override
    public void execute() throws IOException {
        Response r = null;
        try {
            // Read the first 8192 bytes.
            // The full header should fit in here.
            // Apache's default header limit is 8KB.
            // Do NOT assume that a single read will get the entire header
            // at once!
            byte[] buf = new byte[HTTPSession.BUFSIZE];
            this.splitbyte = 0;
            this.rlen = 0;

            int read = -1;
            this.inputStream.mark(HTTPSession.BUFSIZE);
            try {
                read = this.inputStream.read(buf, 0, HTTPSession.BUFSIZE);
            } catch (Exception e) {
                Utils.safeClose(this.inputStream);
                Utils.safeClose(this.outputStream);
                throw new SocketException("NanoHttpd Shutdown");
            }
            if (read == -1) {
                // socket was been closed
                Utils.safeClose(this.inputStream);
                Utils.safeClose(this.outputStream);
                throw new SocketException("NanoHttpd Shutdown");
            }
            while (read > 0) {
                this.rlen += read;
                this.splitbyte = findHeaderEnd(buf, this.rlen);
                if (this.splitbyte > 0) {
                    break;
                }
                read = this.inputStream.read(buf, this.rlen, HTTPSession.BUFSIZE - this.rlen);
            }

            if (this.splitbyte < this.rlen) {
                this.inputStream.reset();
                this.inputStream.skip(this.splitbyte);
            }

            this.parms = new HashMap<String, String>();
            if (null == this.headers) {
                this.headers = new HashMap<String, String>();
            } else {
                this.headers.clear();
            }

            // Create a BufferedReader for parsing the header.
            BufferedReader hin = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(buf, 0, this.rlen)));

            // Decode the header into parms and header java properties
            Map<String, String> pre = new HashMap<String, String>();
            decodeHeader(hin, pre, this.parms, this.headers);

            if (null != this.remoteIp) {
                this.headers.put("remote-addr", this.remoteIp);
                this.headers.put("http-client-ip", this.remoteIp);
            }

            this.method = Method.lookup(pre.get("method"));
            if (this.method == null) {
                throw new ResponseException(Response.Status.BAD_REQUEST, "BAD REQUEST: Syntax error.");
            }

            this.uri = pre.get("uri");

            this.cookies = new CookieHandler(this.headers);

            String connection = this.headers.get("connection");
            boolean keepAlive = protocolVersion.equals("HTTP/1.1") && (connection == null || !connection.matches("(?i).*close.*"));

            // Ok, now do the serve()

            // TODO: long body_size = getBodySize();
            // TODO: long pos_before_serve = this.inputStream.totalRead()
            // (requires implementaion for totalRead())
            r = serve(this);
            // TODO: this.inputStream.skip(body_size -
            // (this.inputStream.totalRead() - pos_before_serve))

            if (r == null) {
                throw new ResponseException(Response.Status.INTERNAL_ERROR, "SERVER INTERNAL ERROR: Serve() returned a null response.");
            } else {
                String acceptEncoding = this.headers.get("accept-encoding");
                this.cookies.unloadQueue(r);
                r.setRequestMethod(this.method);
                r.setGzipEncoding(Decoder.useGzipWhenAccepted(r) && acceptEncoding != null && acceptEncoding.contains("gzip"));
                r.setKeepAlive(keepAlive);
                r.send(this.outputStream);
            }
            if (!keepAlive || "close".equalsIgnoreCase(r.getHeader("connection"))) {
                throw new SocketException("NanoHttpd Shutdown");
            }
        } catch (SocketException e) {
            // throw it out to close socket object (finalAccept)
            throw e;
        } catch (SocketTimeoutException ste) {
            // treat socket timeouts the same way we treat socket exceptions
            // i.e. close the stream & finalAccept object by throwing the
            // exception up the call stack.
            throw ste;
        } catch (IOException ioe) {
            Response resp = Response.newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
            resp.send(this.outputStream);
            Utils.safeClose(this.outputStream);
        } catch (ResponseException re) {
            Response resp = Response.newFixedLengthResponse(re.getStatus(), MIME_PLAINTEXT, re.getMessage());
            resp.send(this.outputStream);
            Utils.safeClose(this.outputStream);
        } finally {
            Utils.safeClose(r);
            this.tempFileManager.clear();
        }
    }

    private int findHeaderEnd(final byte[] buf, int rlen) {
        int splitbyte = 0;
        while (splitbyte + 1 < rlen) {

            // RFC2616
            if (buf[splitbyte] == '\r' && buf[splitbyte + 1] == '\n' && splitbyte + 3 < rlen && buf[splitbyte + 2] == '\r' && buf[splitbyte + 3] == '\n') {
                return splitbyte + 4;
            }

            // tolerance
            if (buf[splitbyte] == '\n' && buf[splitbyte + 1] == '\n') {
                return splitbyte + 2;
            }
            splitbyte++;
        }
        return 0;
    }

    private int[] getBoundaryPositions(ByteBuffer b, byte[] boundary) {
        int[] res = new int[0];
        if (b.remaining() < boundary.length) {
            return res;
        }

        int search_window_pos = 0;
        byte[] search_window = new byte[4 * 1024 + boundary.length];

        int first_fill = (b.remaining() < search_window.length) ? b.remaining() : search_window.length;
        b.get(search_window, 0, first_fill);
        int new_bytes = first_fill - boundary.length;

        do {
            // Search the search_window
            for (int j = 0; j < new_bytes; j++) {
                for (int i = 0; i < boundary.length; i++) {
                    if (search_window[j + i] != boundary[i])
                        break;
                    if (i == boundary.length - 1) {
                        // Match found, add it to results
                        int[] new_res = new int[res.length + 1];
                        System.arraycopy(res, 0, new_res, 0, res.length);
                        new_res[res.length] = search_window_pos + j;
                        res = new_res;
                    }
                }
            }
            search_window_pos += new_bytes;

            // Copy the end of the buffer to the start
            System.arraycopy(search_window, search_window.length - boundary.length, search_window, 0, boundary.length);

            // Refill search_window
            new_bytes = search_window.length - boundary.length;
            new_bytes = (b.remaining() < new_bytes) ? b.remaining() : new_bytes;
            b.get(search_window, boundary.length, new_bytes);
        } while (new_bytes > 0);
        return res;
    }

    @Override
    public CookieHandler getCookies() {
        return this.cookies;
    }

    @Override
    public final Map<String, String> getHeaders() {
        return this.headers;
    }

    @Override
    public final InputStream getInputStream() {
        return this.inputStream;
    }

    @Override
    public final Method getMethod() {
        return this.method;
    }

    @Override
    public final Map<String, String> getParms() {
        return this.parms;
    }

    @Override
    public String getQueryParameterString() {
        return this.queryParameterString;
    }

    private RandomAccessFile getTmpBucket() {
        try {
            TempFile tempFile = this.tempFileManager.createTempFile(null);
            return new RandomAccessFile(tempFile.getName(), "rw");
        } catch (Exception e) {
            throw new Error(e); // we won't recover, so throw an error
        }
    }

    @Override
    public final String getUri() {
        return this.uri;
    }

    public long getBodySize() {
        if (this.headers.containsKey("content-length")) {
            return Long.parseLong(this.headers.get("content-length"));
        } else if (this.splitbyte < this.rlen) {
            return this.rlen - this.splitbyte;
        }
        return 0;
    }

    @Override
    public void parseBody(Map<String, String> files) throws IOException, ResponseException {
        RandomAccessFile randomAccessFile = null;
        try {
            long size = getBodySize();
            ByteArrayOutputStream baos = null;
            DataOutput request_data_output = null;

            // Store the request in memory or a file, depending on size
            if (size < MEMORY_STORE_LIMIT) {
                baos = new ByteArrayOutputStream();
                request_data_output = new DataOutputStream(baos);
            } else {
                randomAccessFile = getTmpBucket();
                request_data_output = randomAccessFile;
            }

            // Read all the body and write it to request_data_output
            byte[] buf = new byte[REQUEST_BUFFER_LEN];
            while (this.rlen >= 0 && size > 0) {
                this.rlen = this.inputStream.read(buf, 0, (int) Math.min(size, REQUEST_BUFFER_LEN));
                size -= this.rlen;
                if (this.rlen > 0) {
                    request_data_output.write(buf, 0, this.rlen);
                }
            }

            ByteBuffer fbuf = null;
            if (baos != null) {
                fbuf = ByteBuffer.wrap(baos.toByteArray(), 0, baos.size());
            } else {
                fbuf = randomAccessFile.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, randomAccessFile.length());
                randomAccessFile.seek(0);
            }

            // If the method is POST, there may be parameters
            // in data section, too, read it:
            if (Method.POST.equals(this.method)) {
                String contentType = "";
                String contentTypeHeader = this.headers.get("content-type");

                StringTokenizer st = null;
                if (contentTypeHeader != null) {
                    st = new StringTokenizer(contentTypeHeader, ",; ");
                    if (st.hasMoreTokens()) {
                        contentType = st.nextToken();
                    }
                }

                if ("multipart/form-data".equalsIgnoreCase(contentType)) {
                    // Handle multipart/form-data
                    if (!st.hasMoreTokens()) {
                        throw new ResponseException(Response.Status.BAD_REQUEST,
                                "BAD REQUEST: Content type is multipart/form-data but boundary missing. Usage: GET /example/file.html");
                    }
                    decodeMultipartFormData(getAttributeFromContentHeader(contentTypeHeader, BOUNDARY_PATTERN, null), //
                            getAttributeFromContentHeader(contentTypeHeader, CHARSET_PATTERN, "US-ASCII"), fbuf, this.parms, files);
                } else {
                    byte[] postBytes = new byte[fbuf.remaining()];
                    fbuf.get(postBytes);
                    String postLine = new String(postBytes).trim();
                    // Handle application/x-www-form-urlencoded
                    if ("application/x-www-form-urlencoded".equalsIgnoreCase(contentType)) {
                        decodeParms(postLine, this.parms);
                    } else if (postLine.length() != 0) {
                        // Special case for raw POST data => create a
                        // special files entry "postData" with raw content
                        // data
                        files.put("postData", postLine);
                    }
                }
            } else if (Method.PUT.equals(this.method)) {
                files.put("content", saveTmpFile(fbuf, 0, fbuf.limit(), null));
            }
        } finally {
            Utils.safeClose(randomAccessFile);
        }
    }

    private String getAttributeFromContentHeader(String contentTypeHeader, Pattern pattern, String defaultValue) {
        Matcher matcher = pattern.matcher(contentTypeHeader);
        return matcher.find() ? matcher.group(2) : defaultValue;
    }

    private String saveTmpFile(ByteBuffer b, int offset, int len, String filename_hint) {
        String path = "";
        if (len > 0) {
            FileOutputStream fileOutputStream = null;
            try {
                TempFile tempFile = this.tempFileManager.createTempFile(filename_hint);
                ByteBuffer src = b.duplicate();
                fileOutputStream = new FileOutputStream(tempFile.getName());
                FileChannel dest = fileOutputStream.getChannel();
                src.position(offset).limit(offset + len);
                dest.write(src.slice());
                path = tempFile.getName();
            } catch (Exception e) { // Catch exception if any
                throw new Error(e); // we won't recover, so throw an error
            } finally {
                Utils.safeClose(fileOutputStream);
            }
        }
        return path;
    }

    public Response serve(IHTTPSession session) {
        Map<String, String> files = new HashMap<String, String>();
        Method method = session.getMethod();
        if (Method.PUT.equals(method) || Method.POST.equals(method)) {
            try {
                session.parseBody(files);
            } catch (IOException ioe) {
                return Response.newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
            } catch (ResponseException re) {
                return Response.newFixedLengthResponse(re.getStatus(), MIME_PLAINTEXT, re.getMessage());
            }
        } else if (Method.GET.equals(method)) {
            String uri = session.getUri();
            if (TextUtils.isEmpty(uri)) {
                return Response.newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "Not Found");
            }

            String text = ServerUtils.Companion.readText(uri);
            if (TextUtils.isEmpty(text)) {
                return Response.newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "Not Found");
            }
            return Response.newFixedLengthResponse(Response.Status.OK, MIME_PLAINTEXT, text);
        }

        Map<String, String> parms = session.getParms();
        parms.put(QUERY_STRING_PARAMETER, session.getQueryParameterString());
        return Response.newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "Not Found");
    }
}