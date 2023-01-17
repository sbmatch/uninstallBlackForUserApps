package com.ma.uninstallBlack.service;

import static android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS;

import android.accounts.Account;
import android.accounts.IAccountManagerResponse;
import android.accounts.OnAccountsUpdateListener;
import android.annotation.SuppressLint;
import android.app.IActivityController;
import android.app.IProcessObserver;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.IPackageDeleteObserver2;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.VersionedPackage;
import android.database.ContentObserver;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.Utils;
import com.google.firebase.FirebaseApp;
import com.ma.uninstallBlack.BuildConfig;
import com.ma.uninstallBlack.IAppIPC;
import com.ma.uninstallBlack.IUserService;
import com.ma.uninstallBlack.MainActivity;
import com.ma.uninstallBlack.util.MyAvController;
import com.ma.uninstallBlack.util.OtherUtils;
import com.ma.uninstallBlack.util.ShizukuExecUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import rikka.shizuku.Shizuku;

public class MyWorkService extends Service implements MainActivity.ISwitchBlockUninstall{

    static final String LOG_TAG = MyWorkService.class.getSimpleName();
    public final static String netMusicAd_Path = "/storage/emulated/0/Android/data/com.netease.cloudmusic/cache";
    public IUserService userService;
    private mContentObserver mContentObs;
    public static int CHANNEL_ID = 2000; //通道id
    public static String CHANNEL_NAME = "后台任务";
    public SharedPreferences sp;

    public Thread h1;
    public SharedPreferences.Editor editor;
    List<String> uninstallBlackList = new ArrayList<>();
    List<String> removePowerSaveWhiteList = new ArrayList<>();

    public MyWorkService()  {
    }



   private final IAppIPC.Stub appIPC = new IAppIPC.Stub() {

       @Override
       public String isServiceAvailable(String clazzName) throws RemoteException {
           return OtherUtils.isServiceRunning(getBaseContext(),clazzName)+"";
       }


       @Override
       public boolean removeUserAccount(Account account) throws RemoteException {

           if (account != null){

               userService.removeAccountAsUser(new IAccountManagerResponse.Stub() {
                   @Override
                   public void onResult(Bundle value) throws RemoteException {
                       Log.w(LOG_TAG, "返回值: " + value.getParcelableArray(null));
                           if (Looper.myLooper() == null) {
                               Looper.prepare();
                           }
                           Log.w(LOG_TAG,"正在移除账号: "+account.name);

                           try{
                               Log.w(LOG_TAG,"正在直接删除账户："+account.name+" 状态："+userService.removeAccountExplicitly(account));
                           }catch (Throwable e){
                               Log.e(LOG_TAG,e.getMessage());
                           }

                           Toast.makeText(MyWorkService.this, "正在移除账号: "+account.name, Toast.LENGTH_SHORT).show();
                       }

                       @Override
                       public void onError(int errorCode, String errorMessage) throws RemoteException {
                           Log.e(LOG_TAG, "errorCode: " + errorCode + " 错误消息: " + errorMessage);
                       }
                   }, account, false, 0);

           }

           return true;
       }

       @Override
       public boolean isUserServiceBinded() throws RemoteException {
           return sp.getBoolean("userService是否绑定成功",false);
       }

       @Override
       public boolean isUninstallBlack(String packageName) throws RemoteException {
           return userService.getBlockUninstallForUser(packageName,0);
       }

       @Override
       public void switchUninstallBlack(String packageName, boolean z) throws RemoteException {
           userService.setBlockUninstallForUser(packageName,z,0);
       }

       @Override
       public void bus() throws RemoteException {
           MyWorkService.this.bindUserService();
       }

       @Override
       public void setACController(IActivityController controller) throws RemoteException {
           userService.setActivityController(controller);
       }
   };

    @Override
    public IBinder onBind(Intent intent) {
        return appIPC;
    }

    private final Shizuku.UserServiceArgs userServiceArgs = new Shizuku.UserServiceArgs(new ComponentName(BuildConfig.APPLICATION_ID, UserService.class.getName())).processNameSuffix("service");

