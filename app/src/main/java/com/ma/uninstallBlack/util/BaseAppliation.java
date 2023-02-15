package com.ma.uninstallBlack.util;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import static com.ma.uninstallBlack.service.MyIPCService.JOB2_ID;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import androidx.core.app.NotificationManagerCompat;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.NotificationUtils;
import com.blankj.utilcode.util.Utils;
import com.google.firebase.FirebaseApp;
import com.ma.uninstallBlack.BuildConfig;
import com.ma.uninstallBlack.MainActivity;
import com.ma.uninstallBlack.activity.DialogActivity;
import com.ma.uninstallBlack.receiver.MyBroadcastReceiver;
import com.ma.uninstallBlack.service.MyJobService;
import com.ma.uninstallBlack.service.MyWorkService;

import org.lsposed.hiddenapibypass.HiddenApiBypass;

import java.lang.reflect.Method;

@SuppressLint("DiscouragedPrivateApi")
public class BaseAppliation extends Application{

    public static JobScheduler jobScheduler;
    public static final int InitTime = (int) SystemClock.uptimeMillis();
    static final String LOG_TAG = BaseAppliation.class.getSimpleName();
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            HiddenApiBypass.addHiddenApiExemptions("");
        }

        Utils.init(this);
        Intent intent = new Intent("com.ma.lockscreen.receiver");
        intent.setComponent(new ComponentName(getPackageName(), MyBroadcastReceiver.class.getName()));
        //sendBroadcast(intent);

        JobInfo jobInfo = new JobInfo.Builder(JOB2_ID,new ComponentName(BuildConfig.APPLICATION_ID, MyJobService.class.getName()))
                .setPersisted(true)
                .setPeriodic(JobInfo.getMinPeriodMillis())
                .build();

        jobScheduler = (JobScheduler) this.getBaseContext().getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(jobInfo);

        StringBuilder ss = new StringBuilder();

        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {

            for (StackTraceElement s: t.getStackTrace()){
                if (s.toString().contains("com.ma.uninstall")){
                    ss.append(s).append("\n");
                }
            }
            //startActivity(new Intent(this, DialogActivity.class).setFlags(FLAG_ACTIVITY_NEW_TASK).setAction(e.fillInStackTrace().toString()));
        });

    }
}
