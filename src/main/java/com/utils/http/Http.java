package com.utils.http;

import com.alibaba.fastjson.JSON;
import com.utils.util.FWrite;
import com.utils.util.Maps;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.utils.enums.Charsets.UTF_8;
import static okhttp3.internal.Util.EMPTY_REQUEST;
import static okhttp3.internal.Util.EMPTY_RESPONSE;

/**
 * http请求封装
 *
 * @author 谢长春 on 2017/11/21 .
 */
@Slf4j
@Accessors(chain = true, fluent = true)
public final class Http {
    private Http(final OkHttpClient client) {
        this.client = client;
    }

    /**
     * 使用默认的 HttpClient 构造 Http 请求
     *
     * @return {@link Http}
     */
    public static Http of() {
        return new Http(HttpClient.getInstance().getHttpClient());
    }

    /**
     * 使用指定的 HttpClient 构造 Http 请求
     *
     * @param client {@link OkHttpClient}
     * @return {@link Http}
     */
    public static Http of(final OkHttpClient client) {
        Objects.requireNonNull(client, "参数【client】是必须的");
        return new Http(client);
    }

    /**
     * 请求客户端
     */
    private OkHttpClient client;
    /**
     * 请求参数类型
     */
    @Setter
    private HttpClient.ContentType type = HttpClient.ContentType.JSON;
    /**
     * 请求url
     */
    private String url;
    /**
     * 请求参数
     */
    private Map<String, Object> params;
    /**
     * 构建header
     */
    @Setter
    private Function<Request.Builder, Request.Builder> headers = (request) -> request;

    /**
     * 请求url构建<br>
     * 示例：<br>
     * url("abc.com/api/{id}/{uuid}", 100, UUID.randomUUID().toString())
     *
     * @param url  {@link String} 可以使用{}占位，将会使用 args 填充占位
     * @param args {@link Object} 替换占位符的参数集合
     * @return {@link Http}
     */
    public Http url(String url, Object... args) {
        if (Objects.nonNull(args)) {
            for (Object value : args) // 替换 url 参数占位符
            {
                url = url.replaceFirst("\\{(\\w+)?}", value.toString());
            }
        }
        this.url = url;
        return this;
    }

    /**
     * 请求url构建<br>
     * 示例：<br>
     * url("abc.com/api/{id}/{uuid}", new HashMap<String, Object>(){{put("id", 100);put("uuid", UUID.randomUUID().toString());}})
     *
     * @param url  {@link String} 可以使用{}占位，将会使用 args 填充占位
     * @param args {@link Object} 替换占位符的参数集合
     * @return {@link Http}
     */
    public Http url(String url, Map<String, Object> args) {
        if (Objects.nonNull(args)) {
            for (Map.Entry<String, Object> entry : args.entrySet()) {
                url = url.replace(String.format("{%s}", entry.getKey()), entry.getValue().toString());
            }
        }
        this.url = url;
        return this;
    }

    /**
     * 可以使用 org.apache.commons.beanutils.BeanMap(Object obj); 转换为 Map
     *
     * @param params {@link Map}{@link Map<String, Object> } 请求参数集合
     * @return {@link Http}
     */
    public Http params(final Map<String, Object> params) {
        this.params = params;
        return this;
    }

    /**
     * http请求body构造器
     *
     * @return {@link RequestBody}
     */
    private RequestBody buildBody() {
        if (Objects.isNull(params)) {
            return EMPTY_REQUEST;
        }
        String content;
        switch (type) {
            case JSON:
                content = JSON.toJSONString(params);
                break;
            case FORM_URLENCODED:
                content = params.entrySet().stream()
                        .map(entry -> Objects.isNull(entry.getValue()) ? null : entry.getKey() + "=" + entry.getValue().toString())
                        .filter(Objects::nonNull)
                        .collect(Collectors.joining("&"));
                break;
            default:
                throw new IllegalArgumentException("未处理 content-type:".concat(type.comment));
        }
        return RequestBody.create(type.mediaType, content);
    }

