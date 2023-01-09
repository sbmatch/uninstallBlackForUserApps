package com.ma.uninstallBlack.service;

import static android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.IAccountManagerResponse;
import android.accounts.OnAccountsUpdateListener;
import android.annotation.SuppressLint;
import android.app.IProcessObserver;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.IPackageDeleteObserver2;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.VersionedPackage;
import android.database.ContentObserver;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.IntentCompat;
import androidx.core.content.PackageManagerCompat;
import androidx.core.content.UnusedAppRestrictionsConstants;

import com.blankj.utilcode.util.LogUtils;
import com.google.common.util.concurrent.ListenableFuture;
import com.ma.uninstallBlack.BuildConfig;
import com.ma.uninstallBlack.IAppIPC;
import com.ma.uninstallBlack.IUserService;
import com.ma.uninstallBlack.activity.DialogActivity;
import com.ma.uninstallBlack.util.MyAvController;
import com.ma.uninstallBlack.util.OtherUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import rikka.shizuku.Shizuku;

public class MyWorkService extends Service {

    static final String LOG_TAG = MyWorkService.class.getSimpleName();
    public final static String s = "/storage/emulated/0/Android/data/com.netease.cloudmusic/cache";
    public static boolean isServiceOK = false, isUserServiceBinded = false, isRemoveAcc = false;
    public IUserService userService;
    private mContentObserver mContentObs;
    public static final int CHANNEL_ID = 2000; //通道id
    private List<String> list,uninstallBlackList = new ArrayList<>();

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
           return isUserServiceBinded;
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
                isUserServiceBinded = true;

                Log.w(LOG_TAG,"userService 绑定成功 ");

               try{

                   if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                       //userService.startWatchFromFileObserver(s);
                       //Log.w(LOG_TAG,"开始监听文件夹");
                       //OtherUtils.startWatch(getBaseContext().getExternalFilesDir(null).getAbsolutePath());
                   }


                   ListenableFuture<Integer> future = PackageManagerCompat.getUnusedAppRestrictionsStatus(getBaseContext());
                   future.addListener(() -> {
                       try{
                           switch (future.get()){
                               case UnusedAppRestrictionsConstants.ERROR:
                                   Log.w("RestrictionsStatus","应用的目标SDK版本小于30 或 用户处于锁定设备引导模式");
                                   break;
                               case UnusedAppRestrictionsConstants.DISABLED:
                                   Log.w("RestrictionsStatus","此应用程序不会自动删除其权限或被休眠");
                                   break;
                               case UnusedAppRestrictionsConstants.FEATURE_NOT_AVAILABLE:
                                   Log.w("RestrictionsStatus","此应用程序没有可用的未使用应用程序限制");
                                   break;
                               case UnusedAppRestrictionsConstants.API_30:
                               case UnusedAppRestrictionsConstants.API_30_BACKPORT:
                                   if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                       Log.w("RestrictionsStatus","已启用未使用应用限制：权限自动重置 是否在自动重置白名单中: "+getBaseContext().getPackageManager().isAutoRevokeWhitelisted());
                                   }
                                   Intent u = IntentCompat.createManageUnusedAppRestrictionsIntent(getBaseContext(),getPackageName());
                                   //ActivityUtils.startActivityForResult(ActivityUtils.getTopActivity(),u,200000);
                                   break;
                               case UnusedAppRestrictionsConstants.API_31:
                                   Log.w("RestrictionsStatus","已启用未使用应用限制：权限自动重置 和 应用休眠");
                                   break;
                           }
                       }catch (ExecutionException| InterruptedException e){
                           e.printStackTrace();
                       }
                   }, ContextCompat.getMainExecutor(getBaseContext()));

                   //List<Uri> list = new ArrayList<>();
                   //list.add(Uri.fromFile(new File("/system/priv-app/PackageInstaller/PackageInstaller.apk")));
                   //userService.doInstallApk(list);

                   for (String b : uninstallBlackList){
                       Log.w(LOG_TAG,b+"  "+userService.getBlockUninstallForUser(b, 0));
                       if (!userService.getBlockUninstallForUser(b, 0)){
                           //userService.setBlockUninstallForUser(b,true,0);
                          // MainActivity.editor.putBoolean(b,true).commit();
                       }else {
                           userService.setBlockUninstallForUser(b,false,0);
                       }
                       //userService.deletePackageVersioned(getVersionedPackage(b),deleteObserver2,0,0x00000002);
                   }

                   //startActivity(new Intent(getBaseContext(), DialogActivity.class).setAction("当前卸载黑名单状态："+userService.getBlockUninstallForUser(getPackageName(),0)).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

