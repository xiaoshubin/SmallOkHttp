package com.smallcake.okhttp;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.facebook.stetho.Stetho;
import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.smallcake.okhttp.callback.DownloadCallback;
import com.smallcake.okhttp.callback.UploadCallback;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;

import static android.content.Context.BIND_AUTO_CREATE;


/**
 *  compile 'com.squareup.okhttp3:okhttp:3.8.1'
    compile 'com.squareup.okio:okio:1.13.0'
    compile 'com.squareup.okhttp3:logging-interceptor:3.4.1'
    compile 'com.facebook.stetho:stetho-okhttp3:1.4.2'
 *
 *
 *
 * MyApplication --  com.smallcake.okhttp
 * Created by Small Cake on  2017/8/25 10:42.
 * base com.squareup.okhttp3:okhttp，
 * Application singleton pattern
 * OkHttp more info：http://square.github.io/okhttp/
 */

public class SmallOkHttp{
    long MAX_CACHE_SIZE = 10*1024*1024;//max cache default 10M
    int CONNECT_TIME_OUT = 20*1000;//connect time out
    int READ_TIME_OUT = 20*1000;//read time out
    int WRITE_TIME_OUT = 20*1000;//write time out

    private static volatile SmallOkHttp mInstance;
    private static OkHttpClient okHttpClient;

    /**
     * CONSTRUCT 1
     * set yours OkHttpClient
     * @param client
     */
    private SmallOkHttp(OkHttpClient client) {
        this.okHttpClient =client;

    }
    /**
     * CONSTRUCT 2
     * default okhttp client
     * @param context
     */
    private SmallOkHttp(Application context) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .cache(new Cache(context.getCacheDir(), MAX_CACHE_SIZE))
                .readTimeout(READ_TIME_OUT, TimeUnit.MILLISECONDS)
                .writeTimeout(WRITE_TIME_OUT, TimeUnit.MILLISECONDS)
                .connectTimeout(CONNECT_TIME_OUT, TimeUnit.MILLISECONDS);

        boolean debug = (Boolean) getBuildConfigValue(context, "DEBUG");
        if (debug) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(interceptor);
            /**
             * FaceBook network debug，can  on Chrome Brower debug ， look SharePreferences,sqlite data ...
             * method:input [chrome://inspect/] into Chrome Brower
             * but need take init [Stetho.initializeWithDefaults(this)] on yours MyApplication
             */
            builder.addNetworkInterceptor(new StethoInterceptor());
            Stetho.initializeWithDefaults(context);
        }
        okHttpClient = builder.build();

    }

    /**
     * get BuildConfig values from context
     * @param context
     * @param fieldName
     * @return
     */
    private  Object getBuildConfigValue(Context context, String fieldName) {
        try {
            Class<?> clazz = Class.forName(context.getPackageName() + ".BuildConfig");
            Field field = clazz.getField(fieldName);
            return field.get(null);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * INIT 1
     * ★★★★★must init on yours MyApplication
     * @param application
     * @return SmallOkHttp
     */
    public static SmallOkHttp initClient(@Nonnull Application application) {
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
     * INIT 2
     * ★★★★★must init on yours MyApplication
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

    /**
     * if someone don't init throw this message
     * @return
     */
    public static OkHttpClient getOkHttpClient() {
        if (okHttpClient == null) throw new NullPointerException("SmallOkHttp must be init On MyApplication");
        return okHttpClient;
    }



    /**
     * 【POST】
     * params type is Map<String, String>
     * Callback can use StringCallback or JSONObjectCallback
     * @param url
     * @param map
     * @param callback
     */

    public static void post(String url,  Map<String, String> map, final Callback callback) {
        RequestBody body=null;
        if (map!=null){
            FormBody.Builder builder = new FormBody.Builder();
            for (String key : map.keySet()) builder.add(key, map.get(key));
            body = builder.build();
        }
        Request request = new Request.Builder().url(url).post(body != null ? body : okhttp3.internal.Util.EMPTY_REQUEST).build();
        getOkHttpClient().newCall(request).enqueue(callback);
    }

    /**
     * 【GET】
     * no params, if you want ,you can use  ? and & add behind url
     * Callback can use StringCallback or JSONObjectCallback
     * @param url
     * @param callback
     */
    public static void  get(String url, final Callback callback){
        Request request = new Request.Builder().url(url).build();
        getOkHttpClient().newCall(request).enqueue(callback);
    }

    /**
     * 6.0+ need take dynamic permissions
     * <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
     * <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
     * <uses-permission android:name="android.permission.MOUNT_UNMOUNT_FILESYSTEMS" />
     * @param downUrl
     * just use for SmallDownloadService
     */

    protected static void download(String downUrl,DownloadCallback callback) {
        Request request = new Request.Builder().get().url(downUrl).build();
        getOkHttpClient().newCall(request).enqueue(callback);

    }

    /**
     * 【DOWNLOAD】
     * DOWNLOAD WITH SERVICE ,DATA BACK ON UI
     * download callback on UI threed
     * download end unbind service auto
     * download some file < 100M
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
                        call.cancel();
                        e.printStackTrace();
                        unBind();
                    }
                });
            }

            private void unBind() {
                if (isServiceRunning(context,SmallDownloadService.class.getName()))context.unbindService(this);

            }
        };
        Intent bindIntent = new Intent(context, SmallDownloadService.class);
       if (context!=null)context.bindService(bindIntent, connection, BIND_AUTO_CREATE);


    }


    private static boolean isServiceRunning(Context mContext, String  serviceName) {
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

    /**
     * 【UPLOAD】
     * must setType MultipartBody.FORM beacuse you need params,
     * need fileKey,fileName,RequestBody body is a file stream
     * FileRequestBody can get progress then transmit to UploadCallback
     * @param url
     * @param map
     * @param fileKey
     * @param fileName
     * @param file
     * @param callback
     */
    public static void upload(String url, Map<String,String> map, String fileKey, String fileName, File file, final UploadCallback callback){
        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);
        for (String key:map.keySet()) builder.addFormDataPart(key,map.get(key));
        builder.addFormDataPart(fileKey,fileName,RequestBody.create(MediaType.parse("application/octet-stream"),file));
        FileRequestBody body = new FileRequestBody(builder.build(), new FileRequestBody.LoadingListener() {
            @Override
            public void onProgress(long currentLength, long contentLength) {
                callback.onProgress(currentLength,contentLength);
            }
        });
        Request request = new Request.Builder().url(url).post(body).build();
        getOkHttpClient().newCall(request).enqueue(callback);
    }


}
