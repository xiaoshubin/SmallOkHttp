package com.smallcake.okhttp.volley.callback;

import com.android.volley.VolleyError;

/**
 * MyApplication --  com.smallcake.okhttp.volley
 * Created by Small Cake on  2017/9/11 16:35.
 */

public  abstract class VolleyStringCallback implements VolleyBaseCallback<String> {
    public abstract void onSuccess(String string);
    @Override
    public void onResponse(String response) {
        onSuccess(response);
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }
}
