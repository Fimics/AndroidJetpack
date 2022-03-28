package com.mic.castserver.request;

import android.text.TextUtils;


import com.mic.castserver.NetUtils;
import com.mic.castserver.ServerException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Properties;
import java.util.StringTokenizer;


public class HttpRequest implements IRequest {

    private  InputStream is;
    private  Socket socket;

    private  InetAddress address;
    private int port;
    private String method;
    private String url;
    private Properties headers;


    private static ServerException HTTP_BAD_REQUEST_EXCEPTION = new ServerException("BAD REQUEST");


    public HttpRequest(Socket socket) throws IOException, ServerException {
        if(socket!=null&&socket.isConnected()) {
            this.socket = socket;
            this.address = this.socket.getInetAddress();
            this.is = this.socket.getInputStream();
            this.port = this.socket.getPort();
            Inet4Address cast = NetUtils.CAST_ADDRESS;

            if (cast != null && address != null && !cast.equals(address)) {
                throw HTTP_BAD_REQUEST_EXCEPTION;
            }
            process();
        }
    }

    @Override
    public void process() throws IOException, ServerException {

        // Read the first 8192 bytes.
        // The full header should fit in here.
        // Apache's default header limit is 8KB.
        // Do NOT assume that a single read will get the entire header at once!
        final int bufSize = 8192;
        byte[] buf = new byte[bufSize];
        int splitByte;
        int rLen = 0;
        {
            int read = is.read(buf, 0, bufSize);
            while (read > 0) {
                rLen += read;
                splitByte = findHeaderEnd(buf, rLen);
                if (splitByte > 0)
                    break;
                read = is.read(buf, rLen, bufSize - rLen);
            }
        }

        Properties pre = new Properties();
        Properties header = new Properties();
        // Create a BufferedReader for parsing the header.
        ByteArrayInputStream hbis = new ByteArrayInputStream(buf, 0, rLen);
        BufferedReader hin = new BufferedReader(new InputStreamReader(hbis));

        // Decode the header into parms and header java properties
        decodeHeader(hin, pre, header);

        method = pre.getProperty("method");
        url = pre.getProperty("uri");
        headers = header;
        if (TextUtils.isEmpty(method) ||!method.equalsIgnoreCase("GET")) {
            throw HTTP_BAD_REQUEST_EXCEPTION;
        }
    }


    private int findHeaderEnd(final byte[] buf, int rlen) {
        int splitbyte = 0;
        while (splitbyte + 3 < rlen) {
            if (buf[splitbyte] == '\r' && buf[splitbyte + 1] == '\n' && buf[splitbyte + 2] == '\r' && buf[splitbyte + 3] == '\n')
                return splitbyte + 4;
            splitbyte++;
        }
        return 0;
    }


    private void decodeHeader(BufferedReader in, Properties pre, Properties header) throws ServerException, IOException {

        if(in==null || pre==null ||header==null) return;

        String inLine = in.readLine();
        if (inLine == null) return;

        StringTokenizer st = new StringTokenizer(inLine);
        if (!st.hasMoreTokens())
            throw HTTP_BAD_REQUEST_EXCEPTION;

        String method = st.nextToken();
        pre.put("method", method);

        if (!st.hasMoreTokens())
            throw HTTP_BAD_REQUEST_EXCEPTION;

        String uri = st.nextToken();
        // Decode parameters from the URI
        int qmi = uri.indexOf('?');
        if (qmi >= 0) {
            uri = decodePercent(uri.substring(0, qmi));
        } else uri = decodePercent(uri);

        // If there's another token, it's protocol version,
        // followed by HTTP headers. Ignore version but parse headers.
        // NOTE: this now forces header names lowercase since they are
        // case insensitive and vary by client.
        if (st.hasMoreTokens()) {
            String line = in.readLine();
            while (line != null && line.trim().length() > 0) {
                int p = line.indexOf(':');
                if (p >= 0)
                    header.put(line.substring(0, p).trim().toLowerCase(), line.substring(p + 1).trim());
                line = in.readLine();
            }
        }

        pre.put("uri", uri);
    }


    private String decodePercent(String str) throws UnsupportedEncodingException {
        if(TextUtils.isEmpty(str)) return "";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            switch (c) {
                case '+':
                    baos.write((int) ' ');
                    break;
                case '%':
                    baos.write(Integer.parseInt(str.substring(i + 1, i + 3), 16));
                    i += 2;
                    break;
                default:
                    baos.write((int) c);
                    break;
            }
        }
        return new String(baos.toByteArray(), "UTF-8");
    }

    @Override
    public int port() {
        return port;
    }

    @Override
    public InetAddress address() {
        return address;
    }

    @Override
    public String method() {
        return method;
    }

    @Override
    public String url() {
        return url;
    }

    @Override
    public Properties headers() {
        return headers;
    }

    @Override
    public void info() {
        toString();
    }

    @Override
    public String toString() {
        return "HttpRequest{" +
                "socket=" + socket +
                ", address=" + address +
                ", port=" + port +
                ", method='" + method + '\'' +
                ", url='" + url + '\'' +
                ", headers=" + headers +
                '}';
    }
}
