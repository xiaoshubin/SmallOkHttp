package com.smallcake.okhttp.volley;

import android.content.Context;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import okhttp3.OkHttpClient;

/**
 * Created by Small Cake on 2015/12/29.
 * Here are some examples of performing RequestQueue operations using the singleton class:
 * // Get a RequestQueue
 * RequestQueue queue = MySingleton.getInstance(this.getApplicationContext()).
 * getRequestQueue();
 * // Add a request (in this example, called stringRequest) to your RequestQueue.
 * MySingleton.getInstance(this).addToRequestQueue(stringRequest);
 */
public class MySingleton {

    private static MySingleton mInstance;
    private RequestQueue mRequestQueue;
    private static Context mCtx;
    OkHttpClient mOkHttpClient;

    private MySingleton(Context context) {
        mCtx = context;
        mRequestQueue = getRequestQueue();

    }
    private MySingleton(Context context, OkHttpClient okHttpClient) {
        mCtx = context;
        mOkHttpClient = okHttpClient;
        mRequestQueue = getRequestQueue();

    }

    public static synchronized MySingleton getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new MySingleton(context);
        }
        return mInstance;
    }
    public static synchronized MySingleton getInstance(Context context, OkHttpClient okHttpClient) {
        if (mInstance == null) {
            mInstance = new MySingleton(context,okHttpClient);
        }
        return mInstance;
    }

    /**
     * get the request queue
     * @return
     */
    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext(),mOkHttpClient==null?new OkHttpStack(mCtx):new OkHttpStack(mOkHttpClient));
        }
        return mRequestQueue;
    }

    /**
     * add some request into volley queue
     * @param req
     * @param <T>
     */
    public <T> void addToRequestQueue(Request<T> req) {
        req.setRetryPolicy(new DefaultRetryPolicy());
        getRequestQueue().add(req);
    }



}
