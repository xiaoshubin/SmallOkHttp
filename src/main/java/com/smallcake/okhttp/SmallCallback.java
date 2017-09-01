package com.smallcake.okhttp;

import android.app.Activity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * MyApplication --  com.smallcake.okhttp
 * Created by Small Cake on  2017/8/25 18:06.
 */

public class SmallCallback implements Callback {
    Activity context;
    CallbackListener listener;

    public SmallCallback(Activity context, CallbackListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @Override
    public void onFailure(Call call, IOException e) {

    }

    @Override
    public void onResponse(Call call, final Response response) throws IOException {
        if (context!=null){
            //切换到UI线程
            context.runOnUiThread(new Runnable() {//运行在UI线程，方便直接赋值给控件
                @Override
                public void run() {
                    try {
                        if (listener!=null)listener.successed(new JSONObject(response.body().string()));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }else{
            try {
                if (listener!=null)listener.successed(new JSONObject(response.body().string()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