    private  final ServiceConnection userServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder binder) {
            if (binder != null && binder.pingBinder()) {
                userService = IUserService.Stub.asInterface(binder);
                Log.w(LOG_TAG,"userService 绑定成功");
                editor.putBoolean("是否绑定userService",true).commit();

                removePowerSaveWhiteList.add("moe.shizuku.privileged.api");
                removePowerSaveWhiteList.add("com.google.android.gms");

               try{
                   userService.setActivityController(new MyAvController());
                   Log.i("ActivityController","启动崩溃控制台...");

                   if (sp.getBoolean("之后是否直接监听",false)){
                       setRemoveAd(true);
                   }

                   //List<Uri> list = new ArrayList<>();
                   //list.add(Uri.fromFile(new File("/system/priv-app/PackageInstaller/PackageInstaller.apk")));
                   //userService.doInstallApk(list);

//                   for (String b : uninstallBlackList){
//                       Log.w(LOG_TAG,b+"  "+userService.getBlockUninstallForUser(b, 0));
//                       if (!userService.getBlockUninstallForUser(b, 0)){
//                           userService.setBlockUninstallForUser(b,true,0);
//                          MainActivity.editor.putBoolean(b,true).commit();
//                       }else {
//                           userService.setBlockUninstallForUser(b,false,0);
//                       }
//                       userService.deletePackageVersioned(getVersionedPackage(b),deleteObserver2,0,0x00000002);
//                   }

                   //startActivity(new Intent(getBaseContext(), DialogActivity.class).setAction("当前卸载黑名单状态："+userService.getBlockUninstallForUser(getPackageName(),0)).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

                   //userService.grantRuntimePermission(getPackageName(), Manifest.permission.READ_LOGS,0);

                   //userService.registerAccountListener(new String[]{},"com.android.shell");

                   //Log.w(LOG_TAG,"系统更新政策："+userService.getSystemUpdatePolicy());

                  // userService.ac_reg_proObserver(iProcessObserver);
//                   new Thread(() -> {
//                       try {
//                           if (!OtherUtils.checkPackageInstalled(getApplicationContext(),"com.android.packageinstaller")){
//                               userService.exec("pm install --user 0 /system/priv-app/PackageInstaller/PackageInstaller.apk");
//                           }
//                       } catch (RemoteException e) {
//                           e.printStackTrace();
//                       }
//                   }).start();

               }catch (Exception e){
                   e.printStackTrace();
               }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            //bindUserService();
            Log.w(LOG_TAG,"userService断开连接...");
            bindUserService();
        }
    };


    private final android.accounts.OnAccountsUpdateListener listener = new OnAccountsUpdateListener() {
        @Override
        public void onAccountsUpdated(Account[] accounts) {
            for (Account accc : accounts){
                if (accc.type.equals("com.ss.android.ugc.aweme")){
                    try {
                        userService.removeAccountAsUser(new IAccountManagerResponse.Stub() {
                            @Override
                            public void onResult(Bundle value) throws RemoteException {
                                Toast.makeText(getBaseContext(), "" + accc.name + " 正在移除账号", Toast.LENGTH_SHORT).show();
                                Log.w(LOG_TAG, "返回值: " + value.getParcelableArrayList("android.accounts.IAccountManagerResponse"));
                            }

                            @Override
                            public void onError(int errorCode, String errorMessage) throws RemoteException {
                                Log.e(LOG_TAG, "errorCode: " + errorCode + " 错误消息: " + errorMessage);
                            }
                        }, accc, false, 0);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    };


    public  void bindUserService() {
        try {
            Log.i(LOG_TAG,"正在绑定userService...");
            Shizuku.bindUserService(userServiceArgs, userServiceConnection);
        } catch (Throwable i) {
            Log.e(LOG_TAG,i.toString());
        }
    }

    private void unbindUserService() {
        try {
            Shizuku.unbindUserService(userServiceArgs, userServiceConnection, true);
            editor.putBoolean("是否绑定userService",false).commit();
            Log.w(LOG_TAG,"正在解绑userService...");
        } catch (Throwable i) {
            Log.e(LOG_TAG,i.toString());
        }
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

        Intent i = new Intent().setAction(ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.fromParts("package",getPackageName(),null));

        OtherUtils.showNotification(MyWorkService.this,i, String.valueOf(CHANNEL_ID),CHANNEL_NAME,"正在运行后台服务", NotificationManager.IMPORTANCE_MIN);

        startForeground(CHANNEL_ID,OtherUtils.notification);

        Uri parse = Uri.parse("content://settings/system");

        mContentObs = new mContentObserver(getBaseContext(), new Handler(msg -> {
            LogUtils.w(msg.obj);
            return true;
        }));

        getContentResolver().registerContentObserver(parse,true,mContentObs);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (!sp.getBoolean("是否绑定userService",false)){
            bindUserService();
        }else {
            Log.w(LOG_TAG,"已绑定userService...");
        }

        //stopForeground(false);

        return START_STICKY;
    }

    public void showNotion(String n){
        ThreadUtils.runOnUiThread(() -> OtherUtils.showNotification(getBaseContext(),new Intent(), MyWorkService.CHANNEL_ID+"",MyWorkService.CHANNEL_NAME,n, NotificationManager.IMPORTANCE_DEFAULT));
    }

    public IProcessObserver iProcessObserver = new IProcessObserver.Stub(){

        @Override
        public void onProcessDied(int pid, int uid) throws RemoteException {
            String pkg_name = getPackageManager().getPackagesForUid(uid)[0];
            if (pkg_name != null) {
                try {
                    PackageInfo packageInfo = getPackageManager().getPackageInfo(pkg_name,0);

                    if (pkg_name.equals("com.ss.android.ugc.aweme")){
                        Log.e("processObserver",packageInfo.applicationInfo.loadLabel(getPackageManager())+" 进程被杀 pid: " + pid );
                    }

                }catch (PackageManager.NameNotFoundException e){
                    Log.e(LOG_TAG,e.fillInStackTrace()+"");
                }
            }
        }

        @Override
        public void onForegroundServicesChanged(int pid, int uid, int serviceTypes) throws RemoteException {
            String pkg_name = getPackageManager().getPackagesForUid(uid)[0];
            try{
                Log.w(LOG_TAG,"前台服务："+pkg_name +"  pid: "+pid+" 服务类型: "+serviceTypes +" 应用名："+getPackageManager().getPackageInfo(pkg_name, 0).applicationInfo.loadLabel(getPackageManager()));
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        @Override
        public void onForegroundActivitiesChanged(int pid, int uid, boolean z) throws RemoteException {

            String pkg_name = getPackageManager().getPackagesForUid(uid)[0];

            if (pkg_name !=  null){

                try {
                    if (z){
                        String appName = (String) getPackageManager().getPackageInfo(pkg_name, 0).applicationInfo.loadLabel(getPackageManager());
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

    };

    @Override
    public void SwitchMsg(String pkgName, boolean blockUninstall) {
        try{
            userService.setBlockUninstallForUser(pkgName,blockUninstall,0);
        }catch (RemoteException e){
            e.printStackTrace();
        }
    }

    @Override
    public String setRemoveAd(boolean z) {
        new Thread(() -> {
            try {
                if (z) {
                    userService.startWatchFromFileObserver(netMusicAd_Path);
                    Log.i(LOG_TAG, "开始监听目录 --> "+netMusicAd_Path);
                    OtherUtils.showNotification(getBaseContext(),new Intent(), String.valueOf(MyWorkService.CHANNEL_ID),CHANNEL_NAME,"监听目录: "+netMusicAd_Path, NotificationManager.IMPORTANCE_DEFAULT);
                    ShizukuExecUtils.ShizukuExec(getBaseContext(),"rm -fr "+netMusicAd_Path+"/Ad");
                }else {
                    userService.stopWatchFromFileObserver();
                    Log.w(LOG_TAG, "已停止监听");
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
                            OtherUtils.showNotification(getBaseContext(),new Intent(), String.valueOf(MyWorkService.CHANNEL_ID),CHANNEL_NAME,"已移除优化白名单: "+ buffer, NotificationManager.IMPORTANCE_HIGH);
                        }else {
                            Log.d(LOG_TAG,"用户级电池白名单已全部移除"+" 时间："+ new SimpleDateFormat("HH:mm:ss").format(new Date().getTime()));
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

    public static class mContentObserver extends ContentObserver{

        public Handler handler;
        public Context context;
        public List<Uri> uriIgnoreList = new ArrayList<>();

        public mContentObserver(Context context,Handler handler) {
            super(handler);
            this.handler = handler;
            this.context = context;
            this.uriIgnoreList.add(Uri.parse("content://settings/system/launcher_state"));
            this.uriIgnoreList.add(Uri.parse("content://settings/system/count_for_mi_connect"));
            this.uriIgnoreList.add(Uri.parse("content://settings/system/screen_brightness"));
            this.uriIgnoreList.add(Uri.parse("content://settings/system/contrast_alpha"));
            //this.uriIgnoreList.add(Uri.parse(""));
            //this.uriIgnoreList.add(Uri.parse(""));
            //this.uriIgnoreList.add(Uri.parse(""));
            //this.uriIgnoreList.add(Uri.parse(""));
        }

        @Override
        public void onChange(boolean selfChange, @Nullable Uri uri, int flags) {
            if (!uriIgnoreList.contains(uri)) {
                Log.w("mContentObs", "Uri: " + uri + " Flags:" + flags);
            }
            super.onChange(selfChange, uri, flags);
        }
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(LOG_TAG,"死亡："+" 时间："+ new SimpleDateFormat("HH:mm:ss").format(new Date().getTime()));
        try {
            userService.ac_unreg_proObserver(iProcessObserver);
            getContentResolver().unregisterContentObserver(mContentObs);
            userService.stopWatchFromFileObserver();
            unbindUserService();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}