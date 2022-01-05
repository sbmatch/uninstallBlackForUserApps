package com.ma.powersoundswitch.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.FragmentUtils;
import com.huawei.agconnect.AGConnectApp;
import com.huawei.agconnect.AGConnectInstance;
import com.huawei.agconnect.crash.AGConnectCrash;
import com.ma.powersoundswitch.R;
import com.ma.powersoundswitch.fragment.SettingFragment;
import com.ma.powersoundswitch.mViewModel;
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener {
    private FragmentManager fm;
    private FragmentTransaction transition;
    //private long exitTime;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        AppCenter.start(getApplication(), "b5f71581-37c7-42a2-b631-45a8a56a17df", Analytics.class, Crashes.class);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle("设置");

        fm = getSupportFragmentManager();
        transition = fm.beginTransaction();

        FragmentUtils.add(fm,new SettingFragment(),R.id.fragmentContainerView);
        FragmentUtils.show(fm);

        mViewModel viewModel = new ViewModelProvider(this).get(mViewModel.class);
        viewModel.getCallString().observe(this, s -> {
             //Ops检查权限状态(AppOpsManager.permissionToOp(Manifest.permission.WRITE_SECURE_SETTINGS));
        });

    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        startActivity(new Intent().setComponent(new ComponentName("com.miui.home","com.miui.home.launcher.Launcher")));
        //ActivityUtils.startHomeActivity();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                    AppUtils.exitApp();
                    System.exit(0);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        return super.onKeyDown(keyCode, event);

    }
}
