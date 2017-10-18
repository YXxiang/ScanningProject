package com.android.yyx.scanningproject.tools;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.android.yyx.scanningproject.base.BaseActivity;
import com.android.yyx.scanningproject.network.NetWorkUtils;

/**
 * Created by yeyuanxiang on 2017/10/17.
 */

public class NetWorkStatusReceiver extends BroadcastReceiver {
    public NetWorkStatusReceiver(){

    }
    @Override
    public void onReceive(Context context, Intent intent) {
        BaseActivity.isNetWorkConnected = NetWorkUtils.isNetworkConnected(context);
        if (!BaseActivity.isNetWorkConnected){
            Toast.makeText(context, "网络异常哟~~", Toast.LENGTH_LONG).show();
        }
    }
}
