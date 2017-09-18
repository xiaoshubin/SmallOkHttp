package com.smallcake.okhttp.retrofit2;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Converter;

/**
 * MyApplication --  com.smallcake.okhttp.retrofit2
 * Created by Small Cake on  2017/9/18 11:40.
 */

public class StringResponseBodyConverter implements Converter<ResponseBody, String> {
    @Override
    public String convert(ResponseBody value) throws IOException {
        try {
            return value.string();
        } finally {
            value.close();
        }
    }
}
