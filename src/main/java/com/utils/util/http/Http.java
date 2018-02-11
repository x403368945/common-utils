package com.utils.util.http;

import com.alibaba.fastjson.JSON;
import com.utils.util.Charsets;
import com.utils.util.FWrite;
import com.utils.util.Maps;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.cglib.beans.BeanMap;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static okhttp3.internal.Util.EMPTY_REQUEST;
import static okhttp3.internal.Util.EMPTY_RESPONSE;

/**
 * http请求封装
 * @author Jason Xie on 2017/11/21.
 */
@Slf4j
@Accessors(chain = true, fluent = true)
public class Http {
    private Http(final OkHttpClient client) {
        this.client = client;
    }
    public static Http of() {
        return new Http(HttpClient.getInstance().getHttpClient());
    }
    public static Http of(@NonNull final OkHttpClient client) {
        return new Http(client);
    }

    private OkHttpClient client;
    @Setter
    private HttpClient.ContentType type = HttpClient.ContentType.JSON;
    private String url;
    private Map<String, Object> params;
    /**
     * 构建header
     */
    @Setter
    private Function<Request.Builder, Request.Builder> headers = (request) -> request;

    public Http url(String url, Object... args) {
        if (Objects.nonNull(args)) {
            for (Object value : args) // 替换 url 参数占位符
                url = url.replaceFirst("\\{([a-zA-Z0-9]+)?\\}", value.toString());
        }
        this.url = url;
        return this;
    }
    public Http url(String url, Map<String, Object> args) {
        if (Objects.nonNull(args)) {
            for (Map.Entry<String, Object> entry : args.entrySet())
                url = url.replace(String.format("{%s}", entry.getKey()), entry.getValue().toString());
        }
        this.url = url;
        return this;
    }

    public Http params(Map<String, Object> params){
        this.params = params;
        return this;
    }
    public Http params(Object params){
        this.params = BeanMap.create(params);
        return this;
    }

