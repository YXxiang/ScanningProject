package com.android.yyx.scanningproject.fragment;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.yyx.scanningproject.R;
import com.android.yyx.scanningproject.activity.OperationActivity;
import com.android.yyx.scanningproject.base.MyApplication;
import com.android.yyx.scanningproject.tools.CallBack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by yeyuanxiang on 2017/10/18.
 */

public class MainFragment extends Fragment {

    public CallBack callBack;
    public TextView userName;
    public TextView userNumber;
    public TextView title;

    public TextView carNum;
    public TextView drive;
    public TextView textView1;
    public TextView textView2;
    public TextView textView3;
    public TextView textView4;
    public EditText textView5;
    public EditText textView6;

    public TextView scanBtn;
    public TextView saveBtn;
    public TextView exitBtn;
    public RadioGroup radioGroup;
    public Boolean isInOrOut = true;
    public String flag = "";
    private List<TextView> textViews = new ArrayList<>();


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.mainfragment,container,false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initView();

        String str = getArguments().getString("user");
        if (str != null){
            String[] list = str.split(",");
            userName.setText(list[1]);
            userNumber.setText(list[0]);
            title.setText("車輛出入登記");
        }
    }

    private void initView(){

        userName = getActivity().findViewById(R.id.username);
        userNumber = getActivity().findViewById(R.id.number);
        title = getActivity().findViewById(R.id.toolbar_title);


        saveBtn = getActivity().findViewById(R.id.save_btn);
        exitBtn = getActivity().findViewById(R.id.cancle_btn);
        scanBtn = getActivity().findViewById(R.id.scan_btn);
        radioGroup = getActivity().findViewById(R.id.radioGroup);


        carNum = getActivity().findViewById(R.id.carNum);
        drive = getActivity().findViewById(R.id.drive);
        textView1 = getActivity().findViewById(R.id.textView1);
        textView2 = getActivity().findViewById(R.id.textView2);
        textView3 = getActivity().findViewById(R.id.textView3);
        textView4 = getActivity().findViewById(R.id.textView4);
        textView5 =  getActivity().findViewById(R.id.textView5);
        textView6 =  getActivity().findViewById(R.id.textView6);

//        textViews.add(carNum);
        textViews.add(drive);
        textViews.add(textView1);
        textViews.add(textView2);
        textViews.add(textView3);
        textViews.add(textView4);


        initNullText();


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
                        break;
                }

                callBack.callBackResult(isInOrOut);
            }
        });



        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] ss = {"1","2"};
                List list = Arrays.asList(ss);
                callBack.saveCallBack(list);
            }
        });

        exitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callBack.exitCallBack();
            }
        });

        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callBack.resetCallBack();
            }
        });


    }


    public void initTextView(String dataString){
        Log.d("输出","responseBody = "+dataString);
        String[] datas = dataString.split(";");
        String s1 = datas[0];   //标志位P：私家家  C：公司车
        String s2 = datas[1];   //车牌号
        String s3 = datas[2];   //司机信息

        String[] ss = s2.split(":");
        String numStr = ss.length > 1 ? ss[1] : "無車輛信息記錄!";

        if (s1.equals("C") && !flag.equals("C") && carNum.getText().toString().isEmpty()){
            flag = "C";
            carNum.setText(numStr);

        }else if (s1.equals("P")){
            if (carNum.getText().toString().isEmpty()){
                carNum.setText(numStr);
            }
            for (TextView tt : textViews){
                if (tt.getText().toString().isEmpty()){
                    tt.setText(s3);
                    break;
                }
            }

        }


    }


    public void initNullText(){
        carNum.setText(null);
        drive.setText(null);
        textView1.setText(null);
        textView2.setText(null);
        textView3.setText(null);
        textView4.setText(null);
        textView5.setText(null);
        textView6.setText(null);
        flag = "";

    }


    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void onPause() {
        super.onPause();
    }

}
