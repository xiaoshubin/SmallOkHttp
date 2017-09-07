package com.smallcake.okhttp.callback;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * MyApplication --  com.smallcake.okhttp
 * Created by Small Cake on  2017/9/7 11:00.
 */

public abstract class StringCallback implements Callback {


    public abstract void onResponse(Call call,String string);
    @Override
    public  void onResponse(Call call, Response response) throws IOException {
        onResponse(call,response.body().string());
    }

    @Override
    public void onFailure(Call call, IOException e) {

    }
}
