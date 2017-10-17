package com.android.yyx.scanningproject.network;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by yeyuanxiang on 2017/9/4.
 */

public class ServiceManager {
    private static ServiceManager instances = null;

    private ServiceManager(){

    }

    public static ServiceManager getInstances(){
        if (instances == null){
            instances = new ServiceManager();
        }

        return instances;
    }


    public ApiService configerApi(){

        //设置
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10,TimeUnit.SECONDS)
                .build();


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(HostServiceApi.getRelease_HOST())
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        return apiService;
    }


}
