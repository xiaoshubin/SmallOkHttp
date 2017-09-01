package com.smallcake.okhttp;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.facebook.stetho.okhttp3.StethoInterceptor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

import static android.content.Context.BIND_AUTO_CREATE;

/**
 * MyApplication --  com.smallcake.okhttp
 * Created by Small Cake on  2017/8/25 10:42.
 * 基于com.squareup.okhttp3:okhttp的封装，
 * OkHttp使用：http://square.github.io/okhttp/
 */

public class SmallOkHttp implements ISmallOkHttp{

    final static String TAG = SmallOkHttp.class.getSimpleName();

    private static volatile  SmallOkHttp instance;
    private OkHttpClient okHttpClient;


    private SmallOkHttp(){
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .readTimeout(READ_TIME_OUT, TimeUnit.MILLISECONDS)
                .writeTimeout(WRITE_TIME_OUT, TimeUnit.MILLISECONDS)
                .connectTimeout(CONNECT_TIME_OUT, TimeUnit.MILLISECONDS);
        if (true){
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(interceptor);
            /**
             * FaceBook 网络调试器，可在Chrome调试网络请求，查看SharePreferences,数据库等,
             * 查看方式，在浏览器输入chrome://inspect/
             * 但需要在自定义的Application中初始化Stetho.initializeWithDefaults(this);
             */

            builder.addNetworkInterceptor(new StethoInterceptor());
        }
        okHttpClient = builder.build();
    }


    private static SmallOkHttp getInstance() {
        if (instance == null) {
            synchronized (SmallOkHttp.class) {
                if (instance == null) {
                    instance = new SmallOkHttp();
                }
            }
        }
        return instance;
    }

    private OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    /**
     * 1【POST】【异步】【子线程】【有参】
     * 没有传入Activity,结果运行在子线程，无法设置数据到View上
     * 用于service或其他无需界面的线程，如后台数据
     * @param url
     * @param map
     * @param listener
     */
    public static void postJson(String url, TreeMap<String, String> map, final CallbackListener listener){
        postJson(null,url,map,listener);
    }
    /**2【POST】【异步】【UI线程】【有参】
     * 传入了Activity,结果数据可以直接设置在View上
     * 如：textView.setText(jsonObject.getString("reason"));
     * 返回的数据都转换为了JSONObject
     * 这里的参数使用了我们熟悉的TreeMap来代替，它对输入的键值对进行了排序，然后传递给FormBody
     * @param url
     * @param map
     * @param listener
     */
    public static void postJson(final Activity context, String url, TreeMap<String, String> map, final CallbackListener listener){
        FormBody.Builder builder = new FormBody.Builder();
        for (String key:map.keySet())builder.add(key,map.get(key));
        RequestBody body = builder.build();
        Request request = new Request.Builder().url(url).post(body!=null?body:okhttp3.internal.Util.EMPTY_REQUEST).build();
        getInstance().getOkHttpClient().newCall(request).enqueue(new SmallCallback(context,listener));
    }
    /**
     * 1【GET】【异步】【子线程】【无参】
     * @param url
     * @param listener
     */
    public static void getJson( String url, final CallbackListener listener){
        getJson(null,url,null,listener);
    }
    /**
     * 2【GET】【异步】【子线程】【有参】
     * @param url
     * @param map
     * @param listener
     */
    public static void getJson( String url, TreeMap<String,String> map, final CallbackListener listener){
        getJson(null,url,map,listener);
    }
    /**
     * 3【GET】【异步】【UI线程】【无参】
     * @param url
     * @param listener
     */
    public static void getJson( final Activity context,String url, final CallbackListener listener){
        getJson(context,url,null,listener);
    }

    /**
     * 4【GET】【异步】【UI线程】【有参】
     * @param context
     * @param url
     * @param map
     * @param listener
     */
    public static void getJson(final Activity context, String url, TreeMap<String,String> map, final CallbackListener listener){
        StringBuffer buffer = new StringBuffer(url+"?");
        if(map!=null) for (String key:map.keySet()) buffer.append(key).append("=").append(map.get(key)).append("&");
        String s = buffer.toString();
        s = s.substring(0,s.length()-1);//去掉最后多于的&
        Request request = new Request.Builder().url(s).build();
        getInstance().getOkHttpClient().newCall(request).enqueue(new SmallCallback(context,listener));
    }

    /**
     * 下载文件:6.0+需要动态权限申请
     * <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
     * <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
     * <uses-permission android:name="android.permission.MOUNT_UNMOUNT_FILESYSTEMS" />
     *
     * @param downUrl 下载地址
     * @param savePath 保存文件的路径
     * @param saveFileName 保存文件的名称
     * @param listener 下载回调
     * savePath=null，下载到Download
     * saveFileName=null,默认取下载文件名称
     */

