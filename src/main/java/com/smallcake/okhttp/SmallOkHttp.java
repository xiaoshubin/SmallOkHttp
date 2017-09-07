package com.smallcake.okhttp;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.smallcake.okhttp.callback.DownloadCallback;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;

import static android.content.Context.BIND_AUTO_CREATE;


/**
 * MyApplication --  com.smallcake.okhttp
 * Created by Small Cake on  2017/8/25 10:42.
 * base com.squareup.okhttp3:okhttp，
 * Application singleton pattern
 * OkHttp more info：http://square.github.io/okhttp/
 */

public class SmallOkHttp implements ISmallOkHttp {


    private static volatile SmallOkHttp mInstance;
    private static OkHttpClient okHttpClient;

    /**
     * default okhttp client
     * @param context
     */
    private SmallOkHttp(Context context) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .cache(new Cache(context.getCacheDir(), MAX_CACHE_SIZE))
                .readTimeout(READ_TIME_OUT, TimeUnit.MILLISECONDS)
                .writeTimeout(WRITE_TIME_OUT, TimeUnit.MILLISECONDS)
                .connectTimeout(CONNECT_TIME_OUT, TimeUnit.MILLISECONDS);
        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(interceptor);
            /**
             * FaceBook network debug，can  on Chrome Brower debug ， look SharePreferences,sqlite data ...
             * method:input [chrome://inspect/] into brower
             * but need take init [Stetho.initializeWithDefaults(this)] on yours MyApplication
             */

