package com.android.yyx.scanningproject.base;


import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.yyx.scanningproject.R;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;

/**
 * Created by yeyuanxiang on 2017/9/12.
 */

public class BaseActivity extends RxAppCompatActivity {

    //布局
    private RelativeLayout mToolbar;
    //导航栏的标题
    private TextView toolBarTitle;

    private TextView username;
    private TextView number;

    public static boolean isNetWorkConnected;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.toolbar);
        init();
    }

    private void init(){
        mToolbar = (RelativeLayout) findViewById(R.id.base_toolbar);
        toolBarTitle =  (TextView) findViewById(R.id.toolbar_title);
        username = (TextView) findViewById(R.id.username);
        number = (TextView) findViewById(R.id.number);
    }

    @CallSuper
    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        View activityView= LayoutInflater.from(this).inflate(layoutResID,null,false);
        ViewGroup viewGroup= (ViewGroup) mToolbar.getParent();
        viewGroup.addView(activityView);
        //空出边距给toolbar
        FrameLayout.LayoutParams lp= (FrameLayout.LayoutParams) activityView.getLayoutParams();
        lp.setMargins(0,(int)this.getResources().getDimension(R.dimen.toolbar_height),0,0);
    }


    @CallSuper
    @Override
    public void setContentView(View view) {
        ViewGroup viewGroup= (ViewGroup) mToolbar.getParent();
        viewGroup.addView(view);
        FrameLayout.LayoutParams lp= (FrameLayout.LayoutParams) view.getLayoutParams();
        lp.setMargins(0,(int)this.getResources().getDimension(R.dimen.toolbar_height),0,0);
    }
    public void setToolBarTitle(String toolBarTitle) {
        this.toolBarTitle.setText(toolBarTitle);
    }
    public void setUsername(String username) {
        this.username.setText(username);
    }
    public void setNumber(String number) {
        this.number.setText(number);
    }
    public void goneToolBar(){
        mToolbar.setVisibility(View.GONE);
    }

}
