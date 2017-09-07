package com.smallcake.okhttp;

/**
 * MyApplication --  com.smallcake.okhttp
 * Created by Small Cake on  2017/8/25 10:43.
 * some default data
 */

public interface ISmallOkHttp {
     long MAX_CACHE_SIZE = 10*1024*1024;//max cache default 10M
    int CONNECT_TIME_OUT = 20*1000;//connect time out
    int READ_TIME_OUT = 20*1000;//read time out
    int WRITE_TIME_OUT = 20*1000;//write time out

}
