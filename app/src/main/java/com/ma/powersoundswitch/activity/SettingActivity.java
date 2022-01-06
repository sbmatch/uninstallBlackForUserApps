package com.ma.powersoundswitch.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.FragmentUtils;
import com.blankj.utilcode.util.LogUtils;
import com.ma.powersoundswitch.ContentUriUtil;
import com.ma.powersoundswitch.R;
import com.ma.powersoundswitch.fragment.SettingFragment;
import com.ma.powersoundswitch.mViewModel;
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;

import java.io.File;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener {
    private FragmentManager fm;
    private FragmentTransaction transition;
    private mViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle("设置");

        fm = getSupportFragmentManager();
        transition = fm.beginTransaction();

        FragmentUtils.add(fm,new SettingFragment(),R.id.fragmentContainerView);
        FragmentUtils.show(fm);

        viewModel = new ViewModelProvider(this).get(mViewModel.class);

    }

    @Override
    public void onClick(View v) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            viewModel.add(ContentUriUtil.getPath(this,data.getData()));
        }
        super.onActivityResult(requestCode, resultCode, data);
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
