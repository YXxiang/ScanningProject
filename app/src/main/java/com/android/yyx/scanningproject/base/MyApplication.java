package com.android.yyx.scanningproject.base;

import android.app.Application;
import android.content.Context;
import android.os.Process;

/**
 * Created by yeyuanxiang on 2017/9/12.
 */

public class MyApplication extends Application {

    private static Context applicationContext;

    @Override
    public void onCreate() {
        super.onCreate();
        applicationContext=getApplicationContext();
    }
    public static Context getAppContext(){

        //后台挂着 applicationContext被回收
        if (applicationContext==null){
            appExit();
        }
        return  applicationContext;
    }

    public static void appExit(){
        Process.killProcess(Process.myPid());
    }

}
