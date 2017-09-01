package com.smallcake.okhttp;

import java.io.IOException;

/**
 * MyApplication --  com.smallcake.okhttp
 * Created by Small Cake on  2017/8/29 15:55.
 */

public interface DownloadListener {
    void start(long totalSize);//文件总大小
    void downloading(int percentage,long currentSize);//百分比进度，当前下载量
    void successed(String successPath,String successFileName);//文件下载成功
    void failed(IOException e);//下载失败
}
