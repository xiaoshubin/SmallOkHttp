package com.smallcake.okhttp;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * MyApplication --  com.smallcake.okhttp
 * Created by Small Cake on  2017/8/29 15:53.
 */

public interface CallbackListener {
    void successed(JSONObject jsonObject) throws JSONException;
}
