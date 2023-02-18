package com.yingding.lib_net.easy;

import javax.net.ssl.*;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class SSLHelper
{
    //获取这个SSLSocketFactory
    //通过这个类我们可以获得SSLSocketFactory，这个东西就是用来管理证书和信任证书的
    public static SSLSocketFactory getSSLSocketFactory()
    {
        try
        {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new X509TrustManager[]{getTrustManager()}, new SecureRandom());
            return sslContext.getSocketFactory();
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private static X509TrustManager trustManager;

    //获取TrustManager
    public static X509TrustManager getTrustManager()
    {
        if(trustManager == null){
            trustManager = new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            };
        }
        return  trustManager;
    }

    private static HostnameVerifier verifier;
    //获取HostnameVerifier
    public static HostnameVerifier getHostnameVerifier()
    {
        if(verifier == null){
            verifier = new HostnameVerifier()
            {
                @Override
                public boolean verify(String s, SSLSession sslSession)
                {
                    //未真正校检服务器端证书域名
                    return true;
                }
            };
        }
        return verifier;
    }
}