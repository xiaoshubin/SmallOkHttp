# SmallOkHttp
封装OkHttp3.0

#### 通过服务下载文件,下载完成或失败，自动关闭服务
1. 首先,添加相关权限（6.0+需要自己申请读写权限），和服务
```
<uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.MOUNT_UNMOUNT_FILESYSTEMS" />
    <application
        android:name=".MyApplication"
        android:allowBackup="true"
        android:icon="@mipmap/cake"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/cake"
        android:supportsRtl="true"
        android:theme="@style/AppTheme">
        <service android:name="com.smallcake.okhttp.SmallDownloadService"/>
    </application>
```
2. 然后，savePath和saveName可以为null
```

        String downUrl = "https://downpack.baidu.com/appsearch_AndroidPhone_v7.9.3(1.0.64.143)_1012271b.apk";
        String savePath = Environment.getExternalStorageDirectory()+ File.separator + Environment.DIRECTORY_DOWNLOADS+ File.separator;
        String saveName = "百度助手.apk";
        SmallOkHttp.downloadUIWithService(this, downUrl, savePath, saveName, new DownloadListener() {
            @Override
            public void start(long totalSize) {L.i(" 开始下载 " + totalSize);}
            @Override
            public void downloading(int percentage, long currentSize) {
                L.i("下载进度==" + percentage + "% 已下载== " + currentSize);
            }
            @Override
            public void successed(String successPath, String successFileName) {L.i(successFileName + " 已保存至 " + successPath);}
            @Override
            public void failed(IOException e) {L.i(" 下载失败 " + e.getMessage());}
        });
    
```
