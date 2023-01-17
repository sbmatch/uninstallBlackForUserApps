package com.ma.uninstallBlack.util;

import static android.content.Context.APP_OPS_SERVICE;

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
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.IPackageDeleteObserver2;
import android.content.pm.IPackageInstallObserver2;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.VersionedPackage;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.FileUtils;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.IntentCompat;
import androidx.core.content.PackageManagerCompat;
import androidx.core.content.UnusedAppRestrictionsConstants;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.RomUtils;
import com.blankj.utilcode.util.Utils;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.FirebaseApp;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.ma.uninstallBlack.BuildConfig;
import com.ma.uninstallBlack.R;

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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class OtherUtils {

    public static Notification notification;
    public static FileAlterationMonitor monitor;
    static final String LOG_TAG = OtherUtils.class.getSimpleName();


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


    public static boolean isServiceRunning(Context context, @NonNull final String className) {
        try {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
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
    public static void showNotification(Context context, Intent intent, String CHANNEL_ID , String CHANNEL_NAME , String contentText, int importance) {

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notification = new NotificationCompat.Builder(context,CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_baseline_bug_report_24)
                .setAutoCancel(true)
                //.setOngoing(true) // 正在运行后台服务
                .setStyle(new NotificationCompat.BigTextStyle().bigText(contentText))
                .setPriority(NotificationCompat.PRIORITY_HIGH) //优先级
                .setContentIntent(PendingIntent.getActivity(context,1000,intent,PendingIntent.FLAG_IMMUTABLE))
                .build();


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //Android 8.0 以上需包添加渠道
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_MIN);
            notificationChannel.setImportance(importance);
            manager.createNotificationChannel(notificationChannel);
            manager.notify(Integer.parseInt(CHANNEL_ID), notification);
        }
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

        DevicePolicyManager dpm = (DevicePolicyManager) mContext.getSystemService(Service.DEVICE_POLICY_SERVICE);
        try {
            @SuppressLint("SystemApi")
            Method method = dpm.getClass().getMethod("setProfileOwner", ComponentName.class, String.class, int.class);
            return (boolean) method.invoke(dpm, admin, ownerName, userHandle);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static long getLongTime() {
        return new Date().getTime();
    }

    /**
     * 获取Profile所有者
     * @return
     */
    public static ComponentName getProfileOwner() {
        DevicePolicyManager dpm = (DevicePolicyManager) Utils.getApp().getBaseContext().getSystemService(Service.DEVICE_POLICY_SERVICE);
        try {
            Method method = dpm.getClass().getMethod("getProfileOwner");
            return (ComponentName) method.invoke(dpm);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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

}
