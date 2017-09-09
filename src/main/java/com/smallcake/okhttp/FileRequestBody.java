package com.smallcake.okhttp;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

/**
 * MyApplication --  com.smallcake.okhttp
 * Created by Small Cake on  2017/9/8 17:52.
 */

public class FileRequestBody extends RequestBody {
    private RequestBody mRequestBody;
    private LoadingListener mLoadingListener;
    private long mContentLength;

    public FileRequestBody(RequestBody requestBody, LoadingListener loadingListener) {
        mRequestBody = requestBody;
        mLoadingListener = loadingListener;
    }

    //total length
    @Override
    public long contentLength() {
        try {
            if (mContentLength == 0)
                mContentLength = mRequestBody.contentLength();
            return mContentLength;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public MediaType contentType() {
        return mRequestBody.contentType();
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        ByteSink byteSink = new ByteSink(sink);
        BufferedSink mBufferedSink = Okio.buffer(byteSink);
        mRequestBody.writeTo(mBufferedSink);
        mBufferedSink.flush();
    }


    private final class ByteSink extends ForwardingSink {
        // uploaded length
        private long mByteLength = 0L;

        ByteSink(Sink delegate) {
            super(delegate);
        }

        @Override
        public void write(Buffer source, long byteCount) throws IOException {
            super.write(source, byteCount);
            mByteLength += byteCount;
            mLoadingListener.onProgress(mByteLength, contentLength());
        }
    }

    public interface LoadingListener {
        void onProgress(long currentLength, long contentLength);
    }
}
