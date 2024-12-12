package com.example.battery.service.impl;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.battery.http.OkHttpUtil;
import com.example.battery.http.ServerUrls;
import com.example.battery.model.RespModel;
import com.example.battery.model.UserInfoModel;
import com.example.battery.service.UserInfoService;
import com.example.battery.service.callback.CommonCallback;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Saturday on 2023/10/24.
 */

public class UserInfoServiceImpl implements UserInfoService {

    private final Gson gson = new GsonBuilder().create();

    @Override
    public void listUserProfessional(@NonNull CommonCallback<List<String>> cb) {
        try {
            OkHttpUtil.get(ServerUrls.USER_PROFESSIONAL, null, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("saturday058","[listUserProfessional.onFailure] error: " + e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    List<String> userProfessionalData = gson.fromJson(response.body().string(), new TypeToken<List<String>>(){}.getType());
                    Log.i("saturday058","userProfessionalData is: " + userProfessionalData+"");
                    if (null != userProfessionalData) {
                        cb.handleData(userProfessionalData);
                    }
                }
            });
        } catch (IOException e) {
            Log.e("saturday058","[listUserProfessional] error: " + e);
        }
    }

    @Override
    public void listPhoneUsage(@NonNull CommonCallback<List<String>> cb) {
        try {
            OkHttpUtil.get(ServerUrls.PHONE_USAGE, null, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("saturday058","[listPhoneUsage.onFailure] error: " + e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    List<String> data = gson.fromJson(response.body().string(), new TypeToken<List<String>>(){}.getType());
                    Log.i("saturday058","phoneUsage is: " + data+"");
                    if (null != data) {
                        cb.handleData(data);
                    }
                }
            });
        } catch (IOException e) {
            Log.e("saturday058","[listPhoneUsage] error: " + e);
        }
    }

    @Override
    public void listPhoneBrand(@NonNull CommonCallback<List<String>> cb) {
        try {
            OkHttpUtil.get(ServerUrls.PHONE_BRAND, null, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("saturday058","[listPhoneBrand.onFailure] error: " + e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    List<String> phoneBrand = gson.fromJson(response.body().string(), new TypeToken<List<String>>(){}.getType());
                    Log.i("saturday058","phoneBrand is: " + phoneBrand+"");
                    if (null != phoneBrand) {
                        cb.handleData(phoneBrand);
                    }
                }
            });
        } catch (IOException e) {
            Log.e("saturday058","[listPhoneBrand] error: " + e);
        }
    }

    @Override
    public void submitUserInfo(@NonNull UserInfoModel userInfoModel,  @NonNull CommonCallback<RespModel> cb) {
        OkHttpUtil.post(ServerUrls.SUBMIT_PHONE_DATA, userInfoModel, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("saturday058","[listPhoneBrand.onFailure] error: " + e);
            }

            @Override
            public void onResponse(Call call, Response response)throws IOException {
                String data = response.body().string();
//                Log.i("REQUEST", "submit response = " +data);
                if (!TextUtils.isEmpty(data) && data.contains("code")) {
                    RespModel resp = gson.fromJson(data, RespModel.class);
//                    Log.i("REQUEST", resp + "");
                    if (null != resp) {
                        cb.handleData(resp);
                    }
                }
            }
        });
    }
}