    /**
     * 发送 GET 请求
     *
     * @return {@link Optional}{@link Optional<ResponseBody>} 响应对象
     */
    public Optional<ResponseBody> get() {
        HttpUrl URL = HttpUrl.parse(url);
        if (Objects.nonNull(params) && params.size() > 0) {
            {
                String enParams = params.entrySet().stream()
                        .map(entry -> Objects.isNull(entry.getValue())
                                ? null
                                : entry.getKey().concat("=").concat(UTF_8.encode(entry.getValue().toString()))
                        )
                        .filter(Objects::nonNull)
                        .collect(Collectors.joining("&"));
                URL = HttpUrl.parse(url.concat("?").concat(enParams));
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("\nmethod:get \nurl:{} \nparams:{}", URL.toString(), JSON.toJSONString(params));
        }
        return HttpClient.getInstance().send(client,
                headers.apply(new Request.Builder().url(URL).get())
                        .build()
        );
    }

    /**
     * 发送 POST 请求
     *
     * @return {@link Optional}{@link Optional<ResponseBody>} 响应对象
     */
    public Optional<ResponseBody> post() {
        if (log.isDebugEnabled()) {
            log.debug("\nmethod:post \nurl:{} \nparams:{}", url, JSON.toJSONString(params));
        }
        return HttpClient.getInstance().send(client,
                headers.apply(new Request.Builder().url(url).post(buildBody()))
                        .build()
        );
    }

    /**
     * 发送 PUT 请求
     *
     * @return {@link Optional}{@link Optional<ResponseBody>} 响应对象
     */
    public Optional<ResponseBody> put() {
        if (log.isDebugEnabled()) {
            log.debug("\nmethod:put \nurl:{} \nparams:{}", url, JSON.toJSONString(params));
        }
        return HttpClient.getInstance().send(client,
                headers.apply(new Request.Builder().url(url).put(buildBody()))
                        .build()
        );
    }

    /**
     * 发送 PATCH 请求
     *
     * @return {@link Optional}{@link Optional<ResponseBody>} 响应对象
     */
    public Optional<ResponseBody> patch() {
        if (log.isDebugEnabled()) {
            log.debug("\nmethod:patch \nurl:{} \nparams:{}", url, JSON.toJSONString(params));
        }
        return HttpClient.getInstance().send(client,
                headers.apply(new Request.Builder().url(url).patch(buildBody()))
                        .build()
        );
    }

    /**
     * 发送 DELETE 请求
     *
     * @return {@link Optional}{@link Optional<ResponseBody>} 响应对象
     */
    public Optional<ResponseBody> delete() {
        if (log.isDebugEnabled()) {
            log.debug("\nmethod:delete \nurl:{} \nparams:{}", url, JSON.toJSONString(params));
        }
        return HttpClient.getInstance().send(client,
                headers.apply(new Request.Builder().url(url).delete(buildBody()))
                        .build()
        );
    }

    /**
     * 下载文件
     *
     * @return {@link Optional}{@link Optional<ResponseBody>} 响应对象
     * @deprecated 暂未实现
     */
    @Deprecated
    public Optional<ResponseBody> download() {
//        okhttp3.Request request = new okhttp3.Request.Builder().url(url).get().build();
//        try {
//            okhttp3.Response response = httpClient.newCall(request).execute();
//
//            if (!response.isSuccessful()) {
//                throw new RuntimeException("Unexpected code " + response);
//            }
//
//            okhttp3.ResponseBody body = response.body();
//            okhttp3.MediaType mediaType = body.contentType();
//            MediaFile mediaFile = new MediaFile();
//            if (Objects.equals(mediaType.type(), "text")) {
//                mediaFile.setError(body.string());
//            } else {
//                BufferedInputStream bis = new BufferedInputStream(body.byteStream());
//
//                String ds = response.header("Content-disposition");
//                String fullName = ds.substring(ds.indexOf("filename=\"") + 10, ds.length() - 1);
//                String relName = fullName.substring(0, fullName.lastIndexOf("."));
//                String suffix = fullName.substring(relName.length()+1);
//
//                mediaFile.setFullName(fullName);
//                mediaFile.setFileName(relName);
//                mediaFile.setSuffix(suffix);
//                mediaFile.setContentLength(body.contentLength() + "");
//                mediaFile.setContentType(body.contentType().toString());
//                mediaFile.setFileStream(bis);
//            }
//            return mediaFile;
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }

        return Optional.empty();
    }

