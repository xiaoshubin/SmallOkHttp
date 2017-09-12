package com.smallcake.okhttp.callback;

import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * MyApplication --  com.smallcake.okhttp.callback
 * Created by Small Cake on  2017/9/7 11:38.
 *
 */

public abstract class DownloadCallback implements Callback {
    String DOWNLOAD_PATH = Environment.getExternalStorageDirectory()
            +File.separator
            +Environment.DIRECTORY_DOWNLOADS
            +File.separator;//default download path

    String downUrl;
    String savePath;
    String saveFileName;

    public DownloadCallback() {
    }

    public DownloadCallback(String downUrl, String savePath, String saveFileName) {
        this.downUrl = downUrl;
        this.savePath = savePath;
        this.saveFileName = saveFileName;
    }

    public abstract void onStart(long totalSize);//file total size
    public abstract void onProgress(int percentage,long currentSize);//%，current size
    public abstract void onSuccessed(String successPath,String successFileName);//success


    @Override
    public void onResponse(Call call, Response response) throws IOException {
        long totalSize = response.body().contentLength();onStart(totalSize);
        InputStream is = response.body().byteStream();
        File file = new File(savePath == null ? DOWNLOAD_PATH : savePath, saveFileName == null ? getDownloadFileName(downUrl) : saveFileName);
        if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
        FileOutputStream fos = new FileOutputStream(file);
        byte[] buf = new byte[2048];
        long sum = 0L;
        int len = 0;
        while ((len = is.read(buf)) != -1) {
            fos.write(buf, 0, len);
            sum += len;
            int percentage = (int) ((sum * 100) / totalSize);
            onProgress(percentage, sum);
            if (percentage == 100)
                onSuccessed(savePath == null ? DOWNLOAD_PATH : savePath, saveFileName == null ? getDownloadFileName(downUrl) : saveFileName);//下载成功
        }
        fos.flush();
        fos.close();
        is.close();
    }
    private String getDownloadFileName(String url) {
        return url.substring(url.lastIndexOf("/") + 1, url.length());
    }
}
