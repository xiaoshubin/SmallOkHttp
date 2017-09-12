package com.smallcake.okhttp.volley;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.HurlStack;
import com.facebook.stetho.Stetho;
import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.smallcake.okhttp.SmallOkHttp;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.Util;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * MyApplication --  com.smallcake.okhttp.callback
 * Created by Small Cake on  2017/9/11 15:36.
 */

public class OkHttpStack extends HurlStack {

    long MAX_CACHE_SIZE = 10*1024*1024;//max cache default 10M
    int CONNECT_TIME_OUT = 20*1000;//connect time out
    int READ_TIME_OUT = 20*1000;//read time out
    int WRITE_TIME_OUT = 20*1000;//write time out

    private static volatile SmallOkHttp mInstance;
    private static OkHttpClient okHttpClient;

    public OkHttpStack(Context context) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .cache(new Cache(context.getCacheDir(), MAX_CACHE_SIZE))
                .readTimeout(READ_TIME_OUT, TimeUnit.MILLISECONDS)
                .writeTimeout(WRITE_TIME_OUT, TimeUnit.MILLISECONDS)
                .connectTimeout(CONNECT_TIME_OUT, TimeUnit.MILLISECONDS);

        boolean debug = (Boolean) getBuildConfigValue(context, "DEBUG");
        if (debug) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(interceptor);
            /**
             * FaceBook network debug，can  on Chrome Brower debug ， look SharePreferences,sqlite data ...
             * method:input [chrome://inspect/] into Chrome Brower
             * but need take init [Stetho.initializeWithDefaults(this)] on yours MyApplication
             */
            builder.addNetworkInterceptor(new StethoInterceptor());
            Stetho.initializeWithDefaults(context);
        }
        okHttpClient = builder.build();
    }

    /**
     * CONSTRUCT 1
     * set yours OkHttpClient
     * @param client
     */
    public OkHttpStack(OkHttpClient client) {
        this.okHttpClient =client;

    }



    /**
     * if someone don't init throw this message
     * @return
     */
    public static OkHttpClient getOkHttpClient() {
        if (okHttpClient == null) throw new NullPointerException("VolleyOkHttp must be init On MyApplication");
        return okHttpClient;
    }


    /**
     * get BuildConfig values from context
     * @param context
     * @param fieldName
     * @return
     */
    private  Object getBuildConfigValue(Context context, String fieldName) {
        try {
            Class<?> clazz = Class.forName(context.getPackageName() + ".BuildConfig");
            Field field = clazz.getField(fieldName);
            return field.get(null);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static HttpEntity entityFromOkHttpResponse(Response response) throws IOException {
        BasicHttpEntity entity = new BasicHttpEntity();
        ResponseBody body = response.body();

        entity.setContent(body.byteStream());
        entity.setContentLength(body.contentLength());
        entity.setContentEncoding(response.header("Content-Encoding"));

        if (body.contentType() != null) {
            entity.setContentType(body.contentType().type());
        }
        return entity;
    }

    @SuppressWarnings("deprecation")
    private static void setConnectionParametersForRequest
            (okhttp3.Request.Builder builder, Request<?> request)
            throws IOException, AuthFailureError {
        switch (request.getMethod()) {
            case Request.Method.DEPRECATED_GET_OR_POST:
                byte[] postBody = request.getPostBody();
                if (postBody != null) {
                    builder.post(RequestBody.create
                            (MediaType.parse(request.getPostBodyContentType()), postBody));
                }
                break;

            case Request.Method.GET:
                builder.get();
                break;

            case Request.Method.DELETE:
                builder.delete();
                break;

            case Request.Method.POST:

                builder.post(createRequestBody(request)==null?Util.EMPTY_REQUEST:createRequestBody(request));
                break;

            case Request.Method.PUT:
                builder.put(createRequestBody(request));
                break;

            case Request.Method.HEAD:
                builder.head();
                break;

            case Request.Method.OPTIONS:
                builder.method("OPTIONS", null);
                break;

            case Request.Method.TRACE:
                builder.method("TRACE", null);
                break;

            case Request.Method.PATCH:
                builder.patch(createRequestBody(request));
                break;

            default:
                throw new IllegalStateException("Unknown method type.");
        }
    }

    private static RequestBody createRequestBody(Request request) throws AuthFailureError {
        final byte[] body = request.getBody();

        Log.i("body",body.getClass().toString());
        if (body == null) return null;

        return RequestBody.create(MediaType.parse(request.getBodyContentType()), body);
    }

    private static ProtocolVersion parseProtocol(final Protocol protocol) {
        switch (protocol) {
            case HTTP_1_0:
                return new ProtocolVersion("HTTP", 1, 0);
            case HTTP_1_1:
                return new ProtocolVersion("HTTP", 1, 1);
            case SPDY_3:
                return new ProtocolVersion("SPDY", 3, 1);
            case HTTP_2:
                return new ProtocolVersion("HTTP", 2, 0);
        }

        throw new IllegalAccessError("Unkwown protocol");
    }

    @Override
    public HttpResponse performRequest(Request<?> request, Map<String, String> additionalHeaders)
            throws IOException, AuthFailureError {
        int timeoutMs = request.getTimeoutMs();
        OkHttpClient client = getOkHttpClient().newBuilder()
                .readTimeout(timeoutMs, TimeUnit.MILLISECONDS)
                .connectTimeout(timeoutMs, TimeUnit.MILLISECONDS)
                .writeTimeout(timeoutMs, TimeUnit.MILLISECONDS)
                .build();

        okhttp3.Request.Builder okHttpRequestBuilder = new okhttp3.Request.Builder();
        Map<String, String> headers = request.getHeaders();
        for (final String name : headers.keySet()) {
            okHttpRequestBuilder.addHeader(name, headers.get(name));
        }

        for (final String name : additionalHeaders.keySet()) {
            okHttpRequestBuilder.addHeader(name, additionalHeaders.get(name));
        }

        setConnectionParametersForRequest(okHttpRequestBuilder, request);

        okhttp3.Request okhttp3Request = okHttpRequestBuilder.url(request.getUrl()).build();
        Response okHttpResponse = client.newCall(okhttp3Request).execute();

        StatusLine responseStatus = new BasicStatusLine
                (
                        parseProtocol(okHttpResponse.protocol()),
                        okHttpResponse.code(),
                        okHttpResponse.message()
                );

        BasicHttpResponse response = new BasicHttpResponse(responseStatus);
        response.setEntity(entityFromOkHttpResponse(okHttpResponse));

        Headers responseHeaders = okHttpResponse.headers();
        for (int i = 0, len = responseHeaders.size(); i < len; i++) {
            final String name = responseHeaders.name(i), value = responseHeaders.value(i);
            if (name != null) {
                response.addHeader(new BasicHeader(name, value));
            }
        }

        return response;
    }
}
