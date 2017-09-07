package com.smallcake.okhttp.callback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * MyApplication --  com.smallcake.okhttp
 * Created by Small Cake on  2017/9/7 11:14.
 */

public abstract class JSONObjectCallback implements Callback {
    public abstract void onResponse(Call call, JSONObject jsonObject);
    @Override
    public void onFailure(Call call, IOException e) {

    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        try {
            onResponse(call,new JSONObject(response.body().string()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
