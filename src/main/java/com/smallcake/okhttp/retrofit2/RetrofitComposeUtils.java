package com.smallcake.okhttp.retrofit2;

import io.reactivex.Observable;

public class RetrofitComposeUtils {
    public static  <T> Observable<T> bindIoUI(Observable<T> observable) {
        return observable.compose(RxErrorHandler.<T>netErrorHandle()).compose(RxSchedulers.<T>ioMain());
    }
}
