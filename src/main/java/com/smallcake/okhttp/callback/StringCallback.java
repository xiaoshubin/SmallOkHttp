package com.smallcake.okhttp.callback;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;
import okhttp3.Callback;

/**
 * MyApplication --  com.smallcake.okhttp
 * Created by Small Cake on  2017/9/7 11:00.
 */

public abstract class StringCallback implements Callback{

    @Override
    public void onFailure(Call call, IOException e) {
        onFail(e);
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        if (response.isSuccessful())onSuccess(response.body().string());
    }

    public abstract void onFail(IOException e);
    public abstract void onSuccess(String string);
}
