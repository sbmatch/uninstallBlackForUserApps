package com.ma.uninstallBlack.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.blankj.utilcode.util.ActivityUtils;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.ma.uninstallBlack.MainActivity;
import com.ma.uninstallBlack.R;
import com.ma.uninstallBlack.service.MyWorkService;
import com.ma.uninstallBlack.util.OtherUtils;

import java.util.Timer;
import java.util.TimerTask;

@SuppressLint("CustomSplashScreen")
public class SplashScreenActivity extends AppCompatActivity {
    SharedPreferences sp;
    SharedPreferences.Editor editor;
    Lifecycle lifecycle = getLifecycle();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        if (editor == null) {
            sp = getSharedPreferences("StartSate", MODE_PRIVATE);
            editor = sp.edit();
        }

//        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
//
//        splashScreen.setOnExitAnimationListener(splashScreenViewProvider -> {
//            editor.putBoolean("启动画面是否显示完成",true).commit();
//            startMainActivity();
//        });

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(null);
            actionBar.setTitle(null);
        }
        //lifecycle.addObserver(defaultLifecycleObserver);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mysplashview);

        startMainActivity();
    }


//    public  DefaultLifecycleObserver defaultLifecycleObserver = new DefaultLifecycleObserver() {
//
//        @Override
//        public void onDestroy(@NonNull LifecycleOwner owner) {
//            lifecycle.removeObserver(defaultLifecycleObserver);
//            DefaultLifecycleObserver.super.onDestroy(owner);
//        }
//    };

    public void startMainActivity() {
        Timer timer = new Timer();
        TimerTask task1 = new TimerTask() {
            @SuppressLint("SimpleDateFormat")
            @Override
            public void run() {
                if (!sp.getBoolean("启动画面是否显示完成", false)) {
                    startActivity(new Intent(SplashScreenActivity.this,MainActivity.class));
                    editor.putBoolean("启动画面是否显示完成",false).commit();
                    SplashScreenActivity.this.finish();
                }
            }
        };
        timer.schedule(task1, 1000L);
    }

}
