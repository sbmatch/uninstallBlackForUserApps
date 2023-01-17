package com.ma.uninstallBlack.util;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

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

import com.blankj.utilcode.util.Utils;
import com.google.firebase.FirebaseApp;
import com.ma.uninstallBlack.activity.DialogActivity;
import com.ma.uninstallBlack.receiver.MyBroadcastReceiver;
import com.ma.uninstallBlack.service.MyJobService;

import org.lsposed.hiddenapibypass.HiddenApiBypass;

@SuppressLint("DiscouragedPrivateApi")
public class BaseAppliation extends Application {
    public static final int JOB2_ID = 100000;
    public static final int InitTime = (int) SystemClock.uptimeMillis();
    public static JobScheduler jobScheduler = null;
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

        JobInfo info = new JobInfo.Builder(JOB2_ID,new ComponentName(getPackageName(), MyJobService.class.getName()))
                .setPersisted(true) //重启后任务继续执行
                .setPeriodic(JobInfo.getMinPeriodMillis())
                .build();

        jobScheduler = (JobScheduler) this.getBaseContext().getSystemService(Context.JOB_SCHEDULER_SERVICE);
        try{
            jobScheduler.schedule(info);
        }catch (RuntimeException e){
            Log.e(LOG_TAG,e.getMessage());
        }

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

    @Override
    public void onCreate() {
        super.onCreate();
    }


}
