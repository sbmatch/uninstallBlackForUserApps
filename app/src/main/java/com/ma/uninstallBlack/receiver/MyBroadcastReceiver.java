package com.ma.uninstallBlack.receiver;

import static android.content.Context.MODE_PRIVATE;
import static com.ma.uninstallBlack.MainActivity.sp;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ma.uninstallBlack.MainActivity;
import com.ma.uninstallBlack.service.MyWorkService;
import com.ma.uninstallBlack.util.OtherUtils;

public class MyBroadcastReceiver extends BroadcastReceiver {


    public static final String LOG_TAG = "MyBroadcastReceiver";

    private BroadcastMsg broadcastMsg = null;

    @SuppressLint({"SimpleDateFormat"})
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.

        Log.w(LOG_TAG,"有新广播了 "+intent);

        switch (intent.getAction()){
            case Intent.ACTION_PACKAGE_REMOVED:
                Log.w(LOG_TAG,"有软件包被卸载了 "+intent);
                break;
            case Intent.ACTION_PACKAGE_ADDED:
                Log.i(LOG_TAG,"有软件包被安装了 "+intent);
                break;
            case "com.ma.lockscreen.receiver":
                break;
        }

        if (!intent.getAction().isEmpty()){
            if (MainActivity.editor == null){
                sp = context.getSharedPreferences("StartSate",MODE_PRIVATE);
                MainActivity.editor = sp.edit();
            }
            if (!sp.getBoolean("isRegisterReceiver",false)){
                MainActivity.editor.putBoolean("isRegisterReceiver",true).commit();
            }
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