    public static void download(final String downUrl, final String savePath, final String saveFileName, final DownloadListener listener){
        Request request = new Request.Builder().get().url(downUrl).build();
        getInstance().getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {//下载失败
                listener.failed(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                long totalSize = response.body().contentLength();
                listener.start(totalSize);
                InputStream is = response.body().byteStream();
                File file = new File(savePath==null?DOWNLOAD_PATH:savePath,saveFileName==null?getDownloadFileName(downUrl):saveFileName);
                //判断目标文件所在的目录是否存在
                if (!file.getParentFile().exists())file.getParentFile().mkdirs();

                FileOutputStream fos = new FileOutputStream(file);
                byte[] buf = new byte[2048];
                long sum = 0L;
                int len = 0;
                while ((len = is.read(buf)) != -1) {
                    fos.write(buf, 0, len);
                    sum += len;
                    int percentage = (int) ((sum * 100) / totalSize);
                    listener.downloading( percentage,sum);
                    if (percentage == 100)listener.successed(savePath==null?DOWNLOAD_PATH:savePath,saveFileName==null?getDownloadFileName(downUrl):saveFileName);//下载成功
                }
                fos.flush();//必须刷新文件才有内容
                fos.close();
                is.close();
            }
        });
    }

    public static void downloadUI(final Activity context, final String downUrl, final String savePath, final String saveFileName, final DownloadListener listener){
        Request request = new Request.Builder().get().url(downUrl).build();
        getInstance().getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {//下载失败
                listener.failed(e);
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            long totalSize = response.body().contentLength();
                            listener.start(totalSize);
                            InputStream is = response.body().byteStream();
                            File file = new File(savePath==null?DOWNLOAD_PATH:savePath,saveFileName==null?getDownloadFileName(downUrl):saveFileName);
                            //判断目标文件所在的目录是否存在
                            if (!file.getParentFile().exists())file.getParentFile().mkdirs();

                            FileOutputStream fos = new FileOutputStream(file);
                            byte[] buf = new byte[2048];
                            long sum = 0L;
                            int len = 0;
                            while ((len = is.read(buf)) != -1) {
                                fos.write(buf, 0, len);
                                sum += len;
                                int percentage = (int) ((sum * 100) / totalSize);
                                listener.downloading( percentage,sum);
                                if (percentage == 100)listener.successed(savePath==null?DOWNLOAD_PATH:savePath,saveFileName==null?getDownloadFileName(downUrl):saveFileName);//下载成功
                            }
                            fos.flush();//必须刷新文件才有内容
                            fos.close();
                            is.close();

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

            }
        });
    }

    /**
     * 通过url获取下载文件名称
     * @param url
     * @return
     */
    private static String getDownloadFileName(String url){
        return url.substring(url.lastIndexOf("/")+1,url.length());
    }

    /**
     * 下载嵌套类
     */
    static class DownData{
        Activity context;
        String downUrl;
        String savePath;
        String saveFileName;
        DownloadListener listener;
        public DownData(Activity context, String downUrl, String savePath, String saveFileName, DownloadListener listener) {
            this.context = context;
            this.downUrl = downUrl;
            this.savePath = savePath;
            this.saveFileName = saveFileName;
            this.listener = listener;
        }
    }

    /**
     * 回调的线程可以用于UI
     * @param context
     * @param downUrl
     * @param savePath
     * @param saveFileName
     * @param listener
     */
    public static void downloadUIWithService(Activity context,final String downUrl, final String savePath, final String saveFileName,final DownloadListener listener){
        downData = new DownData(context,downUrl,savePath,saveFileName,listener);
        Intent bindIntent = new Intent(context, SmallDownloadService.class);
        context.bindService(bindIntent, connection, BIND_AUTO_CREATE);
    }

    /**
     * 回调的数据不能用于UI
     * @param context
     * @param downUrl
     * @param savePath
     * @param saveFileName
     * @param listener
     */
    public static void downloadWithService(Activity context, final String downUrl, final String savePath, final String saveFileName, final DownloadListener listener){
        downData = new DownData(null,downUrl,savePath,saveFileName,listener);
        Intent bindIntent = new Intent(context, SmallDownloadService.class);
        context.bindService(bindIntent, connection, BIND_AUTO_CREATE);
    }


    private static DownData downData;
    private static SmallDownloadService.MyBinder mBinder;
    /**
     * 建立服务连接
     */
    private static ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {}

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBinder = (SmallDownloadService.MyBinder) service;
            mDownHandler.sendEmptyMessage(0);
        }
    };

    public static Handler mDownHandler  = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0://开始下载
                    mBinder.startDownload(downData.context, downData.downUrl, downData.savePath, downData.saveFileName, new DownloadListener() {
                        @Override
                        public void start(long totalSize) {
                            downData.listener.start(totalSize);
                        }

                        @Override
                        public void downloading(int percentage, long currentSize) {
                            downData.listener.downloading(percentage,currentSize);
                        }

                        @Override
                        public void successed(String successFileName, String successPath) {
                            downData.listener.successed(successFileName,successPath);
                            downData.context.unbindService(connection);
                        }

                        @Override
                        public void failed(IOException e) {
                            downData.listener.failed(e);
                            downData.context.unbindService(connection);

                        }
                    });
                    break;
            }
        }
    };




}
