package com.ma.uninstallBlack.util;

import android.app.IProcessObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.RemoteException;
import android.util.Log;

import com.blankj.utilcode.util.Utils;

public class iProcessObserver extends IProcessObserver.Stub{
    public static final String LOG_TAG = "IProcessObserver";
    @Override
    public void onProcessDied(int pid, int uid) throws RemoteException {
        String pkg_name = Utils.getApp().getPackageManager().getPackagesForUid(uid)[0];
        if (pkg_name != null) {
            try {
                PackageInfo packageInfo = Utils.getApp().getPackageManager().getPackageInfo(pkg_name,0);
                String appName = (String) packageInfo.applicationInfo.loadLabel(Utils.getApp().getPackageManager());

                Log.e("processObserver","应用: 【"+appName+"】 进程被杀, 进程Id: " + pid );

            }catch (PackageManager.NameNotFoundException e){
                Log.e(LOG_TAG,e.fillInStackTrace()+"");
            }
        }
    }

    @Override
    public void onForegroundServicesChanged(int pid, int uid, int serviceTypes) throws RemoteException {

    }

    @Override
    public void onForegroundActivitiesChanged(int pid, int uid, boolean z) throws RemoteException {

        String pkg_name = Utils.getApp().getPackageManager().getPackagesForUid(uid)[0];

        if (pkg_name !=  null){

            try {
                if (z){
                    String appName = (String) Utils.getApp().getPackageManager().getPackageInfo(pkg_name, 0).applicationInfo.loadLabel(Utils.getApp().getPackageManager());
                    if (!appName.equals("系统桌面")){
                        Log.w("processObserver","前台活动 ---> pid: "+pid +" 应用名: "+appName);
                    }
                }
            }catch (PackageManager.NameNotFoundException ignored){}

        }
    }

    @Override
    public void onProcessStateChanged(int pid, int uid, int procState) throws RemoteException {
        Log.i(LOG_TAG,"pid: "+pid+" 进程状态: "+procState);
    }
}
