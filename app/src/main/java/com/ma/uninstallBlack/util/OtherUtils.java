package com.ma.uninstallBlack.util;

import static android.content.Context.APP_OPS_SERVICE;
import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.app.admin.SystemUpdatePolicy;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.IPackageDeleteObserver2;
import android.content.pm.IPackageInstallObserver2;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.VersionedPackage;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.FileUtils;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.IntentCompat;
import androidx.core.content.PackageManagerCompat;
import androidx.core.content.UnusedAppRestrictionsConstants;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.Utils;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.ma.uninstallBlack.BuildConfig;
import com.ma.uninstallBlack.IUserService;
import com.ma.uninstallBlack.R;
import com.ma.uninstallBlack.receiver.PolicyDeviceAdminReceiver;
import com.ma.uninstallBlack.service.UserService;

import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

import rikka.shizuku.Shizuku;

public class OtherUtils {

    public static Notification notification;
    public static FileAlterationMonitor monitor;
    static final String LOG_TAG = OtherUtils.class.getSimpleName();
    public static DevicePolicyManager dpm = (DevicePolicyManager) Utils.getApp().getApplicationContext().getSystemService(Service.DEVICE_POLICY_SERVICE);

    public static SharedPreferences sp = Utils.getApp().getSharedPreferences("StartSate",MODE_PRIVATE);

    @SuppressLint("StaticFieldLeak")
    public static IUserService userService;
    public static ComponentName adminName = new ComponentName(Utils.getApp().getBaseContext(), PolicyDeviceAdminReceiver.class.getName());

    public static String CHANNEL_ID = "2000"; //通道id
    public static String CHANNEL_NAME = "后台任务"; //通道名

    public OtherUtils(){
    }

    public static boolean checkPackageInstalled(Context context, String pkgName) {
        if (TextUtils.isEmpty(pkgName)) {
            return false;
        }
        try {
            context.getPackageManager().getPackageInfo(pkgName, 0);
        } catch (Exception x) {
            return false;
        }
        return true;
    }


