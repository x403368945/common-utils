package com.utils.util.http;

import com.utils.exception.HttpException;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * http 客户端定义
 *
 * @author Jason Xie on 2017/11/21.
 */
@Slf4j
public class HttpClient {
    public enum ContentType {
        JSON("application/json; charset=utf-8"),
        FORM_URLENCODED("application/x-www-form-urlencoded; charset=utf-8"),
        ;
        /**
         * 枚举属性说明
         */
        final String comment;
        final MediaType mediaType;

        ContentType(final String comment) {
            this.comment = comment;
            this.mediaType = MediaType.parse(comment);
        }
    }

    private final OkHttpClient httpClient;
    private final OkHttpClient sslClient;
    private static volatile HttpClient instance;

    @SneakyThrows
    private HttpClient() {
        { // http + https 忽略验证
            httpClient = new OkHttpClient()
                    .newBuilder()
                    .connectTimeout(5, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .build();
        }
        if(false){ // https 证书配置
            X509TrustManager x509TrustManager = null;
            { // 加载证书
                @Cleanup InputStream inputStream = HttpClient.class.getResourceAsStream("cers/srca.cer"); // 将 12306 下载的证书放到  resources/cers 目录中
                CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
                KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType()); // KeyStore.getInstance("PKCS12"|"BKS")
                keyStore.load(null);
                keyStore.setCertificateEntry(UUID.randomUUID().toString(), certificateFactory.generateCertificate(inputStream));
                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init(keyStore);
                for (TrustManager manager : trustManagerFactory.getTrustManagers()) {
                    if (manager instanceof X509TrustManager) x509TrustManager = (X509TrustManager) manager;
                }
            }
            KeyManager[] keyManagers = null;
            { // 加载密码证书
//                @Cleanup InputStream inputStream = HttpClient.class.getResourceAsStream("/cers/srca.cer"); // 将证书放到  resources/cers 目录中
//                KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType()); // KeyStore.getInstance("PKCS12"|"BKS")
//                keyStore.load(inputStream, "111111".toCharArray());
//                KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
//                keyManagerFactory.init(keyStore, "111111".toCharArray());
//                keyManagers = keyManagerFactory.getKeyManagers();
            }
            final SSLContext sslContext = SSLContext.getInstance("TLS"); // SSLContext.getInstance("TLS"|"TLSv1")
            sslContext.init(keyManagers, new TrustManager[]{x509TrustManager}, new SecureRandom());
            sslClient = new OkHttpClient()
                    .newBuilder()
                    .connectTimeout(5, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .hostnameVerifier((hostname, sslSession) -> { // 校验服务端证书
                        log.debug("hostname:" + hostname);
//                        try {
//                            //获取证书链中的所有证书
//                            Certificate[] localCertificates = sslSession.getPeerCertificates();
//                            // 打印所有证书内容
//                            for (Certificate c : localCertificates) {
//                                log.debug("verify: " + c.toString());
//                            }
////                        // 将证书链中的第一个写入文件
////                        FWrite.build()
////                                .to("", "cers", "ca.cer")
////                                .write(localCertificates[0].getEncoded())
////                        ;
//                        } catch (Exception e) {
//                            log.error(e.getMessage(), e);
//                            return false;
//                        }
                        return true;
                    })
                    .sslSocketFactory(sslContext.getSocketFactory(), x509TrustManager)
                    .build();
        }
        else sslClient = null;
    }

    public static HttpClient getInstance() {
        if (null == instance) {
            synchronized (HttpClient.class) {
                if (null == instance) {
                    instance = new HttpClient();
                }
            }
        }
        return instance;
    }

    public OkHttpClient getHttpClient() {
        return httpClient;
    }

    public OkHttpClient getSSLClient() {
        return sslClient;
    }

    /**
     * 发送 http|https 请求
     * @param client OkHttpClient
     * @param request Request
     * @return Optional<ResponseBody>
     */
    public Optional<ResponseBody> send(final OkHttpClient client, final Request request) {
        try {
            final Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                return Optional.ofNullable(response.body());
            } else {
                throw new HttpException("请求失败，响应码：" + response.code());
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new HttpException("请求异常", e);
        }
    }

}
