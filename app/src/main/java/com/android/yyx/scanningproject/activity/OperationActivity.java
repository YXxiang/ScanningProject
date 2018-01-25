package com.android.yyx.scanningproject.activity;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.FragmentManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.android.yyx.scanningproject.R;
import com.android.yyx.scanningproject.base.MyApplication;
import com.android.yyx.scanningproject.fragment.LoginFragment;
import com.android.yyx.scanningproject.fragment.MainFragment;
import com.android.yyx.scanningproject.network.NetWorkUtils;
import com.android.yyx.scanningproject.network.ServiceManager;
import com.android.yyx.scanningproject.nfc.MifareClassicUtils;
import com.android.yyx.scanningproject.nfc.ParseListener;
import com.android.yyx.scanningproject.nfc.Utils;
import com.android.yyx.scanningproject.tools.CallBack;
import com.android.yyx.scanningproject.tools.DeviceUuidFactory;
import com.android.yyx.scanningproject.tools.LoginCallBack;
import com.android.yyx.scanningproject.tools.ScanTools;
import com.kaopiz.kprogresshud.KProgressHUD;

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
    private LoginFragment loginFragment;
    private List<String> codeList = new ArrayList<>();
    private List<String> dataList = new ArrayList<>();
    private String deviceID = null;
    private boolean selectedNFC = true;
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;

    private KProgressHUD kProgressHUD;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operation);

        kProgressHUD = KProgressHUD.create(OperationActivity.this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("網絡加載中,請等待...")
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f);

        init();

    }


    /**
     * 初始化Login
     */
    private void init() {

        loginFragment = (LoginFragment) getFragmentManager().findFragmentById(R.id.loginfragment);
        loginFragment.loginCallBack = new LoginCallBack() {
            @Override
            public void selectedIndex(int index) {
                selectedNFC = (index == 0 ? true : false );
            }
        };

        if (DeviceUuidFactory.getSDKVersionNumber() >= 23){
            OperationActivityPermissionsDispatcher.callWithCheck(this);
        }else {
            deviceID =  DeviceUuidFactory.getIMIEStatus(this);
        }


        initNFC();

    }


    /**
     * 查看有木有NFC的功能
     */
    private void initNFC(){
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null){
            Toast.makeText(this,"不支持NFC",Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        try {
            if (!nfcAdapter.isEnabled()){
                Toast.makeText(this,"请打开NFC设置",Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        }catch (Exception e){
            finish();
            return;
        }


        pendingIntent = PendingIntent.getActivity(this,0,new Intent(this,getClass()),0);
    }

    /**
     * 登录认证成功后 replace MainFragment
     * @param str 使用者身份（工号，姓名）
     */

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

        if (nfcAdapter != null){
            nfcAdapter.enableForegroundDispatch(this,pendingIntent,null,null);
        }
        //注册广播
        initBroadcastReceiver();

    }

    /**
     * 注册扫描的广播
     */
    private void initBroadcastReceiver(){
        IntentFilter intentFilter = new IntentFilter();
        m_BroadcastName = "com.barcode.sendBroadcast";
        intentFilter.addAction(m_BroadcastName);
        registerReceiver(broadcastReceiver, intentFilter);
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        processIntent(intent);

    }

    /**
     * 获取NFC卡的信息，识别是哪种类型的卡，（MF1协议）
     * @param intent
     */
    private void processIntent(Intent intent){
        if (intent != null){
            if (nfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())){
                Tag tag = intent.getParcelableExtra(nfcAdapter.EXTRA_TAG);
                if (tag == null) return;
                String[] techList = tag.getTechList();
                byte[] tagId = tag.getId();
                boolean mf1 = false;
                for (String tech : techList){
                    if (tech.equals("android.nfc.tech.MifareClassic")){
                        mf1 = true;
                    }
                }
                if (mf1){
                    MifareClassicUtils.getInstance().readOneSectorData(OperationActivity.this,tag, new ParseListener() {
                        @Override
                        public void onParseComplete(String result) {
                            Message msg = new Message();
                            msg.obj = result;
                            msg.what = 101;
                            myHandler.sendMessage(msg);
                        }
                    });

                }
            }

        }
    }

    /**
     * 封装的MF1协议类中，数据都在子线程中，因此要回调到UI线程
     */
    @SuppressLint("HandlerLeak")
    public Handler myHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String info = (String) msg.obj;
            Log.d("输出解读ID卡号扇区信息 = ",info);
            String[] datas = info.split(",");
            String ss = datas[0];
            if (ss.length() == 0) return;
//            int leading = Utils.hexStringToAlgorism(ss.substring(0,2));
//            int traling = Utils.hexStringToAlgorism(ss.substring(2,6));

            int leading = Integer.parseInt(ss.substring(0,2),16);
            int traling = Integer.parseInt(ss.substring(2,6),16);
            String s1 = String.format("%d",leading);
            String s2 = String.format("%05d",traling);
            ss = s1 + s2;
            getNetworkWithData(ss);
        }
    };


    /**
     * 网络请求总调用
     * @param nfc_code //IC  CODE
     */
    private void getNetworkWithData(String nfc_code){
        if (nfc_code.isEmpty()){
            Toast.makeText(OperationActivity.this,"廠證或者條碼識別不成功，請重新識別!",Toast.LENGTH_SHORT).show();
            return;
        }
        //判斷數據是否是NFC掃描得到的數據，還是掃描得到的數據(0 : NFC   1  : 条码 )
        int p_rdcheck = ScanTools.getNfcDataBySelectedIndex(nfc_code);

        if (!isLogin){
            getUserInfoSucess(p_rdcheck,nfc_code);
        }else {
            getNetworkData(p_rdcheck,nfc_code);
        }


    }

    /**
     * 注销广播
     */
    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    /**
     * 实例话一个扫描条码的通知（PDA）
     */
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
                    getNetworkWithData(str);
                }
            }
        }
    };



    /**
     *************************************** 网络请求的接口
     */



    /**
     * 登录请求
     * @param p_rdcheck 0 NFC  1 条码
     * @param barCodes IC/条形码
     */
    private void getUserInfoSucess(final int p_rdcheck,final String barCodes){

        if (deviceID == null){
            deviceID = DeviceUuidFactory.getIMIEStatus(this);
        }

        String sessionid = ScanTools.getNowTime();
        Log.d("输出","选择(0:NFC,1:条码) = " + p_rdcheck + ", 设备ID = " + deviceID + ", NFC/条码 = " + barCodes + ", 时间 = " + sessionid);

        kProgressHUD.show();

        ServiceManager.getInstances()
                .configerApi()
                .getUserCheckIfo(p_rdcheck,deviceID,barCodes,sessionid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }
                    @Override
                    public void onNext(@NonNull ResponseBody responseBody) {
                        kProgressHUD.dismiss();

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
     * @param barCodes 0 NFC  1 条码
     * @param p_rdcheck IC/条形码
     * 身份识别请求，扫描司机和乘客的api
     */
    private void getNetworkData(final int p_rdcheck,final String barCodes){

        String sessionid = ScanTools.getNowTime();
        Log.d("输出","选择(0:NFC,1:条码) = " + p_rdcheck + ", NFC/条码 = " + barCodes + "，时间 = " + sessionid);
        kProgressHUD.show();
        ServiceManager.getInstances()
                .configerApi()
                .getEmpinfo(p_rdcheck,barCodes,sessionid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }
                    @Override
                    public void onNext(@NonNull ResponseBody responseBody) {
                        kProgressHUD.dismiss();
                        try {
                            String reslut = responseBody.string();
                            String s = ScanTools.getContentFromTag(reslut);
                            if (ScanTools.returnTureOrFalse(s)){
                                if (dataList.size() <= 6 && !dataList.contains(s)){
                                    dataList.add(s);
                                    if (!codeList.contains(barCodes) && codeList.size() <= 6){
                                        codeList.add(barCodes);
                                    }
                                    mainFragment.initTextView(s);
                                }


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
        String p_Mileage = mainFragment.textView6.getText().toString();
        String p_sessionid = ScanTools.getNowTime();
        String userNum = mainFragment.userNumber.getText().toString();
        String[] list = userNum.split(":");
        String p_empno = list[1];
        Log.d("输出", "p_Barcodes = " + p_Barcodes + "," +
                "p_IO = " + p_IO + "," +
                "p_MRK = " + p_MRK + "," +
                "p_Mileage = " + p_Mileage + "," +
                "p_sessionid = " + p_sessionid + "," +
                "p_empno = " + p_empno);

        if (mainFragment.textView6.getText().toString().isEmpty() && mainFragment.flag.equals("C")){
            Toast.makeText(OperationActivity.this, "注意咯:請輸入里程數!", Toast.LENGTH_SHORT).show();
            return;
        }

        kProgressHUD.show();

        ServiceManager.getInstances()
                .configerApi()
                .saveEntryDataInfoCodes(p_Barcodes, p_IO, p_MRK,p_Mileage, p_sessionid,p_empno)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull ResponseBody responseBody) {
                        kProgressHUD.dismiss();
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






    /**
     * *************************动态申请权限
     */
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
