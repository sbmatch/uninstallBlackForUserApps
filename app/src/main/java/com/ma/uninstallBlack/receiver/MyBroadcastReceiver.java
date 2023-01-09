package com.ma.uninstallBlack.receiver;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ma.uninstallBlack.service.MyWorkService;
import com.ma.uninstallBlack.util.OtherUtils;

public class MyBroadcastReceiver extends BroadcastReceiver {


    static final String LOG_TAG = MyBroadcastReceiver.class.getSimpleName();

    private BroadcastMsg broadcastMsg = null;

    @SuppressLint({"UnsafeProtectedBroadcastReceiver", "SimpleDateFormat"})
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.

        if (intent.getAction().equals("com.ma.lockscreen.receiver")){

        }

        try{
            if (broadcastMsg != null ){
                //Log.i(LOG_TAG,intent.getAction());
                broadcastMsg.sendBroadcastMsg(OtherUtils.isServiceRunning(context, MyWorkService.class.getName())+"");
            }
        }catch (Exception e){
            Log.e(LOG_TAG,e.getMessage());
        }



    }

    public interface BroadcastMsg{
        void sendBroadcastMsg(String msg);
    }

    public void setCallback(BroadcastMsg sendMsg){
        this.broadcastMsg= sendMsg;
    }

}