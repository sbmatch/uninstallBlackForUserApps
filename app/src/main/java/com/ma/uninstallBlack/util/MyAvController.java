package com.ma.uninstallBlack.util;

import android.app.IActivityController;
import android.content.Intent;
import android.os.RemoteException;
import android.util.Log;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.ma.uninstallBlack.BuildConfig;
import com.ma.uninstallBlack.MainActivity;

public class MyAvController extends IActivityController.Stub{

    private final String Log_Tag = MyAvController.class.getSimpleName();

    @Override
    public boolean activityStarting(Intent intent, String pkg) throws RemoteException {

        //Log.w(Log_Tag,intent+" pkg: "+pkg);
//        if (pkg.equals("com.ma.lockscreen")){
//            Log.w(Log_Tag,"已禁止启动");
//
//            return false;
//        }

        return true;
    }

    @Override
    public boolean activityResuming(String pkg) throws RemoteException {
        //Log.w(Log_Tag,"activityResuming: "+pkg);

        return true;
    }


    @Override
    public boolean appCrashed(String processName, int pid, String shortMsg, String longMsg, long timeMillis, String stackTrace) throws RemoteException {
        Log.e(Log_Tag,"进程名："+processName +" 崩溃消息: "+longMsg+"  堆栈: "+stackTrace);
        if (processName.equals(BuildConfig.APPLICATION_ID)){
            MainActivity.editor.putString("崩溃",longMsg+"\n\n堆栈：\n"+stackTrace).commit();
            FirebaseCrashlytics.getInstance().log("堆栈信息："+stackTrace);
            return true;
        }
        return false;
    }

    /*
     * 应用即将进入 ANR 状态时触发
     *
     */
    @Override
    public int appEarlyNotResponding(String processName, int pid, String annotation) throws RemoteException {
        if (processName.equals(BuildConfig.APPLICATION_ID)){
            Log.e("ANR","本应用即将进入ANR状态\n\n进程名："+processName+"\nPid: "+pid+"\n原因: "+annotation);
            FirebaseCrashlytics.getInstance().setCustomKey("ANR","即将进入ANR状态 --> 进程名："+processName+"\nPid: "+pid+"\n原因: "+annotation);
        }
        return 0;
    }

    /*
     * 应用进入 ANR 状态时触发
     *
     * return 0 弹出应用无响应 Dialog
     * return 1 继续等待
     * return -1 立即杀死进程
     *
     */
    @Override
    public int appNotResponding(String processName, int pid, String processStats) throws RemoteException {
        if (processName.equals(BuildConfig.APPLICATION_ID)){
            //MainActivity.editor.putString("ANR","\n进程状态：\n"+processStats).commit();
            FirebaseCrashlytics.getInstance().log("ANR: "+processStats);
        }
        return 0;
    }

    @Override
    public int systemNotResponding(String msg) throws RemoteException {
        return 0;
    }
}