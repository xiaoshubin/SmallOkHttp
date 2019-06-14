package com.smallcake.okhttp.retrofit2;

import android.app.Application;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import com.smallcake.okhttp.SmallOkHttp;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * MyApplication --  com.smallcake.okhttp.retrofit2
 * Created by Small Cake on  2017/9/13 9:37.
 */

public class RetrofitHttp {
    private static OkHttpClient okHttpClient;
    private static Retrofit retrofit;
    private static String baseHost;
    public static void init(Application context, String defaultHost){
         okHttpClient = SmallOkHttp.createOkHttpClient(context);
        baseHost = defaultHost;
        setRetrofit(defaultHost);
    }
    public static void init( String defaultHost, OkHttpClient client){
         okHttpClient = client;
        baseHost =defaultHost;
        setRetrofit(defaultHost);
    }

    private static void setRetrofit(String defaultHost) {
        retrofit = new Retrofit.Builder()
                        .baseUrl(defaultHost)
                        .client(okHttpClient)
                        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                        .addConverterFactory(GsonConverterFactory.create()).build();
    }

    /**
     * 使用init初始化里面的host
     * @return
     */
    private static Retrofit getRetrofit() {
        if (retrofit==null) throw new NullPointerException("RetrofitHttp must be init");
        return retrofit;
    }
    public static  <T> T create(final Class<T> service) {
        return getRetrofit().create(service);
    }



    /**
     * 因为有的应用也可能有其他的host
     * @param host
     * @return
     */
    public static Retrofit getRetrofit(String host) {
        return new Retrofit.Builder()
                .baseUrl(host)
                .client(okHttpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create()).build();
    }

    public static final class Builder {
        String defaultHost = baseHost;
        OkHttpClient okHttpClient = RetrofitHttp.okHttpClient;
        public Builder host( String host) {
            defaultHost = host;
            return this;
        }
        public Builder okHttpClient(OkHttpClient okClient) {
            okHttpClient = okClient;
            return this;
        }
        public Retrofit build() {
            return new Retrofit.Builder()
                    .baseUrl(defaultHost)
                    .client(okHttpClient)
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create()).build();
        }
    }



}