    public static void installByPackageManager(Context context, Uri uri, IPackageInstallObserver2 installObserver2, int flag, String installerPackageName){

        PackageManager packageManager = context.getPackageManager();
        try {
            Method method = packageManager.getClass().getMethod("installPackage", Uri.class, IPackageInstallObserver2.class, int.class, String.class );
            method.invoke(packageManager, uri, installObserver2, flag, installerPackageName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static boolean isServiceRunning(@NonNull final String className) {
        try {
            ActivityManager am = (ActivityManager) Utils.getApp().getBaseContext().getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningServiceInfo> info = am.getRunningServices(0x7FFFFFFF);
            if (info == null || info.size() == 0) return false;
            for (ActivityManager.RunningServiceInfo aInfo : info) {
                if (className.equals(aInfo.service.getClassName())) return true;
            }
            return false;
        } catch (Exception ignore) {
            return false;
        }
    }


    public static WindowManager.LayoutParams getMyParams() {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.height = 550;
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.x = 0;
        layoutParams.y = 0;
        layoutParams.alpha = 0.0f;
        layoutParams.format = PixelFormat.TRANSLUCENT;
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        return layoutParams;
    }

    public static int checkOps(String permission) //调用ops权限管理器校验权限是否真的授权
    {
        AppOpsManager opsMgr = (AppOpsManager) Utils.getApp().getBaseContext().getSystemService(APP_OPS_SERVICE);
        // 检查权限是否已授权
        switch (opsMgr.checkOp(AppOpsManager.permissionToOp(permission), android.os.Process.myUid(), BuildConfig.APPLICATION_ID)) {
            case (AppOpsManager.MODE_ALLOWED):
                Log.i("AppOpsManager", permission + " 已授权");
                return 0;
            case (AppOpsManager.MODE_IGNORED):
                //LogUtils.e(a + " 权限被设置为忽略\n"+ RomUtils.getRomInfo().getName());
                return 1;
            case (AppOpsManager.MODE_ERRORED):
                //LogUtils.e(a + " 权限被设置为拒绝\n"+ RomUtils.getRomInfo().getName());
                return 2;
        }
        return -1;
    }

    // 通知
    public static void showNotification(Intent intent, String CHANNEL_ID , String CHANNEL_NAME , String contentText, int importance) {

        NotificationManager manager = (NotificationManager) Utils.getApp().getBaseContext().getSystemService(Context.NOTIFICATION_SERVICE);

        //Intent i1 = new Intent("com.ma.lockscreen.receiver.exit");
        //intent.setComponent(new ComponentName(BuildConfig.APPLICATION_ID, MyBroadcastReceiver.class.getName()));
        notification = new NotificationCompat.Builder(Utils.getApp().getBaseContext(),CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_baseline_bug_report_24)
                .setAutoCancel(true)
                .setOngoing(false)
                .setStyle(new NotificationCompat.BigTextStyle())
                .setContentText(contentText)
                .setPriority(NotificationCompat.PRIORITY_HIGH) //优先级
                //.addAction(R.drawable.ic_baseline_info_24,"退出",PendingIntent.getBroadcast(Utils.getApp().getBaseContext(),100,i1,PendingIntent.FLAG_IMMUTABLE))
                .setContentIntent(PendingIntent.getActivity(Utils.getApp().getBaseContext(),1000,intent,PendingIntent.FLAG_IMMUTABLE))
                .build();


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //Android 8.0 以上需包添加渠道
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_MIN);
            notificationChannel.setImportance(importance);
            manager.createNotificationChannel(notificationChannel);
            manager.notify(Integer.parseInt(CHANNEL_ID), notification);

            Timer timer = new Timer(true);
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    NotificationManagerCompat.from(Utils.getApp().getApplicationContext()).cancelAll();
                }
            },4000L);
        }
    }


    public static void setSystemUpdatePolicyReflect (SystemUpdatePolicy policy){
        try {
            @SuppressLint("SystemApi")
            Method method = dpm.getClass().getMethod("setSystemUpdatePolicy", ComponentName.class, SystemUpdatePolicy.class);
            method.invoke(dpm,adminName, policy);
        } catch (Exception ignored) {}
    }

    public static void clearDeviceOwnerApp (){
        try{
            dpm.clearDeviceOwnerApp(BuildConfig.APPLICATION_ID);
        }catch (Exception e){
            Log.e(LOG_TAG,e.getMessage());
        }
        //ShizukuExecUtils.ShizukuExec("dpm remove-active-admin com.ma.blackuninstaller/com.ma.uninstallBlack.receiver.PolicyDeviceAdminReceiver");
    }

    /**
     * 设置Profile所有者
     * @param pkgName
     * @param ownerName
     * @param userHandle
     * @return
     */
    public static boolean setProfileOwner(Context mContext ,String pkgName, String ownerName, int userHandle) {

        ComponentName admin = null;

        List<ResolveInfo> infos = mContext.getPackageManager().queryBroadcastReceivers(
                new Intent(DeviceAdminReceiver.ACTION_DEVICE_ADMIN_ENABLED),
                PackageManager.GET_DISABLED_UNTIL_USED_COMPONENTS);
        if (infos != null) {
            for (ResolveInfo info : infos) {
                if (info.activityInfo.packageName.equals(pkgName)) {
                    admin = new ComponentName(pkgName, info.activityInfo.name);
                    if (admin == null) {
                        return false;
                    }
                }
            }
        }

       try {
            @SuppressLint("SystemApi")
            Method method = dpm.getClass().getMethod("setProfileOwner", ComponentName.class, String.class, int.class);
            return (boolean) method.invoke(dpm, admin, ownerName, userHandle);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

   public static SystemUpdatePolicy getSystemUpdatePolicyReflect() {
       try {
           @SuppressLint("SystemApi")
           Method method = dpm.getClass().getMethod("getSystemUpdatePolicy");
           SystemUpdatePolicy policy = (SystemUpdatePolicy) method.invoke(dpm);
           return policy;
       } catch (Exception e) {
           e.printStackTrace();
       }
       return null;
   }


    /**
     * 获取Profile所有者
     * @return
     */
    public static boolean isAdminActive() {
        try {
            Method method = dpm.getClass().getMethod("isAdminActive", ComponentName.class);
            return (boolean) method.invoke(dpm,adminName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    public static boolean getBlockUninstallForUserReflect(String package_name, int i){
        try {
            Object obj = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
            @SuppressLint("PrivateApi")
            Class<?> clazz = Class.forName("android.content.pm.IPackageManager");
            @SuppressLint("DiscouragedPrivateApi")
            Method method = clazz.getDeclaredMethod("getBlockUninstallForUser", String.class,int.class);
            method.setAccessible(true);
            boolean z = (boolean) method.invoke(obj,package_name,i);
            //Log.w("IPackageManager",package_name+" 卸载黑名单状态"+"  -->  "+z);
            return z;
        } catch (ClassNotFoundException | RuntimeException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            FirebaseCrashlytics.getInstance().setCustomKey("反射","方法：android.content.pm.IPackageManager.getBlockUninstallForUser(String.class,int.class) 堆栈: "+e.fillInStackTrace());
        }
        return false;
    }

    public static void showNotificationReflect(String msg){
        try {
            @SuppressLint("PrivateApi")
            Class<?> clazz = OtherUtils.class;
            @SuppressLint("DiscouragedPrivateApi")
            Method method = clazz.getMethod("showNotification", Intent.class, String.class, String.class, String.class, int.class);
            method.setAccessible(true);
            method.invoke(null,new Intent(),CHANNEL_ID,CHANNEL_NAME,msg,NotificationManager.IMPORTANCE_HIGH);
        } catch (RuntimeException | NoSuchMethodException | IllegalAccessException |
                 InvocationTargetException e) {
            Log.e("反射","方法：android.content.pm.IPackageManager.getBlockUninstallForUser(String.class,int.class) 堆栈: "+e.fillInStackTrace());
        }
    }

    public static void showNotificationReflect(Intent intent, String msg){
        try {
            @SuppressLint("PrivateApi")
            Class<?> clazz = OtherUtils.class;
            @SuppressLint("DiscouragedPrivateApi")
            Method method = clazz.getMethod("showNotification", Intent.class, String.class, String.class, String.class, int.class);
            method.setAccessible(true);
            method.invoke(null, intent, CHANNEL_ID, CHANNEL_NAME, msg, NotificationManager.IMPORTANCE_HIGH);
        } catch (RuntimeException | NoSuchMethodException | IllegalAccessException |
                 InvocationTargetException e) {
            Log.e("反射","方法：android.content.pm.IPackageManager.getBlockUninstallForUser(String.class,int.class) 堆栈: "+e.fillInStackTrace());
        }
    }

    public static void requestPermissionsNative(@NonNull Activity activity, @NonNull String[] strArr, int i) {

        if (strArr.length > 0) {
            try {
                Method method = activity.getClass().getMethod("requestPermissions", String[].class, Integer.TYPE);
                method.invoke(activity, strArr, i);
            } catch (Throwable th) {
                LogUtils.e(th);
            }
        }
    }


    public static VersionedPackage getVersionedPackage(String pkg) throws PackageManager.NameNotFoundException {
        return new VersionedPackage(pkg, Utils.getApp().getPackageManager().getPackageInfo(pkg,0).versionCode);
    }

    public static IPackageDeleteObserver2 deleteObserver2 = new IPackageDeleteObserver2.Stub() {
        @Override
        public void onUserActionRequired(Intent intent) throws RemoteException {
            //Log.w("",intent.toString());
        }

        @Override
        public void onPackageDeleted(String packageName, int returnCode, String msg) throws RemoteException {
            //Log.w(LOG_TAG,"包名: "+packageName +" 返回代码："+returnCode +" 消息："+msg);
            try {
                String AppName = (String) Utils.getApp().getPackageManager().getPackageInfo(packageName,0).applicationInfo.loadLabel(Utils.getApp().getPackageManager());

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


    public class mContentObserver extends ContentObserver {

        public Handler handler;
        public Context context;
        public List<Uri> uriBlackList = new ArrayList<>();

        public mContentObserver(Context context,Handler handler) {
            super(handler);
            this.handler = handler;
            this.context = context;
            uriBlackList.add(Uri.parse("content://settings/system/launcher_state"));
            uriBlackList.add(Uri.parse("content://settings/system/count_for_mi_connect"));
            uriBlackList.add(Uri.parse("content://settings/system/screen_brightness"));
            uriBlackList.add(Uri.parse("content://settings/system/contrast_alpha"));
            uriBlackList.add(Uri.parse("content://settings/system/peak_refresh_rate"));
            uriBlackList.add(Uri.parse("content://settings/secure/freeform_timestamps"));
            uriBlackList.add(Uri.parse("content://settings/secure/freeform_window_state"));
            uriBlackList.add(Uri.parse("content://settings/secure/applock_mask_notify"));
            uriBlackList.add(Uri.parse("content://settings/global/ble_scan_low_power_window_ms"));
            uriBlackList.add(Uri.parse("content://settings/global/ble_scan_low_power_interval_ms"));
            uriBlackList.add(Uri.parse("content://settings/system/apptimer_load_data_time"));
            uriBlackList.add(Uri.parse("content://settings/global/fast_connect_ble_scan_mode"));
            uriBlackList.add(Uri.parse("content://settings/global/nsd_on"));
            uriBlackList.add(Uri.parse("content://settings/secure/media_button_receiver"));
            uriBlackList.add(Uri.parse("content://settings/system/next_alarm_formatted"));
        }

        @Override
        public void onChange(boolean selfChange, @Nullable Uri uri, int flags) {
            if (!uriBlackList.contains(uri)) {
                Log.w("mContentObs", "Uri: " + uri + " Flags:" + flags);
                String uristr = uri.toString().replace("content://settings/", "");
                String namespace = Pattern.compile("/").split(uristr)[0];
                Cursor cursor = Utils.getApp().getBaseContext().getContentResolver().query(uri, null, null, null, null);
                while (cursor != null && cursor.moveToNext()) {
                    String name = cursor.getString(1);
                    String value = cursor.getString(2);
                    String inline = "类别：" + namespace + ",名称：" + name + ",值：" + value;
                    //showNotion(new Intent(),inline,NotificationManager.IMPORTANCE_HIGH);
                }
                assert cursor != null;
                cursor.close();
            }
            super.onChange(selfChange, uri, flags);
        }
    }

    public static int getApplicationAutoStartReflectForMiui(String pkg) throws RemoteException {
        try {
            @SuppressLint({"SystemApi", "PrivateApi"})
            Class<?> clazz = Class.forName("android.miui.AppOpsUtils");
            Method method_getApplicationAutoStart = clazz.getMethod("getApplicationAutoStart",Context.class,String.class);
            int i =  (int) method_getApplicationAutoStart.invoke(null,Utils.getApp().getBaseContext(),pkg);
            Log.w("miui.AppOpsUtils","pkg: "+pkg+", autoStart: "+i);
            return i;
        } catch (Exception e) {
            Log.e("miui.AppOpsUtils","反射失败: "+e.getMessage(),e.getCause());
        }
        return 1;
    }


    public static IUserService getUserService (boolean z){
        ComponentName cpn = new ComponentName(BuildConfig.APPLICATION_ID, UserService.class.getName());
        Shizuku.UserServiceArgs userServiceArgs = new Shizuku.UserServiceArgs(cpn).processNameSuffix("service");
        ServiceConnection userServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder binder) {
                if (binder != null && binder.pingBinder()) {
                    userService = IUserService.Stub.asInterface(binder);
                    Log.w(LOG_TAG, "userService 绑定成功");
                    sp.edit().putBoolean("是否绑定userService", true).apply();
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                Log.w(LOG_TAG, "userService断开连接...");
            }
        };

        if (z){
            new Thread(() -> {
                while (userService == null){
                    Shizuku.bindUserService(userServiceArgs,userServiceConnection);
                    Log.i("Shizuku","绑定userService...");
                    try {
                        Thread.sleep(1000L);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();
        }else {
            Shizuku.unbindUserService(userServiceArgs, userServiceConnection, true);
            sp.edit().putBoolean("是否绑定userService",false).apply();
        }

        return  userService;

    }

    public static void 获取未使用时的优化政策(){
        ListenableFuture<Integer> future = PackageManagerCompat.getUnusedAppRestrictionsStatus(Utils.getApp().getBaseContext());
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
                            Log.w("RestrictionsStatus","已启用未使用应用限制：权限自动重置 是否在自动重置白名单中: "+Utils.getApp().getBaseContext().getPackageManager().isAutoRevokeWhitelisted());
                        }
                        Intent u = IntentCompat.createManageUnusedAppRestrictionsIntent(Utils.getApp().getBaseContext(),Utils.getApp().getPackageName());
                        //ActivityUtils.startActivityForResult(ActivityUtils.getTopActivity(),u,200000);
                        break;
                    case UnusedAppRestrictionsConstants.API_31:
                        Log.w("RestrictionsStatus","已启用未使用应用限制：权限自动重置 和 应用休眠");
                        break;
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(Utils.getApp().getBaseContext()));
    }

    public static void startWatch(String file, FileAlterationListener listener, long interval)  {

        monitor = new FileAlterationMonitor(interval);
        FileAlterationObserver observer = new FileAlterationObserver(new File(file));
        monitor.addObserver(observer);
        observer.addListener(listener);
        try {
            monitor.start();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public static void stopWatch() {
       if (monitor != null){
           try {
               monitor.stop();
           }catch (Throwable w){
               w.fillInStackTrace();
           }
       }
    }


    /**
     *
     * Uri 转 File
     * @param context 传入一个上下文
     * @param uri 传入一个 uri 对象
     * @return 返回一个 File 对象
     */
    public static File uriToFileApiQ(Uri uri, Context context) {
        File file = null;
        if (uri == null) return file;
        //android10以上转换
        if (uri.getScheme().equals(ContentResolver.SCHEME_FILE)) {
            file = new File(uri.getPath());
        } else if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            //把文件复制到沙盒目录
            ContentResolver contentResolver = context.getContentResolver();
            String displayName = System.currentTimeMillis() + Math.round((Math.random() + 1) * 1000)
                    + "." + MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri));

            try {
                InputStream is = contentResolver.openInputStream(uri);
                File cache = new File(context.getCacheDir().getAbsolutePath(), displayName);
                FileOutputStream fos = new FileOutputStream(cache);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    FileUtils.copy(is, fos);
                }
                file = cache;
                fos.close();
                is.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    /**
     * 将inputStream转化为file
     * @param is 输入流
     * @param file 要输出的文件目录
     */
    public static void inputStream2File (InputStream is, File file) throws IOException {
        OutputStream os;
        try {
            os = new FileOutputStream(file);
            int len = 0;
            byte[] buffer = new byte[8192];

            while ((len = is.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
            os.close();
        } finally {
            is.close();
        }
    }

//     try {
//        Class<SystemUpdatePolicy> clazz = SystemUpdatePolicy.class;
//        SystemUpdatePolicy policy = clazz.newInstance();
//        @SuppressLint("SoonBlockedPrivateApi")
//        Field f = clazz.getDeclaredField("mPolicyType");
//        f.setAccessible(true);
//        f.setInt(clazz,4);
//        Method getTestMethod = clazz.getDeclaredMethod("getPolicyType");
//        int ii = (int) getTestMethod.invoke(policy);
//        //Log.w("SystemUpdatePolicy",ii+""+f.get(clazz));
//    }catch (Exception e){
//        e.fillInStackTrace();
//    }

}
