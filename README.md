# SmallOkHttp
封装OkHttp3.0

#####通过服务下载文件
1.首先
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
2.然后
```
private static final int RC_WRITE = 0X0002;//获取拍照和读卡权限
    @AfterPermissionGranted(RC_WRITE)
    private void down() {
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        if (!EasyPermissions.hasPermissions(this, perms)) {
            EasyPermissions.requestPermissions(this, "没有此权限，将导致应用无法使用！", RC_WRITE, perms);
            return;
        }
        String downUrl = "https://downpack.baidu.com/appsearch_AndroidPhone_v7.9.3(1.0.64.143)_1012271b.apk";
        String savePath = Environment.getExternalStorageDirectory()+ File.separator + Environment.DIRECTORY_DOWNLOADS+ File.separator;
        String saveName = "百度助手.apk";
        SmallOkHttp.downloadUIWithService(this, downUrl, savePath, saveName, new DownloadListener() {
            @Override
            public void start(long totalSize) {
                L.i(" 开始下载 " + totalSize);
                progressBar.setProgress(0);
            }
            @Override
            public void downloading(int percentage, long currentSize) {
                L.i("下载进度==" + percentage + "% 已下载== " + FormatUtils.formatSize(MainActivity.this, currentSize));
                progressBar.setProgress(percentage);
            }
            @Override
            public void successed(String successPath, String successFileName) {
                L.i(successFileName + " 已保存至 " + successPath);
            }
            @Override
            public void failed(IOException e) {
                L.i(" 下载失败 " + e.getMessage());

            }
        });
    }
```
