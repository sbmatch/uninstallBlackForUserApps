package com.ma.uninstallBlack.service;

import static com.ma.uninstallBlack.service.MyIPCService.JOB2_ID;

import android.annotation.SuppressLint;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.Utils;
import com.ma.uninstallBlack.receiver.MyBroadcastReceiver;
import com.ma.uninstallBlack.util.OtherUtils;

import java.util.Date;

@SuppressLint("SpecifyJobSchedulerIdRange")
public class MyJobService extends JobService{
    static final String LOG_TAG = MyJobService.class.getSimpleName();
    public static Intent ipcService = new Intent(Utils.getApp().getBaseContext(), MyIPCService.class);;

    public MyJobService() {
    }

    private final Handler handler = new android.os.Handler(new android.os.Handler.Callback() {
        @SuppressLint("SimpleDateFormat")
        @Override
        public boolean handleMessage(@NonNull Message msg) {

            //Toast.makeText(MyJobService.this, "游戏模式已启动", Toast.LENGTH_SHORT).show();

            JobParameters parameters = (JobParameters) msg.obj;

            //Log.i(LOG_TAG,"jobId: "+parameters.getJobId());

            if (parameters.getJobId() == JOB2_ID){
                if (!OtherUtils.isServiceRunning(MyIPCService.class.getName())){
                    Log.w(LOG_TAG,"正在启动"+MyIPCService.class.getName()+" 时间："+ new SimpleDateFormat("HH:mm:ss").format(new Date().getTime()));
                    try{
                        startService(ipcService);
                    }catch (Throwable e){
                        e.fillInStackTrace();
                    }
                }
            }

            jobFinished(parameters,true);

            return true;
        }
    });

    @Override
    public boolean onStartJob(JobParameters params) {

        Message message = Message.obtain();
        message.obj = params;
        handler.sendMessage(message);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        handler.removeCallbacksAndMessages(null);
        return false;
    }

}