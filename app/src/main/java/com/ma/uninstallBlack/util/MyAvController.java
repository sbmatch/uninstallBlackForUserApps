package com.ma.uninstallBlack.util;

import android.app.IActivityController;
import android.content.Intent;
import android.os.RemoteException;
import android.util.Log;

public class MyAvController extends IActivityController.Stub{

    private final String Log_Tag = MyAvController.class.getSimpleName();

    @Override
    public boolean activityStarting(Intent intent, String pkg) throws RemoteException {

        Log.w(Log_Tag,intent+" pkg: "+pkg);

//        if (pkg.equals("com.ma.lockscreen")){
//            Log.w(Log_Tag,"已禁止启动");
//
//            return false;
//        }

        return true;
    }

    @Override
    public boolean activityResuming(String pkg) throws RemoteException {
        Log.w(Log_Tag,"activityResuming: "+pkg);

        return true;
    }

    @Override
    public boolean appCrashed(String processName, int pid, String shortMsg, String longMsg, long timeMillis, String stackTrace) throws RemoteException {
        Log.e(Log_Tag,"进程名："+processName +" 崩溃消息: "+longMsg);
        return false;
    }

    @Override
    public int appEarlyNotResponding(String processName, int pid, String annotation) throws RemoteException {
        return 0;
    }

    @Override
    public int appNotResponding(String processName, int pid, String processStats) throws RemoteException {
        return 0;
    }

    @Override
    public int systemNotResponding(String msg) throws RemoteException {
        return 0;
    }
}