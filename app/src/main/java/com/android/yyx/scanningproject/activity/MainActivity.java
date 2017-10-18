package com.android.yyx.scanningproject.activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.IdRes;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.yyx.scanningproject.R;
import com.android.yyx.scanningproject.base.BaseActivity;
import com.android.yyx.scanningproject.base.MyApplication;
import com.android.yyx.scanningproject.network.ServiceManager;
import com.android.yyx.scanningproject.tools.NetWorkStatusReceiver;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;


import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;


public class MainActivity extends BaseActivity implements View.OnClickListener {
    private TextView scanBtn;
    private TextView carNum;
    private TextView drive;
    private TextView textView1;
    private TextView textView2;
    private TextView textView3;
    private TextView textView4;
    private EditText textView5;
    private RadioGroup radioGroup;
    //1 : in  |  0 : out
    private Boolean isInOrOut = true;
    private Boolean login = false;
    private List<String> CODELIST = new ArrayList<String>();
    private List<String> reslutCodes = new ArrayList<String>();
    private TextView saveBtn;
    private TextView exitBtn;
    private NetWorkStatusReceiver netWorkStatusReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setToolBarTitle("车辆信息登记");
        setUsername("");
        setNumber("");
        init();
        gotoOne_CodeCamera();
        initRegisterReceiver();
    }

    private void initRegisterReceiver(){
        netWorkStatusReceiver = new NetWorkStatusReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(netWorkStatusReceiver,intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(netWorkStatusReceiver);
    }

    private void init(){

        carNum = (TextView) findViewById(R.id.carNum);
        drive = (TextView) findViewById(R.id.drive);
        textView1 = (TextView) findViewById(R.id.textView1);
        textView2 = (TextView) findViewById(R.id.textView2);
        textView3 = (TextView) findViewById(R.id.textView3);
        textView4 = (TextView) findViewById(R.id.textView4);
        textView5 = (EditText) findViewById(R.id.textView5);
        saveBtn = (TextView) findViewById(R.id.save_btn);
        exitBtn = (TextView) findViewById(R.id.cancle_btn);
        scanBtn = (TextView) findViewById(R.id.scan_btn);

        //
        resultBtnEntable();

        saveBtn.setOnClickListener(this);
        exitBtn.setOnClickListener(this);
        scanBtn.setOnClickListener(this);
        initRadioGroup();
        initNullText();

    }

    private void resultBtnEntable(){
        saveBtn.setEnabled(login);
        exitBtn.setEnabled(login);
        scanBtn.setEnabled(login);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.save_btn :
            {
                if (this.CODELIST.size() > 0){
                    saveEmtryDataToNetwork();
                }else {
                    Toast.makeText(MainActivity.this, "数据有误，保存失败!", Toast.LENGTH_SHORT).show();
                    Log.d("输出","数据有误，保存失败!");
                }
            }
                break;
            case R.id.cancle_btn :
            {
                MyApplication.appExit();
            }
                break;

            case R.id.scan_btn :
            {
                //跳转相机
                gotoOne_CodeCamera();
            }
            break;

            default:
                break;
        }
    }

    private void initRadioGroup(){
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
//        radioGroup.check(isInOrOut ? R.id.btn_in : R.id.btn_out);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                switch (i){
                    case R.id.btn_in:
                        Log.d("输出","in");
                        isInOrOut = true;
                        break;
                    case R.id.btn_out:
                        Log.d("输出","out");
                        isInOrOut = false;
                        break;
                    default:
                        Log.d("输出","default");
                        break;
                }
            }
        });
    }

    private void initNullText(){
        carNum.setText(null);
        drive.setText(null);
        textView1.setText(null);
        textView2.setText(null);
        textView3.setText(null);
        textView4.setText(null);
        textView5.setText(null);

    }



    private void initTextView(String s){
        Log.d("输出",s);
        if (s == null) return;;
        initNullText();
        String[] list = s.split(";");
//        List<String> strList = Arrays.asList(list);
        for (int i = 0; i < list.length; i++) {
            switch (i) {
                case 0:
                    String[] ss = list[0].split(":");
                    if (ss.length > 1){
                        carNum.setText(ss[1]);
                    }else {
                        carNum.setText("无车辆信息记录!");
                    }
                    break;
                case 1:
                    drive.setText(list[1]);
                    break;
                case 2:
                    textView1.setText(list[2]);
                    break;
                case 3:
                    textView2.setText(list[3]);
                    break;
                case 4:
                    textView3.setText(list[4]);
                    break;
                case 5:
                    textView4.setText(list[5]);
                    break;
                default:
                    Log.d("输出", "default");
                    break;
            }
        }

    }

    /**
     * 解析XML数据
     * @param result  源数据（XML转String）
     * @return        String 字符串
     */
    private String getContentFromTag(String result){
        String tag1 = "<string xmlns=\"http://tempuri.org/\">";
        String tag2 = "</string>";
        String s = result.substring(result.indexOf(tag1)+tag1.length(),result.indexOf(tag2));
        return s;
    }

    /**
     *
     * @param resultString  分割的字符串
     * @param pattern   分割的字符
     * @return   数组
     */
    private String[] retultList(String resultString, String pattern) {
        Pattern pen = Pattern.compile(pattern);
        String[] temp = pen.split(resultString);
        return  temp;
    }

    //判断数据是否成功（true） or 失败（false）
    private Boolean returnTureOrFalse(String s){
        if (s == null) return false;
        String str = s.substring(0,1);
        if (str.equals("-")){
            return false;
        }else {
            return true;
        }
    }

    /**
     * 获取barcode
     * @return String
     */
    private String getBarCodes(){
        Log.d("输出",CODELIST.size()+"");
        if (CODELIST == null) return null;
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < CODELIST.size(); i++){
            if (i == 0){
                stringBuilder.append(CODELIST.get(i));
            }else {
                stringBuilder.append(","+CODELIST.get(i));
            }
        }

        return stringBuilder.toString();
    }

    /**
     * 获取当前的时间
     * @return
     */
    private String getNowTime(){
        Date date = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("yyyyMMddHHmmss");
        String p_sessionid = ft.format(date);
        return p_sessionid;
    }




    /**
     *
     * 跳转到扫描条形码界面
     */
    private void gotoOne_CodeCamera(){
        IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
        //ALL_CODE_TYPES(全部类型)、QR_CODE_TYPES(二维码)、ONE_D_CODE_TYPES(一维码)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt("");
        integrator.setCameraId(0);  // Use a specific camera of the device
        integrator.setBeepEnabled(true);
        integrator.initiateScan();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult r = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        Log.d("输出"," requestCode="+requestCode+" resultCode="+resultCode);
        if(r != null) {
            String s = r.getContents();
            if(s == null) {
                if (reslutCodes.size() > 0){
                    CODELIST = new ArrayList<String>();
                    for (String ss : reslutCodes){
                        CODELIST.add(ss);
                    }
                    //网络请求
                    getNetworkData();
                    reslutCodes.clear();
                }else {
                    if (login){
                        Toast.makeText(MainActivity.this, "扫描取消", Toast.LENGTH_SHORT).show();
                    }else {
                        finish();
                        MyApplication.appExit();
                    }

                }
            } else {//扫描成功
                if (login){
                    reslutCodes.add(s);
                    gotoOne_CodeCamera();
                }else {
                    getUserInfoSucess(s);
                    login = true;
                }
            }
        } else {
            Toast.makeText(MainActivity.this, "扫码失败", Toast.LENGTH_SHORT).show();
            super.onActivityResult(requestCode, resultCode, data);
        }

    }


    private void getUserInfoSucess(String barCodes){
//        barCodes = "4411490";
        String sessionid = getNowTime();
        Log.d("输出","条码 = " + barCodes + "，时间 = " + sessionid);
        ServiceManager.getInstances()
                .configerApi()
                .getUserCheckIfo(barCodes,sessionid)
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
                            String s = getContentFromTag(reslut);
                            Log.d("输出","responseBody = "+s);
                            if (returnTureOrFalse(s)){
                                resultBtnEntable();
                                Toast.makeText(MainActivity.this, "身份认证成功,欢迎使用!", Toast.LENGTH_LONG).show();
                                String[] list = s.split(",");
                                setUsername(list[1]);
                                setNumber(list[0]);

                            }else {
                                Toast.makeText(MainActivity.this, s, Toast.LENGTH_LONG).show();
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        finish();
                                        MyApplication.appExit();
                                    }
                                },3000);
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


    //网络请求
    private void getNetworkData(){
        String barCodes = getBarCodes();
        String sessionid = getNowTime();
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
                            String s = getContentFromTag(reslut);
                            Log.d("输出","responseBody = "+s);
                            if (returnTureOrFalse(s)){
                                initTextView(s);
                            }else {
                                Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT).show();
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



    private void saveEmtryDataToNetwork() {
        String p_Barcodes = getBarCodes();
        String p_IO = (isInOrOut ? "I" : "O");
        String p_MRK = textView5.getText().toString();
        String p_sessionid = getNowTime();

        Log.d("输出", "p_Barcodes = " + p_Barcodes + "," +
                "p_IO = " + p_IO + "," +
                "p_MRK = " + p_MRK + "," +
                "p_sessionid = " + p_sessionid);
        ServiceManager.getInstances()
                .configerApi()
                .saveEntryDataInfoCodes(p_Barcodes, p_IO, p_MRK, p_sessionid)
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
                            String s = getContentFromTag(results);
                            Log.d("输出",s);
                            String toastText = "";
                            if (returnTureOrFalse(s)){
                                toastText = "保存成功!";
                                removeData();

                            }else {
                                toastText = "保存失败!";
                            }
                            Toast.makeText(MainActivity.this, toastText, Toast.LENGTH_SHORT).show();

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


    private void removeData(){
        initNullText();
        CODELIST.clear();
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (isShouldHideKeyboard(v, ev)) {
                hideKeyboard(v.getWindowToken());
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 根据EditText所在坐标和用户点击的坐标相对比，来判断是否隐藏键盘，因为当用户点击EditText时则不能隐藏
     *
     * @param v
     * @param event
     * @return
     */
    private boolean isShouldHideKeyboard(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] l = {0, 0};
            v.getLocationInWindow(l);
            int left = l[0],
                    top = l[1],
                    bottom = top + v.getHeight(),
                    right = left + v.getWidth();
            if (event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom) {
                // 点击EditText的事件，忽略它。
                return false;
            } else {
                return true;
            }
        }
        // 如果焦点不是EditText则忽略，这个发生在视图刚绘制完，第一个焦点不在EditText上，和用户用轨迹球选择其他的焦点
        return false;
    }

    /**
     * 获取InputMethodManager，隐藏软键盘
     * @param token
     */
    private void hideKeyboard(IBinder token) {
        if (token != null) {
            InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            im.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

}