//                   Log.w(LOG_TAG,getPackageName()+" 卸载黑名单："+userService.setBlockUninstallForUser(getPackageName(),false,0));

                   //userService.grantRuntimePermission(getPackageName(), Manifest.permission.READ_LOGS,0);

                   //Toast.makeText(MyWorkService.this, "当前卸载黑名单状态："+userService.getBlockUninstallForUser(getPackageName(),0), Toast.LENGTH_SHORT).show();

                   userService.setActivityController(new MyAvController());

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

                  // XCrash.testJavaCrash(true);
               }catch (Exception ignored){}

                new Thread(() -> {
                    Timer timer = new Timer();
                    TimerTask task1 = new TimerTask() {
                        @SuppressLint("SimpleDateFormat")
                        @Override
                        public void run() {

                            if (Looper.myLooper() == null) {
                                Looper.prepare();
                            }
                            try {

                                if (userService.getUserPowerWhitelist().length != 0){
                                    Log.i(LOG_TAG, "正在移除电池优化白名单"+" 时间："+ new SimpleDateFormat("HH:mm:ss").format(new Date().getTime()));
                                    StringBuffer buffer = new StringBuffer();
                                    for (String i_pkg: userService.getUserPowerWhitelist()){
                                        String rPkgName = (String) getBaseContext().getPackageManager().getPackageInfo(i_pkg,0).applicationInfo.loadLabel(getBaseContext().getPackageManager());
                                        Log.w(LOG_TAG,"已移除白名单："+rPkgName);
                                        //list.add(rPkgName);
                                        buffer.append(rPkgName+" ");
                                        userService.removePowerWhitelistApp(i_pkg);
                                    }
                                    OtherUtils.showNotification(getBaseContext(),new Intent(), String.valueOf(MyWorkService.CHANNEL_ID),"后台任务","已移除优化白名单: "+ buffer, NotificationManager.IMPORTANCE_HIGH);
                                }else {
                                    Log.d(LOG_TAG,"用户级电池白名单已全部移除"+" 时间："+ new SimpleDateFormat("HH:mm:ss").format(new Date().getTime()));
                                }

                            } catch (NullPointerException |RemoteException | PackageManager.NameNotFoundException e) {
                                e.printStackTrace();
                            }

                        }
                    };
                    timer.schedule(task1,0,10 * 60 * 1000L); //立即开始 每10分钟执行一次

                }).start();//启动一个线程

            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isUserServiceBinded = false;
            bindUserService();
        }
    };

    private VersionedPackage getVersionedPackage(String pkg) throws PackageManager.NameNotFoundException {
        return new VersionedPackage(pkg,getPackageManager().getPackageInfo(pkg,0).versionCode);
    }

    private final IPackageDeleteObserver2 deleteObserver2 = new IPackageDeleteObserver2.Stub() {
        @Override
        public void onUserActionRequired(Intent intent) throws RemoteException {
            Log.w(LOG_TAG,intent.toString());
        }

        @Override
        public void onPackageDeleted(String packageName, int returnCode, String msg) throws RemoteException {
            //Log.w(LOG_TAG,"包名: "+packageName +" 返回代码："+returnCode +" 消息："+msg);
            try {

                String AppName = (String) getPackageManager().getPackageInfo(packageName,0).applicationInfo.loadLabel(getPackageManager());

                switch (returnCode) {
                    case -1:
                        Log.e(LOG_TAG,"应用 "+AppName+" 卸载失败, 原因未知");
                        break;
                    case -3:
                        Log.e(LOG_TAG, "应用 "+AppName+" 卸载失败, 因为用户受限");
                        break;
                    case -4:
                        Log.e(LOG_TAG, "应用 "+AppName+" 卸载失败, 因为配置文件或设备所有者已将其标记为不可卸载");
                        break;
                    case -6:
                        Log.e(LOG_TAG, "应用 "+AppName+" 卸载失败, 因为该应用是其他已安装应用使用的共享库");
                        break;
                    case 1:
                        Log.i(LOG_TAG, "应用 "+AppName+" 卸载成功");
                        break;
                }
            }catch (Exception e){}
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

    private boolean removeAcc() {
        try{
            for (Account acc : AccountManager.get(getBaseContext()).getAccounts()){
                Log.w("AccountInfo",acc.toString());
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (Looper.myLooper() != null){
                        Looper.prepare();
                    }
                   try{
                       AccountManager.get(MyWorkService.this).addOnAccountsUpdatedListener(listener,new Handler(new Handler.Callback() {
                           @Override
                           public boolean handleMessage(@NonNull Message msg) {
                               Log.w("AccountManager",msg.obj.toString());
                               return true;
                           }
                       }),true);
                   }catch (Throwable e){

                   }
                }
            }).start();
        }catch (Throwable e){
                Log.e(LOG_TAG,e.getMessage());
                    Intent i = new Intent(getBaseContext(), DialogActivity.class);
                    i.setAction(e.getMessage());
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //startActivity(i);
        }
        return false;
    }

    private final Shizuku.OnBinderReceivedListener BINDER_RECEIVED_LISTENER = () -> {
        //ToastUtils.showShort("Shizuku进程启动");
        Log.w(LOG_TAG,"Shizuku进程启动");
        if (!isUserServiceBinded){
            bindUserService();
        }
    };

    private final Shizuku.OnBinderDeadListener BINDER_DEAD_LISTENER = () -> {
        //ToastUtils.showShort("Shizuku进程停止");
        Log.d(LOG_TAG, "Shizuku进程停止");
    };


    public  void bindUserService() {
        try {
            unbindUserService();
            Shizuku.bindUserService(userServiceArgs, userServiceConnection);
        } catch (Throwable ignored) {
        }
    }

    private void unbindUserService() {
        try {
            Shizuku.unbindUserService(userServiceArgs, userServiceConnection, true);
        } catch (Throwable ignored) {
        }
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(LOG_TAG,"服务成功创建"+" 时间："+ new SimpleDateFormat("HH:mm:ss").format(new Date().getTime()));

        isServiceOK = true;
        Shizuku.addBinderReceivedListenerSticky(BINDER_RECEIVED_LISTENER);
        Shizuku.addBinderDeadListener(BINDER_DEAD_LISTENER);

        Intent i = new Intent().setAction(ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.fromParts("package",getPackageName(),null));

        OtherUtils.showNotification(getBaseContext(),i, String.valueOf(CHANNEL_ID),"后台任务","正在运行后台服务", NotificationManager.IMPORTANCE_MIN);

        uninstallBlackList.add("com.tencent.tmgp.sgame");
        uninstallBlackList.add("com.tencent.tmgp.pubgmhd");
        uninstallBlackList.add("com.miHoYo.Yuanshen");
        uninstallBlackList.add("com.netease.sky");
        uninstallBlackList.add("com.tencent.tmgp.cf");

        Uri parse = Uri.parse("content://downloads");

        mContentObs = new mContentObserver(getBaseContext(), new Handler(msg -> {
            LogUtils.w(msg.obj);
            return true;
        }));
        getContentResolver().registerContentObserver(parse,true,mContentObs);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        startForeground(CHANNEL_ID,OtherUtils.notification);

        return START_STICKY_COMPATIBILITY;
    }


    public IProcessObserver iProcessObserver = new  IProcessObserver.Stub(){

        @Override
        public void onProcessDied(int pid, int uid) throws RemoteException {
            String pkg_name = getPackageManager().getPackagesForUid(uid)[0];

            if (pkg_name != null) {
                try {
                    PackageInfo packageInfo = getPackageManager().getPackageInfo(pkg_name,0);

                    if (pkg_name.equals("com.ss.android.ugc.aweme")){
                        Log.e("processObserver",packageInfo.applicationInfo.loadLabel(getPackageManager())+" 进程被杀 pid: " + pid );
                        //removeAcc();
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

    public static class mContentObserver extends ContentObserver{

        public Handler handler;
        public Context context;

        /**
         * Creates a content observer.
         *
         * @param handler The handler to run {@link #onChange} on, or null if none.
         */
        public mContentObserver(Context context,Handler handler) {
            super(handler);
            this.handler = handler;
            this.context = context;
        }


        @Override
        public void onChange(boolean selfChange, @Nullable Uri uri, int flags) {

            Log.w("mContentObs","Uri: "+uri +" Flags:"+flags);
            super.onChange(selfChange, uri, flags);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isServiceOK = false;
        Log.e(LOG_TAG,"死亡："+" 时间："+ new SimpleDateFormat("HH:mm:ss").format(new Date().getTime()));
        try {
            userService.ac_unreg_proObserver(iProcessObserver);
            getContentResolver().unregisterContentObserver(mContentObs);
            userService.stopWatchFromFileObserver();
            //userService.unregisterAccountListener(new String[]{},"com.android.shell");
            //AccountManager.get(getBaseContext()).removeOnAccountsUpdatedListener(listener);
            unbindUserService();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}