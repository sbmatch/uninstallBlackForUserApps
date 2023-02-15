package com.ma.uninstallBlack.service;

import static android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS;

import static com.ma.uninstallBlack.util.OtherUtils.CHANNEL_ID;
import static com.ma.uninstallBlack.util.OtherUtils.userService;

import android.accounts.Account;
import android.accounts.IAccountManagerResponse;
import android.accounts.OnAccountsUpdateListener;
import android.annotation.SuppressLint;
import android.app.IActivityController;
import android.app.IProcessObserver;
import android.app.NotificationManager;
import android.app.Service;
import android.app.admin.SystemUpdatePolicy;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationManagerCompat;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.ma.uninstallBlack.BuildConfig;
import com.ma.uninstallBlack.IAppIPC;
import com.ma.uninstallBlack.IUserService;
import com.ma.uninstallBlack.MainActivity;
import com.ma.uninstallBlack.util.MyAvController;
import com.ma.uninstallBlack.util.OtherUtils;
import com.ma.uninstallBlack.util.ShellUtils;
import com.ma.uninstallBlack.util.ShizukuExecUtils;
import com.ma.uninstallBlack.util.iProcessObserver;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

import rikka.shizuku.Shizuku;

public class MyWorkService extends Service implements MainActivity.ISwitchBlockUninstall{

    static final String LOG_TAG = MyWorkService.class.getSimpleName();
    public final static String netMusicAd_Path = "/storage/emulated/0/Android/data/com.netease.cloudmusic/cache";
    public SharedPreferences sp;
    public Thread h1;
    public SharedPreferences.Editor editor;
    public IProcessObserver iProcessObserver = new iProcessObserver();
    public List<String> removePowerSaveWhiteList = new ArrayList<>();

    public MyWorkService()  {
        removePowerSaveWhiteList.add("moe.shizuku.privileged.api");
        removePowerSaveWhiteList.add("com.google.android.gms");
    }


    @Override
    public IBinder onBind(Intent intent) {
        return (IBinder) userService;
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    public void onCreate() {
        super.onCreate();

        if (editor == null){
            sp = getSharedPreferences("StartSate",MODE_PRIVATE);
            editor = sp.edit();
        }

        editor.putBoolean("是否绑定userService",false).commit();

        new MainActivity().setSwitchUninstallBlackInterfaceCallback(this);

        Log.i(LOG_TAG,"服务成功创建"+" 时间："+ new SimpleDateFormat("HH:mm:ss").format(new Date().getTime()));

        //Intent i = new Intent().setAction(ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.fromParts("package",getPackageName(),null));
        OtherUtils.showNotificationReflect("正在运行后台服务");
        startForeground(Integer.parseInt(CHANNEL_ID),OtherUtils.notification);
        stopForeground(false);
        userService = OtherUtils.getUserService(true);
    }


    @Override
    public String setRemoveAd(boolean z) {
        new Thread(() -> {
            try {
                if (z) {
                    userService.startWatchFromFileObserver(netMusicAd_Path);
                    OtherUtils.showNotificationReflect("监听目录: "+netMusicAd_Path);
                    Log.i(LOG_TAG, "开始监听目录 --> "+netMusicAd_Path);
                    ShizukuExecUtils.ShizukuExec("rm -fr "+netMusicAd_Path+"/Ad");
                }else {
                    userService.stopWatchFromFileObserver();
                    Log.w(LOG_TAG, "已停止监听");
                    OtherUtils.showNotificationReflect("停止监听");
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }).start();
        return null;
    }

    @Override
    public void addPowerSaveForUser(String packageName) {
        try{
            userService.addPowerSaveWhitelistApp(packageName);
        }catch (RemoteException e){
            e.printStackTrace();
        }
    }

    @Override
    public void removePowerSaveForUser(String packageName) {
       h1 = new Thread(() -> {
            Timer timer = new Timer();
            TimerTask task1 = new TimerTask() {
                @SuppressLint("SimpleDateFormat")
                @Override
                public void run() {

                    if (Looper.myLooper() == null) {
                        Looper.prepare();
                    }
                    try {
                        StringBuilder buffer = new StringBuilder();
                        if (userService.getUserPowerWhitelist().length != 0){
                            for (String i_pkg: userService.getUserPowerWhitelist()){
                                String rPkgName = (String) getBaseContext().getPackageManager().getPackageInfo(i_pkg,0).applicationInfo.loadLabel(getBaseContext().getPackageManager());
                                if (removePowerSaveWhiteList.contains(i_pkg)){
                                    Log.i(LOG_TAG,"保持电池优化白名单: ---> "+rPkgName);
                                }else {
                                    Log.i(LOG_TAG, "正在移除电池优化白名单: "+rPkgName+ " 时间："+ new SimpleDateFormat("HH:mm:ss").format(new Date().getTime()));
                                    buffer.append(rPkgName).append(" ");
                                    userService.removePowerWhitelistApp(i_pkg);
                                }
                            }
                            if (buffer.length() > 0){
                                OtherUtils.showNotificationReflect("已移除优化白名单: "+ buffer);
                            }
                        }else {
                            Log.d(LOG_TAG,"用户级电池白名单已全部移除"+" 时间："+ new SimpleDateFormat("HH:mm:ss").format(new Date().getTime()));
                            ToastUtils.showShort("已全部移除");
                        }

                    } catch (NullPointerException |RemoteException | PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }

                }
            };
            timer.schedule(task1,0,10 * 60 * 1000L); //立即开始 每10分钟执行一次
        });

       h1.start();

    }

    @Override
    public boolean isPowerSaveWhitelistApp(String name) throws RemoteException {
        return userService.isPowerSaveWhitelistApp(name);
    }

    @Override
    public void deleteAccount(Account account) throws RemoteException {
        userService.removeAccountAsUser(new IAccountManagerResponse.Stub() {
            @Override
            public void onResult(Bundle value) throws RemoteException {

                try {
                    Log.w(LOG_TAG, "正在直接删除账户：" + account.name + " 状态：" + userService.removeAccountExplicitly(account));
                } catch (Throwable e) {
                    e.fillInStackTrace();
                }

            }

            @Override
            public void onError(int errorCode, String errorMessage) throws RemoteException {
                Log.e(LOG_TAG, "errorCode: " + errorCode + " 错误消息: " + errorMessage);
            }
        }, account, false, 0);
    }

    @Override
    public void killerSelf() throws RemoteException {
        userService.forceStopPackage(BuildConfig.APPLICATION_ID, 0);
    }

    @Override
    public int getApplicationAutoStartReflectForMiui(String pkg) throws RemoteException {
        return OtherUtils.getApplicationAutoStartReflectForMiui(pkg);
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(LOG_TAG,"死亡："+" 时间："+ new SimpleDateFormat("HH:mm:ss").format(new Date().getTime()));
        try {
            userService.ac_unreg_proObserver(iProcessObserver);
            //getContentResolver().unregisterContentObserver(mContentObs);
            userService.stopWatchFromFileObserver();
            OtherUtils.getUserService(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}