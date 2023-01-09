package com.ma.uninstallBlack.service;

import static com.ma.uninstallBlack.service.MyIPCService.mReceiver;

import android.annotation.SuppressLint;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.ma.uninstallBlack.receiver.MyBroadcastReceiver;
import com.ma.uninstallBlack.util.BaseAppliation;
import com.ma.uninstallBlack.util.OtherUtils;

import java.util.Date;

@SuppressLint("SpecifyJobSchedulerIdRange")
public class MyJobService extends JobService implements MyBroadcastReceiver.BroadcastMsg{


    static final String LOG_TAG = MyJobService.class.getSimpleName();
    public static Intent i , ipcService;

    public MyJobService() {
    }

    private Handler handler = new android.os.Handler(new android.os.Handler.Callback() {
        @SuppressLint("SimpleDateFormat")
        @Override
        public boolean handleMessage(@NonNull Message msg) {

            //Toast.makeText(MyJobService.this, "游戏模式已启动", Toast.LENGTH_SHORT).show();

            JobParameters parameters = (JobParameters) msg.obj;

            //Log.i(LOG_TAG,"jobId: "+parameters.getJobId());

            if (parameters.getJobId() == BaseAppliation.JOB2_ID){
                if (!OtherUtils.isServiceRunning(getBaseContext(),MyIPCService.class.getName())){
                    Log.w(LOG_TAG,"正在启动"+MyIPCService.class.getName()+" 时间："+ new SimpleDateFormat("HH:mm:ss").format(new Date().getTime()));
                    startForegroundService(ipcService);
                }
            }else {
                startForegroundService(i);
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

        i = new Intent(this, MyWorkService.class);
        ipcService = new Intent(this, MyIPCService.class);
        mReceiver.setCallback(this);

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        handler.removeCallbacksAndMessages(null);
        return false;
    }

    @Override
    public void sendBroadcastMsg(String msg) {

        if (!msg.equals("true")){
            Log.w(LOG_TAG,"发动 魔法卡 死者苏生 从墓地复活"+MyWorkService.class.getSimpleName());
            startForegroundService(i);
        }else {
            Log.i(LOG_TAG,MyWorkService.class.getSimpleName()+" 正在运行");
            //Log.w(LOG_TAG, String.valueOf(ActivityUtils.isActivityAlive(getApplication())));
        }
    }

}