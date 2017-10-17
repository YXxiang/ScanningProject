package com.android.yyx.scanningproject.network;

/**
 * Created by yeyuanxiang on 2017/9/12.
 */

public class HostServiceApi {

    //Host
    private static final String Release_HOST = "http://183.63.5.27:9008/IpadService.asmx/";

    private static final String Debug_HOST = "http://183.63.5.27:9008/IpadService.asmx/";

    public static String getDebug_HOST() {
        return Debug_HOST;
    }

    public static String getRelease_HOST() {
        return Release_HOST;
    }
}