    /**
     * 上传文件
     *
     * @return {@link Optional}{@link Optional<ResponseBody>} 响应对象
     * @deprecated 暂未实现
     */
    @Deprecated
    public Optional<ResponseBody> upload(final File file) {
        Objects.requireNonNull(file, "参数【file】是必须的");
//        okhttp3.RequestBody fileBody = okhttp3.RequestBody
//                .create(okhttp3.MediaType.parse("application/octet-stream"), file);
//
//        okhttp3.MultipartBody.Builder builder = new okhttp3.MultipartBody.Builder()
//                .setType(okhttp3.MultipartBody.FORM)
//                .addFormDataPart("media", file.getName(), fileBody);
//
//        if (StrKit.notBlank(params)) {
//            builder.addFormDataPart("description", params);
//        }
//
//        okhttp3.RequestBody requestBody = builder.build();
//        okhttp3.Request request = new okhttp3.Request.Builder()
//                .url(url)
//                .post(requestBody)
//                .build();
//
//        return exec(request);
        return Optional.empty();
    }

    public static void main(String[] args) {
        try {
            FWrite.of("logs", Http.class.getSimpleName(), "get.json")
                    .write(
                            Http.of()
                                    .url("http://localhost/api/test/{id}", UUID.randomUUID().toString())
                                    .get()
                                    .orElse(EMPTY_RESPONSE)
                                    .string()
                    );
            FWrite.of("logs", Http.class.getSimpleName(), "search.json")
                    .write(
                            Http.of()
                                    .url("http://localhost/api/test")
                                    .params(
                                            Maps.ofSO()
                                                    .put("name", "JX")
                                                    .put("phone", "18700000000")
                                                    .jsonKey()
                                    )
                                    .get()
                                    .orElse(EMPTY_RESPONSE)
                                    .string()
                    );
            FWrite.of("logs", Http.class.getSimpleName(), "page.json")
                    .write(
                            Http.of()
                                    .url("http://localhost/api/test/{pageIndex}/{pageSize}", 1, 20)
                                    .params(
                                            Maps.ofSO()
                                                    .put("name", "JX")
                                                    .put("phone", "18700000000")
                                                    .jsonKey()
                                    )
                                    .get()
                                    .orElse(EMPTY_RESPONSE)
                                    .string()
                    );
            FWrite.of("logs", Http.class.getSimpleName(), "post.json")
                    .write(
                            Http.of()
                                    .url("http://localhost/api/test", 1, 20)
                                    .params(
                                            Maps.ofSO()
                                                    .put("name", "JX")
                                                    .put("phone", "18700000000")
                                                    .jsonKey()
                                    )
                                    .post()
                                    .orElse(EMPTY_RESPONSE)
                                    .string()
                    );
            FWrite.of("logs", Http.class.getSimpleName(), "putjson")
                    .write(
                            Http.of()
                                    .url("http://localhost/api/test/{id}", UUID.randomUUID().toString())
                                    .params(
                                            Maps.ofSO()
                                                    .put("name", "JX")
                                                    .put("phone", "18700000000")
                                                    .jsonKey()
                                    )
                                    .put()
                                    .orElse(EMPTY_RESPONSE)
                                    .string()
                    );
            FWrite.of("logs", Http.class.getSimpleName(), "patch.json")
                    .write(
                            Http.of()
                                    .url("http://localhost/api/test/{id}", UUID.randomUUID().toString())
                                    .params(
                                            Maps.ofSO()
                                                    .put("name", "JX")
                                                    .put("phone", "18700000000")
                                                    .jsonKey()
                                    )
                                    .patch()
                                    .orElse(EMPTY_RESPONSE)
                                    .string()
                    );
            FWrite.of("logs", Http.class.getSimpleName(), "delete.json")
                    .write(
                            Http.of()
                                    .url("http://localhost/api/test/{id}", UUID.randomUUID().toString())
                                    .delete()
                                    .orElse(EMPTY_RESPONSE)
                                    .string()
                    );
            FWrite.of("logs", Http.class.getSimpleName(), "baidu.html")
                    .write(
                            Http.of()
                                    .url("https://www.baidu.com/")
                                    .get()
                                    .orElse(EMPTY_RESPONSE)
                                    .string()
                    );
            FWrite.of("logs", Http.class.getSimpleName(), "12306.html")
                    .write(
                            Http.of(HttpClient.getInstance().getSSLClient())
                                    .url("https://kyfw.12306.cn/otn/login/init")
                                    .get()
                                    .orElse(EMPTY_RESPONSE)
                                    .string()
                    );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
