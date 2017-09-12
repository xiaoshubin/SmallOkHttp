package com.smallcake.okhttp.volley.callback;


import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * MyApplication --  com.smallcake.okhttp.volley
 * Created by Small Cake on  2017/9/11 17:31.
 */

public abstract class VolleyJSONObjectCallback implements VolleyBaseCallback<String> {
    public abstract void onSuccess(JSONObject jsonObject);


    @Override
    public void onResponse(String response) {
        try {
            onSuccess(new JSONObject(response));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }
}
