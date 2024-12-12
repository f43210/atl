package com.example.battery.service.callback;

/**
 * 通用回调
 * Created by Saturday on 2023/10/24.
 */

public interface CommonCallback<T> {

    void handleData(T data);
}
