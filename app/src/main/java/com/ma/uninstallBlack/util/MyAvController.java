package com.ma.uninstallBlack.util;

import android.annotation.SuppressLint;
import android.app.IActivityController;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.RemoteException;
import android.util.Log;

import com.blankj.utilcode.util.Utils;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.ma.uninstallBlack.BuildConfig;
import com.ma.uninstallBlack.MainActivity;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class MyAvController extends IActivityController.Stub{

    private final String Log_Tag = "ActivityController";
    public PackageManager pm = Utils.getApp().getPackageManager();
    public List<String> xhw = new ArrayList<>();

    @Override
    public boolean activityStarting(Intent intent, String pkg) throws RemoteException {

        //Log.w(Log_Tag,intent+" pkg: "+pkg);
//        if (pkg.equals("com.ma.lockscreen")){
//            Log.w(Log_Tag,"已禁止启动");
//
//            return false;
//        }

        //xhw.add("com.tencent.mobileqq");
        xhw.add("com.xiaomi.market");
        xhw.add("com.android.updater");

        try {
            PackageInfo packageInfo = pm.getPackageInfo(pkg,0);
            String appName = (String) packageInfo.applicationInfo.loadLabel(pm);
            Log.i(Log_Tag,"系统正在尝试启动【"+appName+"】"+" 包名："+pkg);
            if (xhw.contains(pkg)){
                Log.e(Log_Tag,"已阻止启动【"+appName+"】");
                //OtherUtils.showNotificationReflect("已阻止启动【"+appName+"】");
                return false;
            }
        }catch (android.content.pm.PackageManager.NameNotFoundException e){
            e.fillInStackTrace();
        }
        return true;
    }

    @Override
    public boolean activityResuming(String pkg) throws RemoteException {
        //Log.w(Log_Tag,"activityResuming: "+pkg);

        if (xhw.contains(pkg)){
            Log.e(Log_Tag,"已阻止启动【"+pkg+"】");
            //OtherUtils.showNotificationReflect("已阻止启动【"+appName+"】");
            return false;
        }

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
            Log.e("ANR","processName："+processName+" Pid: "+pid+" 原因: "+annotation);
            OtherUtils.showNotificationReflect("即将触发ANR:\nprocessName："+processName+"\nPid: "+pid+"\n原因: "+annotation);
            FirebaseCrashlytics.getInstance().setCustomKey("即将触发ANR","即将进入ANR状态 --> 进程名："+processName+"\nPid: "+pid+"\n原因: "+annotation);
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