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

import java.util.Arrays;
import java.util.List;

/**
 * Created by yeyuanxiang on 2017/10/18.
 */

public class MainFragment extends Fragment {

    public CallBack callBack;
    private TextView userName;
    private TextView userNumber;
    private TextView title;

    public TextView carNum;
    public TextView drive;
    public TextView textView1;
    public TextView textView2;
    public TextView textView3;
    public TextView textView4;
    public EditText textView5;

    public TextView scanBtn;
    public TextView saveBtn;
    public TextView exitBtn;
    public RadioGroup radioGroup;
    public Boolean isInOrOut = true;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d("输出","onAttach");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("输出","onCreate");

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        Log.d("输出","onCreateView");
        View view = inflater.inflate(R.layout.mainfragment,container,false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        Log.d("输出","onActivityCreated");
        super.onActivityCreated(savedInstanceState);

        initView();

        String str = getArguments().getString("user");
        if (str != null){
            String[] list = str.split(",");
            userName.setText(list[1]);
            userNumber.setText(list[0]);
            title.setText("车辆出入登记");
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


    public void initTextView(List<String> list){
//        Log.d("输出",s);
//        if (s == null) return;;
//        initNullText();
//        String[] list = s.split(";");
        for (int i = 0; i < list.size(); i++) {
            switch (i) {
                case 0:
                    String[] ss = list.get(0).split(";");
                    String[] sss = ss[0].split(":");
                    if (sss.length > 1){
                        carNum.setText(ss[1]);
                    }else {
                        carNum.setText("无车辆信息记录!");
                    }
                    drive.setText(ss[1]);
                    break;
                case 1:
                    textView1.setText(list.get(1));
                    break;
                case 2:
                    textView2.setText(list.get(2));
                    break;
                case 3:
                    textView3.setText(list.get(3));
                    break;
                case 4:
                    textView4.setText(list.get(4));
                    break;
                case 5:
//                    textView4.setText(list.get(5));
                    break;
                default:
                    Log.d("输出", "default");
                    break;
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