    private RequestBody buildBody() {
        if (Objects.isNull(params)) return EMPTY_REQUEST;
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
                throw new IllegalArgumentException("未处理 content-type:" + type.comment);
        }
        return RequestBody.create(type.mediaType, content);
    }

    public Optional<ResponseBody> get() {
        HttpUrl URL = HttpUrl.parse(url);
        if (Objects.nonNull(params) && params.size() > 0) {
            {
                String enParams = params.entrySet().stream()
                        .map(entry -> {
                            try {
                                return Objects.isNull(entry.getValue()) ? null : entry.getKey() + "=" + URLEncoder.encode(entry.getValue().toString(), Charsets.UTF_8.name());
                            } catch (UnsupportedEncodingException e) {
                                log.error(e.getMessage(), e);
                                return null;
                            }
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.joining("&"));
                URL = HttpUrl.parse(url + "?" + enParams);
            }
        }
        log.debug("\nmethod:get \nurl:" + URL.toString() + "\nparams:" + JSON.toJSONString(params));
        return HttpClient.getInstance().send(client,
                headers.apply(new Request.Builder().url(URL).get())
                        .build()
        );
    }

    public Optional<ResponseBody> post() {
        log.debug("\nmethod:post \nurl:" + url + "\nparams:" + JSON.toJSONString(params));
        return HttpClient.getInstance().send(client,
                headers.apply(new Request.Builder().url(url).post(buildBody()))
                        .build()
        );
    }

    public Optional<ResponseBody> put() {
        log.debug("\nmethod:put \nurl:" + url + "\nparams:" + JSON.toJSONString(params));
        return HttpClient.getInstance().send(client,
                headers.apply(new Request.Builder().url(url).put(buildBody()))
                        .build()
        );
    }

    public Optional<ResponseBody> patch() {
        log.debug("\nmethod:patch \nurl:" + url + "\nparams:" + JSON.toJSONString(params));
        return HttpClient.getInstance().send(client,
                headers.apply(new Request.Builder().url(url).patch(buildBody()))
                        .build()
        );
    }

    public Optional<ResponseBody> delete() {
        log.debug("\nmethod:delete \nurl:" + url + "\nparams:" + JSON.toJSONString(params));
        return HttpClient.getInstance().send(client,
                headers.apply(new Request.Builder().url(url).delete(buildBody()))
                        .build()
        );
    }

    public Optional<ResponseBody> download() {
//        okhttp3.Request request;
//        if (Util.isNotEmpty(params)) {
//            okhttp3.RequestBody body = okhttp3.RequestBody.create(CONTENT_TYPE_FORM, params);
//            request = new okhttp3.Request.Builder().url(url).post(body).build();
//        } else {
//            request = new okhttp3.Request.Builder().url(url).get().build();
//        }
//        try {
//            okhttp3.Response response = httpClient.newCall(request).execute();
//
//            if (!response.isSuccessful()) throw new RuntimeException("Unexpected code " + response);
//
//            return response.body().byteStream();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }

        return Optional.empty();
    }

    public Optional<ResponseBody> upload(@NonNull final File file) {
//        okhttp3.RequestBody fileBody = okhttp3.RequestBody
//                .create(okhttp3.MediaType.parse("application/octet-stream"), file);
//
//        okhttp3.MultipartBody.Builder builder = new okhttp3.MultipartBody.Builder()
//                .setType(okhttp3.MultipartBody.FORM)
//                .addFormDataPart("media", file.getName(), fileBody);
//
//        if (Util.isNotEmpty(params)) {
//            builder.addFormDataPart("description", params);
//        }
//
//        okhttp3.RequestBody requestBody = builder.build();
//        okhttp3.Request request = new okhttp3.Request.Builder()
//                .url(url)
//                .post(requestBody)
//                .build();
        return Optional.empty();
    }

    public static void main(String[] args) {
        try {
            FWrite.of("logs", Http.class.getSimpleName(), "get.json")
                    .write(
                            Http.of()
                                    .url("http://localhost/anavss/api/test/{id}", UUID.randomUUID().toString())
                                    .get()
                                    .orElse(EMPTY_RESPONSE)
                                    .string()
                    );
            FWrite.of("logs", Http.class.getSimpleName(), "search.json")
                    .write(
                            Http.of()
                                    .url("http://localhost/anavss/api/test")
                                    .params(
                                            Maps.ofSO()
                                                    .put("name", "Jason")
                                                    .put("phone", "18717942600")
                                                    .buildRoundParams()
                                    )
                                    .get()
                                    .orElse(EMPTY_RESPONSE)
                                    .string()
                    );
            FWrite.of("logs", Http.class.getSimpleName(), "page.json")
                    .write(
                            Http.of()
                                    .url("http://localhost/anavss/api/test/{pageIndex}/{pageSize}", 1, 20)
                                    .params(
                                            Maps.ofSO()
                                                    .put("name", "Jason")
                                                    .put("phone", "18717942600")
                                                    .buildRoundParams()
                                    )
                                    .get()
                                    .orElse(EMPTY_RESPONSE)
                                    .string()
                    );
            FWrite.of("logs", Http.class.getSimpleName(), "post.json")
                    .write(
                            Http.of()
                                    .url("http://localhost/anavss/api/test", 1, 20)
                                    .params(
                                            Maps.ofSO()
                                                    .put("name", "Jason")
                                                    .put("phone", "18717942600")
                                                    .buildRoundParams()
                                    )
                                    .post()
                                    .orElse(EMPTY_RESPONSE)
                                    .string()
                    );
            FWrite.of("logs", Http.class.getSimpleName(), "putjson")
                    .write(
                            Http.of()
                                    .url("http://localhost/anavss/api/test/{id}", UUID.randomUUID().toString())
                                    .params(
                                            Maps.ofSO()
                                                    .put("name", "Jason")
                                                    .put("phone", "18717942600")
                                                    .buildRoundParams()
                                    )
                                    .put()
                                    .orElse(EMPTY_RESPONSE)
                                    .string()
                    );
            FWrite.of("logs", Http.class.getSimpleName(), "patch.json")
                    .write(
                            Http.of()
                                    .url("http://localhost/anavss/api/test/{id}", UUID.randomUUID().toString())
                                    .params(
                                            Maps.ofSO()
                                                    .put("name", "Jason")
                                                    .put("phone", "18717942600")
                                                    .buildRoundParams()
                                    )
                                    .patch()
                                    .orElse(EMPTY_RESPONSE)
                                    .string()
                    );
            FWrite.of("logs", Http.class.getSimpleName(), "delete.json")
                    .write(
                            Http.of()
                                    .url("http://localhost/anavss/api/test/{id}", UUID.randomUUID().toString())
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
