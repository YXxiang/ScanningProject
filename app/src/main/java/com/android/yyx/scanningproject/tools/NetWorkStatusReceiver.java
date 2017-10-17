package com.android.yyx.scanningproject.tools;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;
import android.widget.Toast;

import com.android.yyx.scanningproject.base.BaseActivity;
import com.android.yyx.scanningproject.network.NetWorkUtils;

/**
 * Created by yeyuanxiang on 2017/10/17.
 */

public class NetWorkStatusReceiver extends BroadcastReceiver {

    private final String myAction = "com.android.yyx.CONNECTIVITY_CHANGE";

    public NetWorkStatusReceiver(){

    }
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(myAction)){
            BaseActivity.isNetWorkConnected = NetWorkUtils.isNetworkConnected(context);
            if (BaseActivity.isNetWorkConnected){
                Toast.makeText(context, "network changed", Toast.LENGTH_LONG).show();
            }
        }
    }
}
