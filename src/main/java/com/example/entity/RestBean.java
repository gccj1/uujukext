package com.example.entity;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.fasterxml.jackson.databind.util.JSONPObject;

public record RestBean<T>(int status, T data, String message) {
    public static  <T> RestBean<T> success() {
     return   success(null);
    }
    public static  <T> RestBean<T> success(T data) {
        return new RestBean<>(200, data, "操作成功");
    }
    public static  <T> RestBean<T> failure() {
        return new RestBean<>(400, null, "操作失败");
    }
    public static  <T> RestBean<T> failure(int status,String message) {
        return new RestBean<>(status, null, message);
    }
    public  String toJSONString() {
        return JSONObject.toJSONString(this, JSONWriter.Feature.WriteNulls);
    }
}
