package com.smallcake.okhttp.callback;

import okhttp3.Callback;

/**
 * MyApplication --  com.smallcake.okhttp.callback
 * Created by Small Cake on  2017/9/9 11:04.
 */

public abstract class UploadCallback implements Callback {

    public abstract void onProgress(long currentLength, long contentLength);
}
