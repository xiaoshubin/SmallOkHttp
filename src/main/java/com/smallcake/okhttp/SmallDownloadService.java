package com.smallcake.okhttp;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.smallcake.okhttp.callback.DownloadCallback;

import org.jetbrains.annotations.NotNull;

/**
 * MyApplication --  com.smallcake.okhttp
 * Created by Small Cake on  2017/8/30 14:13.
 * download service just small
 */

public class SmallDownloadService extends Service {
    public static final String TAG = "SmallDownloadService";
    private MyBinder mBinder = new MyBinder();
    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG,"onCreate########################");

    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG,"onStartCommand########################");
        return super.onStartCommand(intent, flags, startId);

    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG,"onDestroy########################");
    }
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class MyBinder extends Binder {

        public void startDownload(@NotNull final String downUrl,final DownloadCallback callback) {

            new Thread(new Runnable() {
                @Override
                public void run() {
                    SmallOkHttp.download(downUrl, callback);
                }
            }).start();


        }


    }

}
