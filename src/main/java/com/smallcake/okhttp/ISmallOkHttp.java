package com.smallcake.okhttp;

import android.os.Environment;

import java.io.File;

/**
 * MyApplication --  com.smallcake.okhttp
 * Created by Small Cake on  2017/8/25 10:43.
 * 规范工具接口
 */

public interface ISmallOkHttp {
    int CONNECT_TIME_OUT = 20*1000;//连接超时时间
    int READ_TIME_OUT = 20*1000;//读取超时时间
    int WRITE_TIME_OUT = 20*1000;//读取超时时间
    String DOWNLOAD_PATH = Environment.getExternalStorageDirectory()+ File.separator+Environment.DIRECTORY_DOWNLOADS;//默认的下载下载路径
}
