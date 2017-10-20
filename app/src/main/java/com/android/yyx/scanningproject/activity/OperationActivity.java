package com.android.yyx.scanningproject.activity;
import android.Manifest;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.android.yyx.scanningproject.R;
import com.android.yyx.scanningproject.base.MyApplication;
import com.android.yyx.scanningproject.fragment.LoginFragment;
import com.android.yyx.scanningproject.fragment.MainFragment;
import com.android.yyx.scanningproject.network.NetWorkUtils;
import com.android.yyx.scanningproject.network.ServiceManager;
import com.android.yyx.scanningproject.tools.CallBack;
import com.android.yyx.scanningproject.tools.DeviceUuidFactory;
import com.android.yyx.scanningproject.tools.ScanTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;


@RuntimePermissions
public class OperationActivity extends AppCompatActivity {

    private boolean isLogin = false;
    String m_BroadcastName = null;
    private boolean isInOrOut = true;
    private MainFragment mainFragment;
    private List<String> codeList = new ArrayList<>();
    private List<String> dataList = new ArrayList<>();
    private String deviceID = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operation);

        if (DeviceUuidFactory.getSDKVersionNumber() >= 23){
            OperationActivityPermissionsDispatcher.callWithCheck(this);
        }else {
            deviceID =  DeviceUuidFactory.getIMIEStatus(this);
        }


    }

    public void replaceFragment(String str){
        mainFragment = new MainFragment();
        Bundle bundle = new Bundle();
        bundle.putString("user",str);
        mainFragment.setArguments(bundle);
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.loginfragment,mainFragment).commit();

        mainFragment.callBack = new CallBack() {
            @Override
            public void exitCallBack() {
                isLogin = false;
                MyApplication.appExit();
            }

            @Override
            public void saveCallBack(List<String> list) {

                if (codeList.size() > 0){
                    String codes = ScanTools.getBarCodes(codeList);
                    saveEmtryDataToNetwork(codes);
                }

            }

            @Override
            public void callBackResult(boolean isInOut) {
                OperationActivity.this.isInOrOut = isInOut;

            }

            @Override
            public void resetCallBack() {
                mainFragment.initNullText();
                codeList.clear();
                dataList.clear();
            }
        };

    }



    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        m_BroadcastName = "com.barcode.sendBroadcast";
        intentFilter.addAction(m_BroadcastName);
        registerReceiver(broadcastReceiver, intentFilter);
    }


    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);

    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(m_BroadcastName)) {
                String str = intent.getStringExtra("BARCODE");
                if (!NetWorkUtils.isNetworkConnected(OperationActivity.this)){
                    Toast.makeText(context, "網絡連接異常,請檢查設備網絡!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (str.isEmpty()) {
                    Toast.makeText(context, "掃描失敗，請重新掃描~~", Toast.LENGTH_SHORT).show();
                } else {
                    if (!isLogin){
                        getUserInfoSucess(str);
                    }else {
                        Log.d("输出",str);
                        getNetworkData(str);

                    }
                }
            }
        }
    };



    /**
     * 登录请求
     * @param barCodes 条形码
     */
    private void getUserInfoSucess(String barCodes){

        if (deviceID == null){
            deviceID = DeviceUuidFactory.getIMIEStatus(this);
        }

        String sessionid = ScanTools.getNowTime();
        Log.d("输出","设备ID = " + deviceID + ", 条码 = " + barCodes + ", 时间 = " + sessionid);
        ServiceManager.getInstances()
                .configerApi()
                .getUserCheckIfo(deviceID,barCodes,sessionid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }
                    @Override
                    public void onNext(@NonNull ResponseBody responseBody) {
                        try {
                            String reslut = responseBody.string();
                            String s = ScanTools.getContentFromTag(reslut);
                            Log.d("输出","responseBody = "+s);
                            if (ScanTools.returnTureOrFalse(s)){
                                isLogin = true;
                                Toast.makeText(OperationActivity.this, "身份認證成功,歡迎使用!", Toast.LENGTH_SHORT).show();
                                replaceFragment(s);

                            }else {
                                Toast.makeText(OperationActivity.this, s, Toast.LENGTH_SHORT).show();
                            }
                        }catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                    @Override
                    public void onError(@NonNull Throwable e) {

                    }
                    @Override
                    public void onComplete() {

                    }
                });

    }


    /**
     * 身份识别请求，扫描司机和乘客的api
     */
    private void getNetworkData(final String barCodes){

        String sessionid = ScanTools.getNowTime();
        Log.d("输出","条码 = " + barCodes + "，时间 = " + sessionid);
        ServiceManager.getInstances()
                .configerApi()
                .getEmpinfo(barCodes,sessionid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }
                    @Override
                    public void onNext(@NonNull ResponseBody responseBody) {
                        try {
                            String reslut = responseBody.string();
                            String s = ScanTools.getContentFromTag(reslut);
                            Log.d("输出","responseBody = "+s);
                            if (!codeList.contains(barCodes)){
                                codeList.add(barCodes);
                            }
                            if (ScanTools.returnTureOrFalse(s)){
                                if (dataList.size() > 4) return;
                                if (dataList.contains(s)) return;
                                dataList.add(dataList.size(),s);
                                mainFragment.initTextView(dataList);

                            }else {
                                Toast.makeText(OperationActivity.this, s, Toast.LENGTH_SHORT).show();

                            }
                        }catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                    @Override
                    public void onError(@NonNull Throwable e) {

                    }
                    @Override
                    public void onComplete() {

                    }
                });
    }



    /**
     * 保存的回调接口
     */
    private void saveEmtryDataToNetwork(String barCodes) {
        String p_Barcodes = barCodes;
        String p_IO = (isInOrOut ? "I" : "O");
        String p_MRK = mainFragment.textView5.getText().toString();
        String p_sessionid = ScanTools.getNowTime();
        String userNum = mainFragment.userNumber.getText().toString();
        String[] list = userNum.split(":");
        String p_empno = list[1];
        Log.d("输出", "p_Barcodes = " + p_Barcodes + "," +
                "p_IO = " + p_IO + "," +
                "p_MRK = " + p_MRK + "," +
                "p_sessionid = " + p_sessionid + "," +
                "p_empno = " + p_empno);
        ServiceManager.getInstances()
                .configerApi()
                .saveEntryDataInfoCodes(p_Barcodes, p_IO, p_MRK, p_sessionid,p_empno)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull ResponseBody responseBody) {
                        try {
                            String results = responseBody.string();
                            String s = ScanTools.getContentFromTag(results);
                            Log.d("输出",s);
                            String toastText = "";
                            if (ScanTools.returnTureOrFalse(s)){
                                toastText = "保存成功!";
                                mainFragment.initNullText();
                                codeList.clear();
                                dataList.clear();

                            }else {
                                toastText = "保存失敗!";
                            }
                            Toast.makeText(OperationActivity.this, toastText, Toast.LENGTH_SHORT).show();

                        }catch (IOException e){
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }


    @NeedsPermission(Manifest.permission.READ_PHONE_STATE)
    void call() {
        Toast.makeText(this, "获取READ_PHONE_STATE权限", Toast.LENGTH_SHORT).show();
        deviceID =  DeviceUuidFactory.getIMIEStatus(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        OperationActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }
}
