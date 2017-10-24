package com.android.yyx.scanningproject.fragment;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.yyx.scanningproject.R;
import com.android.yyx.scanningproject.tools.CallBack;
import com.android.yyx.scanningproject.tools.LoginCallBack;

/**
 * Created by yeyuanxiang on 2017/10/18.
 */

public class LoginFragment extends Fragment {

    private TextView textView;
    private RadioGroup loginRadioGroup;
    public LoginCallBack loginCallBack;
    private int index;

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
        View view = inflater.inflate(R.layout.loginfragment,container,false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
    }


    private void initView(){
        textView = getActivity().findViewById(R.id.op_textview);
        loginRadioGroup = getActivity().findViewById(R.id.login_radioGroup);
        loginRadioGroup.setVisibility(View.INVISIBLE);
        loginRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                switch (checkedId){
                    case R.id.selected_nfc:
                        index = 0;
                        break;
                    case R.id.selected_code:
                        index = 1;
                        break;
                    default:
                        break;
                }
                loginCallBack.selectedIndex(index);
            }
        });
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