            builder.addNetworkInterceptor(new StethoInterceptor());
        }
        okHttpClient = builder.build();

    }

    /**
     * yours OkHttpClient
     * @param client
     */
    private SmallOkHttp(OkHttpClient client) {
        this.okHttpClient =client;

    }

    /**
     * must init on yours MyApplication
     *
     * @param application
     * @return SmallOkHttp
     */
    public static SmallOkHttp initClient(@Nonnull Context application) {
        if (mInstance == null) {
            synchronized (SmallOkHttp.class) {
                if (mInstance == null) {
                    mInstance = new SmallOkHttp(application);
                }
            }
        }
        return mInstance;
    }
    /**
     * must init on yours MyApplication
     *
     * @param client
     * @return SmallOkHttp
     */
    public static SmallOkHttp initClient(@Nonnull OkHttpClient client) {
        if (mInstance == null) {
            synchronized (SmallOkHttp.class) {
                if (mInstance == null) {
                    mInstance = new SmallOkHttp(client);
                }
            }
        }
        return mInstance;
    }

    public static OkHttpClient getOkHttpClient() {
        if (okHttpClient == null) throw new NullPointerException("SmallOkHttp must be init On MyApplication");
        return okHttpClient;
    }

    /**
     * 1【POST】【asyn】【sub Threed】【with params】
     * without Activity,run in sub threed，don't use data into views
     * may be that you can use with service
     *
     * @param url
     * @param map
     * @param callback
     */
    public static void post(String url, Map<String, String> map, Callback callback) {
        post(null, url, map, callback);
    }

    /**
     * 2【POST】【asyn】【UI Threed】【with params】
     * with Activity,you can use callback data into views
     * such as：textView.setText(jsonObject.getString("reason"));
     * params type is Map<String, String>
     * return data is JSONObject
     * @param context
     * @param url
     * @param map
     * @param callback
     */

    public static void post(final Activity context, String url, Map<String, String> map, final Callback callback) {
        FormBody.Builder builder = new FormBody.Builder();
        for (String key : map.keySet()) builder.add(key, map.get(key));
        RequestBody body = builder.build();
        Request request = new Request.Builder().url(url).post(body != null ? body : okhttp3.internal.Util.EMPTY_REQUEST).build();
        getOkHttpClient().newCall(request).enqueue(callback);
    }

    /**
     * 【GET】【asyn】【sub threed】【no params】
     *
     * @param url
     * @param listener
     */
    public static void get(String url, final Callback listener) {
        get(null, url, listener);
    }




    /**
     * 【GET】【asyn】【UI Threed】
     *
     * @param context
     * @param url
     * @param listener
     */
    public static void get(final Activity context, String url, Callback listener) {

        Request request = new Request.Builder().url(url).build();
        Call call = getOkHttpClient().newCall(request);


        if (context.isDestroyed()){
            call.cancel();
            return;
        }
        call.enqueue(listener);

    }

    /**
     * 6.0+ need take dynamic permissions
     * <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
     * <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
     * <uses-permission android:name="android.permission.MOUNT_UNMOUNT_FILESYSTEMS" />
     *
     * @param downUrl      download url
     * if savePath=null，default = Download
     * if saveFileName=null,default = take name from url
     */

    public static void download(String downUrl,DownloadCallback callback) {
        Request request = new Request.Builder().get().url(downUrl).build();
        getOkHttpClient().newCall(request).enqueue(callback);

    }






    /**
     * download callback on UI threed
     *
     * @param context
     * @param downUrl
     * @param savePath
     * @param saveFileName
     * @param callback
     */
    public static void downloadUIWithService(final Activity context, final String downUrl, final String savePath, final String saveFileName, final DownloadCallback callback) {

        final  Handler mHandler  = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case 0:
                        Bundle data = msg.getData();
                        long totalSize = data.getLong("totalSize");
                        callback.onStart(totalSize);
                        break;
                    case 1:
                        Bundle data1 = msg.getData();
                        int percentage = data1.getInt("percentage");
                        long currentSize = data1.getLong("currentSize");
                        callback.onProgress(percentage,currentSize);
                        break;
                    case 2:
                        Bundle data2 = msg.getData();
                        String successPath = data2.getString("successPath");
                        String successFileName = data2.getString("successFileName");
                        callback.onSuccessed(successPath,successFileName);
                        break;
                    case -1:

                        break;
                }
            }
        };
        ServiceConnection connection = new ServiceConnection() {
            @Override
            public void onServiceDisconnected(ComponentName name) {
            }

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {

                SmallDownloadService.MyBinder mBinder = (SmallDownloadService.MyBinder) service;
                mBinder.startDownload(downUrl, new DownloadCallback(downUrl,savePath,saveFileName) {
                    int progress;
                    @Override
                    public void onStart(long totalSize) {
                        progress = 0;
                        Message message = Message.obtain();
                        message.what = 0;
                        Bundle bundle = new Bundle();
                        bundle.putLong("totalSize",totalSize);
                        message.setData(bundle);
                        mHandler.sendMessage(message);

                    }

                    @Override
                    public void onProgress(int percentage, long currentSize) {
                        if (progress!=percentage){
                            progress = percentage;
                            Message message = Message.obtain();
                            message.what = 1;
                            Bundle bundle = new Bundle();
                            bundle.putInt("percentage",percentage);
                            bundle.putLong("currentSize",currentSize);
                            message.setData(bundle);
                            mHandler.sendMessage(message);
                        }

                    }

                    @Override
                    public void onSuccessed(String successPath, String successFileName) {
                        Message message = Message.obtain();
                        message.what = 2;
                        Bundle bundle = new Bundle();
                        bundle.putString("successPath",successPath);
                        bundle.putString("successFileName",successFileName);
                        message.setData(bundle);
                        mHandler.sendMessage(message);
                        unBind();

                    }

                    @Override
                    public void onFailure(Call call, IOException e) {
                        super.onFailure(call, e);
                        Message message = Message.obtain();
                        message.what = -1;
                        unBind();
                    }
                });
            }

            private void unBind() {
                if (isServiceRunning(context,SmallDownloadService.class.getName()))context.unbindService(this);

            }
        };
        Intent bindIntent = new Intent(context, SmallDownloadService.class);
       if (context!=null&&!context.isDestroyed())context.bindService(bindIntent, connection, BIND_AUTO_CREATE);
    }


    public static boolean isServiceRunning(Context mContext, String  serviceName) {
        boolean isRunning = false;
        ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList = activityManager.getRunningServices(30);
        if (!(serviceList.size()>0)) return false;
        for (int i=0; i<serviceList.size(); i++) {
            if (serviceList.get(i).service.getClassName().equals(serviceName) == true) {
                isRunning = true;
                break;
            }
        }
        return isRunning;
    }


}
