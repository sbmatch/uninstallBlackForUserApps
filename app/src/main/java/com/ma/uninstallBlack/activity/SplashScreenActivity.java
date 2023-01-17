package com.ma.uninstallBlack.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.splashscreen.SplashScreen;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.ma.uninstallBlack.MainActivity;

import java.util.Timer;
import java.util.TimerTask;

public class SplashScreenActivity extends Activity {
    SharedPreferences sp;
    SharedPreferences.Editor editor;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        if (editor == null){
            sp = getSharedPreferences("StartSate",MODE_PRIVATE);
            editor = sp.edit();
        }
        editor.putBoolean("启动画面是否显示完成",false).commit();

        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if (sp.getBoolean("启动画面是否显示完成",false)) {

                    Log.i(SplashScreenActivity.class.getSimpleName(),"启动页显示完成");
                }else {
                    startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
                    SplashScreenActivity.this.finishAffinity();
                }
                FirebaseCrashlytics.getInstance().setCustomKey("启动页是否显示完成",sp.getBoolean("启动画面是否显示完成",false));
            }
        };
        new Timer().schedule(timerTask,1000L);

        splashScreen.setOnExitAnimationListener(splashScreenViewProvider -> {
            editor.putBoolean("启动画面是否显示完成",true).commit();
            startActivity(new Intent(this,MainActivity.class));
            SplashScreenActivity.this.finishAffinity();
        });

        super.onCreate(savedInstanceState);

    }
}
