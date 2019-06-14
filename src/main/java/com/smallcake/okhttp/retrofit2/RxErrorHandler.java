package com.smallcake.okhttp.retrofit2;

import android.util.Log;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.functions.Consumer;

public class RxErrorHandler {
    /**
     * 在io线程进行订阅,在main主线程处理结果
     *
     * @return <T> ObservableTransformer<T, T>
     */
    public static <T> ObservableTransformer<T, T> netErrorHandle() {
        return new ObservableTransformer<T, T>() {
            @Override
            public ObservableSource<T> apply(Observable<T> upstream) {
                return upstream.doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.e("smallokhttp3>>Err",throwable.getMessage());
                    }
                });
            }
        };
    }
}
