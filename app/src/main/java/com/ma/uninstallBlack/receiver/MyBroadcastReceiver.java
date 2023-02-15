package com.ma.uninstallBlack.receiver;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.ProcessUtils;
import com.blankj.utilcode.util.ServiceUtils;
import com.blankj.utilcode.util.Utils;
import com.ma.uninstallBlack.MainActivity;
import com.ma.uninstallBlack.service.MyWorkService;
import com.ma.uninstallBlack.util.OtherUtils;

public class MyBroadcastReceiver extends BroadcastReceiver {


    public static final String LOG_TAG = "MyBroadcastReceiver";

    private BroadcastMsg broadcastMsg;
    public SharedPreferences sp;

    @SuppressLint({"SimpleDateFormat"})
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.

        if (sp == null){
            sp = context.getSharedPreferences("StartSate",MODE_PRIVATE);
        }

        switch (intent.getAction()){
            case "com.ma.lockscreen.receiver":
                if (sp.getBoolean("是否拿到Shizuku授权",false)){
                    if (!OtherUtils.isServiceRunning(MyWorkService.class.getName())){

                    }
                }
                break;
            case "com.ma.lockscreen.receiver.exit":
                broadcastMsg.sendBroadcastMsg("killUSelf");
                //ActivityUtils.finishAllActivities();
                //Log.w(LOG_TAG,"已退出");
                break;
            default:
                //Log.i(LOG_TAG,intent.getAction());

        }


    }

    public interface BroadcastMsg{
        void sendBroadcastMsg(String msg);
    }

    public void setCallback(BroadcastMsg sendMsg){
        this.broadcastMsg= sendMsg;
    }

}