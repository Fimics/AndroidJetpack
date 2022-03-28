package com.mic.castserver.response;


import static com.mic.castserver.StatusCode.HTTP_INTERNAL_ERROR;
import com.mic.castserver.ServerException;
import com.mic.castserver.ServerUtils;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Properties;



public class HttpResponse extends AbstractHttpResponse {

    private String status;

    private String mimeType;

    private InputStream data;

    private Properties requestHeaders;

    private String requestUrl;

    public HttpResponse(Socket socket, Properties headers, String url) throws IOException {
        super(socket);
        this.requestHeaders = headers;
        this.requestUrl = url;
    }

    HttpResponse(String status, String mimeType, InputStream data) {
        this.status = status;
        this.mimeType = mimeType;
        this.data = data;
    }

    HttpResponse(String status, String mimeType, String txt) {
        this.status = status;
        this.mimeType = mimeType;
        try {
            this.data = new ByteArrayInputStream(txt.getBytes("UTF-8"));
        } catch (java.io.UnsupportedEncodingException uee) {
            uee.printStackTrace();
        }
    }

    @Override
    public void process() throws IOException, ServerException {

        HttpResponse r = serve(requestUrl, requestHeaders, new File(ServerUtils.getRootDir()));
        if (r == null) {
            sendError(HTTP_INTERNAL_ERROR, "SERVER ERROR");
        } else {
            sendResponse(r.status, r.mimeType, r.header, r.data);
        }

        is.close();
    }

    @Override
    public Properties requestHeaders() {
        return requestHeaders;
    }

    @Override
    public String requestUrl() {
        return requestUrl;
    }
}