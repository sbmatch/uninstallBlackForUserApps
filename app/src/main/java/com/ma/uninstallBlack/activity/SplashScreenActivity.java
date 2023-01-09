package com.ma.uninstallBlack.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.splashscreen.SplashScreen;

import com.ma.uninstallBlack.MainActivity;
import com.ma.uninstallBlack.R;

public class SplashScreenActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        SplashScreen splashScreen = SplashScreen.installSplashScreen(SplashScreenActivity.this);
        //splashScreen.setKeepOnScreenCondition(() -> true);
        splashScreen.setOnExitAnimationListener(splashScreenViewProvider -> {
            splashScreenViewProvider.remove();
            startActivity(new Intent(SplashScreenActivity.this,MainActivity.class));
            SplashScreenActivity.this.finishAffinity();
            //overridePendingTransition(0, R.anim.shrink_fade_out_from_bottom);
        });

        super.onCreate(savedInstanceState);
    }
}
