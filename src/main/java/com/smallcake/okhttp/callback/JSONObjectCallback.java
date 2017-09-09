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
    @Override
    public void onFailure(Call call, IOException e) {
        onFail(e);
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        if (response.isSuccessful()) {
            try {
                onSuccess(new JSONObject(response.body().string()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    public abstract void onFail(IOException e);
    public abstract void onSuccess(JSONObject jsonObject)throws JSONException;
}
