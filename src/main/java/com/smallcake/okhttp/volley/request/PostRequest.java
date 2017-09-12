package com.smallcake.okhttp.volley.request;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.smallcake.okhttp.volley.callback.VolleyBaseCallback;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import static com.android.volley.Request.Method.POST;

/**
 * MyApplication --  com.smallcake.okhttp.volley.request
 * Created by Small Cake on  2017/9/12 10:31.
 */

public class PostRequest extends Request<String> {


    Map<String,String> mMaps;
    VolleyBaseCallback mCallback;
    public PostRequest(String url, Map<String,String> map, VolleyBaseCallback callback) {
        super(POST,url, callback);
        mMaps = map;
        mCallback = callback;
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return mMaps;
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        String parsed;
        try {
            parsed = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
        } catch (UnsupportedEncodingException e) {
            parsed = new String(response.data);
        }
        return Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(String response) {
        mCallback.onResponse(response);
    }

    @Override
    public void deliverError(VolleyError error) {
        mCallback.onErrorResponse(error);
    }
}
