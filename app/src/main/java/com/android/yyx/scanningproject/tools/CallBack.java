package com.android.yyx.scanningproject.tools;

import java.util.List;

/**
 * Created by yeyuanxiang on 2017/10/18.
 */

public interface CallBack {
    public void exitCallBack();
    public void saveCallBack(List<String> list);
    public void callBackResult(boolean isInOut);
    public void resetCallBack();
}
