package com.smallcake.okhttp;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

/**
 * MyApplication --  com.smallcake.okhttp
 * Created by Small Cake on  2017/8/30 14:13.
 * 小小下载服务
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

        public void startDownload(final Activity context,@NotNull final String downUrl, final String savePath, final String saveFileName, final DownloadListener listener) {

            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (context==null){
                        SmallOkHttp.download(downUrl,savePath,saveFileName, listener);
                    }else{
                        SmallOkHttp.downloadUI(context,downUrl,savePath,saveFileName, listener);
                    }

                }
            }).start();


        }


    }

}
