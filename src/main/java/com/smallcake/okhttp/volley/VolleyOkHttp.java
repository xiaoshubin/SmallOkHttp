package com.smallcake.okhttp.volley;

import android.content.Context;

import com.android.volley.toolbox.StringRequest;
import com.smallcake.okhttp.SmallOkHttp;
import com.smallcake.okhttp.volley.callback.VolleyBaseCallback;
import com.smallcake.okhttp.volley.request.PostRequest;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import okhttp3.OkHttpClient;

import static com.android.volley.Request.Method.GET;

/**
 * MyApplication --  com.smallcake.okhttp
 * Created by Small Cake on  2017/9/11 10:09.
 */

public class VolleyOkHttp {

    private static volatile MySingleton mySingleton;


    public static void initClient(@Nonnull Context context) {
        if (mySingleton == null) {
            synchronized (SmallOkHttp.class) {
                if (mySingleton == null) {
                    mySingleton = MySingleton.getInstance(context);
                }
            }
        }
    }

    /**
     * if you have yours OkHttpClient own,you can init by this
     * @param context
     * @param client
     */
    public static void initClient(@Nonnull Context context,@Nonnull OkHttpClient client) {
        if (mySingleton == null) {
            synchronized (SmallOkHttp.class) {
                if (mySingleton == null) {
                    mySingleton = MySingleton.getInstance(context,client);
                }
            }
        }
    }

    /**
     * if someone don't init throw this message
     * @return
     */
    public static MySingleton getMySingleton() {
        if (mySingleton == null) throw new NullPointerException("VolleyOkHttp must be init On yours MyApplication");
        return mySingleton;
    }


    /**
     * 【POST】
     * @param url
     * @param map nullable
     * @param callback
     */
    public static void post(String url, @Nullable final Map<String, String> map, VolleyBaseCallback callback){
        PostRequest request = new PostRequest(url,map, callback);
        getMySingleton().addToRequestQueue(request);
    }
    public static void get(String url, @Nullable final Map<String, String> map, VolleyBaseCallback callback){
        if (map!=null){
            StringBuilder builder  = new StringBuilder("?");
            for (String key: map.keySet()){
                builder.append(key).append("=").append(map.get(key)).append("&");
            }
            url += builder.toString().substring(0, builder.length() - 1);

        }
        StringRequest request = new StringRequest(GET, url,callback, callback);
        getMySingleton().addToRequestQueue(request);
    }












}
